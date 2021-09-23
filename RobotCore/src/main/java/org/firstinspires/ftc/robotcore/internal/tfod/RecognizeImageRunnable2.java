/*
 * Copyright (C) 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.firstinspires.ftc.robotcore.internal.tfod;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import java.util.List;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

/**
 * Subclass of RecognizeImageRunnable that uses {@link Interpreter}.
 *
 * @author Liz Looney
 */
class RecognizeImageRunnable2 extends RecognizeImageRunnable {

  private final ObjectDetector objectDetector;

  RecognizeImageRunnable2(
      AnnotatedYuvRgbFrame annotatedFrame,
      CameraInformation cameraInformation,
      ObjectDetector objectDetector,
      TfodParameters params,
      double zoomMagnification,
      double zoomAspectRatio,
      List<String> labels,
      AnnotatedFrameCallback callback) {
    super(annotatedFrame, cameraInformation, params, zoomMagnification, zoomAspectRatio, labels,
        callback);
    this.objectDetector = objectDetector;
  }

  @Override
  protected void processFrame() {
    Timer timer = new Timer(annotatedFrame.getTag());
    timer.start("RecognizeImageRunnable2.preprocessFrame");
    TensorImage tensorImage = preprocessFrame();
    timer.end();

    timer.start("ObjectDetector.detect");
    List<Detection> detections = objectDetector.detect(tensorImage);
    timer.end();

    // Postprocess detections
    timer.start("RecognizeImageRunnable2.postprocessDetections");
    postprocessDetections(detections);
    timer.end();
  }

  private TensorImage preprocessFrame() {
    return annotatedFrame.getFrame()
        .getTensorImage(params.inputSize, zoomMagnification, zoomAspectRatio);
  }

  private void postprocessDetections(List<Detection> detections) {
    for (Detection detection : detections) {
      for (Category category : detection.getCategories()) {
        float detectionScore = category.getScore();
        if (detectionScore < params.minResultConfidence) {
          continue;
        }

        int detectedClass = category.getIndex();
        if (detectedClass < 0 || detectedClass >= labels.size()) {
          Log.w(annotatedFrame.getTag(), "RecognizeImageRunnable2.postprocessDetections - got a recognition with an invalid / background label: " + category);
          continue;
        }

        RectF detectionBox = convertDetectionBoundingBox(detection.getBoundingBox());

        annotatedFrame.getRecognitions().add(
            new RecognitionImpl(cameraInformation, labels.get(detectedClass), detectionScore, detectionBox));
      }
    }
  }

  private RectF convertDetectionBoundingBox(RectF detectionBoundingBox) {
    // Return the rectangles in the coordinates of the bitmap we were originally given.
    // Note that this conversion switches the indices from what is returned by the model to
    // what is expected by RectF. The model gives [top, left, bottom, right], while RectF
    // expects [left, top, right, bottom].
    int frameWidth = annotatedFrame.getFrame().getWidth();
    int frameHeight = annotatedFrame.getFrame().getHeight();

    if (Zoom.isZoomed(zoomMagnification)) {
      Rect zoomArea = Zoom.getZoomArea(zoomMagnification, zoomAspectRatio, frameWidth, frameHeight);
      return new RectF(
          detectionBoundingBox.left / params.inputSize * zoomArea.width() + zoomArea.left,
          detectionBoundingBox.top / params.inputSize * zoomArea.height() + zoomArea.top,
          detectionBoundingBox.right / params.inputSize * zoomArea.width() + zoomArea.left,
          detectionBoundingBox.bottom / params.inputSize * zoomArea.height() + zoomArea.top);
    }

    return new RectF(
        detectionBoundingBox.left / params.inputSize * frameWidth,
        detectionBoundingBox.top / params.inputSize * frameHeight,
        detectionBoundingBox.right / params.inputSize * frameWidth,
        detectionBoundingBox.bottom / params.inputSize * frameHeight);
  }
}
