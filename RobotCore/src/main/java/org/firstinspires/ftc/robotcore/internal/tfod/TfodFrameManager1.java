/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.firstinspires.ftc.robotcore.internal.tfod;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.tfod.CameraInformation;
import org.firstinspires.ftc.robotcore.internal.tfod.LabeledObject.CoordinateSystem;
import org.tensorflow.lite.Interpreter;

/**
 * Subclass of TfodFrameManager that uses {@link Interpreter} to support TensorFlow 1 object
 * detection models.
 *
 * @author Vasu Agrawal
 * @author lizlooney@google.com (Liz Looney)
 */
class TfodFrameManager1 extends TfodFrameManager {
  // Constants for float model, used to normalize float images to [-1, 1]
  private static final float IMAGE_MEAN = 128.0f;
  private static final float IMAGE_STD = 128.0f;

  TfodFrameManager1(MappedByteBuffer modelData, List<String> labels, TfodParameters params,
      CameraInformation cameraInformation, ClippingMargins clippingMargins, Zoom zoom,
      ResultsCallback resultsCallback, Consumer<Bitmap> annotatedFrameCallback) {
    super(modelData, labels, params, cameraInformation, clippingMargins, zoom, resultsCallback,
        annotatedFrameCallback);
  }

  @Override
  protected RecognizerPipeline createRecognizerPipeline(MappedByteBuffer modelData, ZoomHelper zoomHelper, Bitmap zoomBitmap) {
    return new RecognizerPipeline1(modelData, zoomHelper, zoomBitmap);
  }

  private class RecognizerPipeline1 extends RecognizerPipeline {
    private final Interpreter interpreter;
    private final int[] argb8888Array;
    private final ByteBuffer imgData;
    private final float[][][] outputLocations;
    private final float[][] outputClasses;
    private final float[][] outputScores;
    private final float[] numDetections;

    private RecognizerPipeline1(MappedByteBuffer modelData, ZoomHelper zoomHelper, Bitmap zoomBitmap) {
      super(zoomHelper, zoomBitmap);

      interpreter = new Interpreter(modelData, params.numInterpreterThreads);

      argb8888Array = new int[4 * params.inputSize * params.inputSize];

      // Allocate the ByteBuffer to be passed as the input to the network
      int numBytesPerChannel = params.isModelQuantized ? 1 /* Quantized */ : 4 /* Floating Point */;
      // capacity = (Width) * (Height) * (Channels) * (Bytes Per Channel)
      int capacity = params.inputSize * params.inputSize * 3 * numBytesPerChannel;
      imgData = ByteBuffer.allocateDirect(capacity);
      imgData.order(ByteOrder.nativeOrder());
      imgData.rewind();

      // Create the output arrays for the interpreter.
      outputLocations = new float[1][params.maxNumDetections][4];
      outputClasses = new float[1][params.maxNumDetections];
      outputScores = new float[1][params.maxNumDetections];
      numDetections = new float[1];
    }

    @Override
    protected void processFrame(long frameTimeNanos) {
      updateBitmapForTfod();

      bitmapForTfod.getPixels(argb8888Array, 0, params.inputSize, 0, 0, params.inputSize, params.inputSize);

      // Copy the data into the ByteBuffer
      ByteBuffer imgData = this.imgData.duplicate();
      for (int i = 0; i < params.inputSize; ++i) {
        for (int j = 0; j < params.inputSize; ++j) {
          int pixelValue = argb8888Array[i * params.inputSize + j];
          if (params.isModelQuantized) { // Quantized model
            // Copy as-is
            imgData.put((byte) ((pixelValue >> 16) & 0xFF));
            imgData.put((byte) ((pixelValue >> 8) & 0xFF));
            imgData.put((byte) (pixelValue & 0xFF));
          } else { // Float model
            // Copy with normalization
            imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
          }
        }
      }

      // Generate the input array and output map to feed the TensorFlow Lite interpreter
      Object[] inputArray = {imgData};
      Map<Integer, Object> outputMap = new HashMap<>();
      outputMap.put(0, outputLocations);
      outputMap.put(1, outputClasses);
      outputMap.put(2, outputScores);
      outputMap.put(3, numDetections);
      interpreter.runForMultipleInputsOutputs(inputArray, outputMap);

      List<LabeledObject> labeledObjects = postProcessDetections(
          outputLocations[0], outputClasses[0], outputScores[0]);
      onResultsFromRecognizerPipeline(frameTimeNanos, labeledObjects, zoomBitmap);
    }

    private List<LabeledObject> postProcessDetections(
        float[][] outputLocations, float[] outputClasses, float[] outputScores) {
      List<LabeledObject> labeledObjects = new ArrayList<>();
      for (int i = 0; i < params.maxNumDetections; i++) {
        // The network will always generate MAX_NUM_DETECTIONS results.
        final float detectionScore = outputScores[i];
        if (detectionScore < params.minResultConfidence) {
          continue;
        }

        // First, determine the label, and make sure it is within bounds.
        int detectedClass = (int) outputClasses[i];

        // We've observed that the detections can some times be out of bounds (potentially when the
        // network isn't confident enough in any positive results) so this keeps the labels in bounds.
        if (detectedClass < 0 || detectedClass >= labels.size()) {
          Log.w("RecognizerPipeline1.postProcessDetections",
              "got a detection with an invalid class: " + detectedClass);
          continue;
        }

        // Note(lizlooney): the model gives [top, left, bottom, right], while RectF expects
        // [left, top, right, bottom].
        RectF detectionBox = new RectF(
            outputLocations[i][1] * params.inputSize,
            outputLocations[i][0] * params.inputSize,
            outputLocations[i][3] * params.inputSize,
            outputLocations[i][2] * params.inputSize);
        // Convert the box to zoom area coordinates.
        tfodToZoomAreaMatrix.mapRect(detectionBox);

        LabeledObject labeledObject = new LabeledObject(
            labels.get(detectedClass), detectionScore,
            zoomHelper, CoordinateSystem.ZOOM_AREA,
            detectionBox.left, detectionBox.top, detectionBox.right, detectionBox.bottom);
        labeledObjects.add(labeledObject);
      }
      return labeledObjects;
    }
  }
}