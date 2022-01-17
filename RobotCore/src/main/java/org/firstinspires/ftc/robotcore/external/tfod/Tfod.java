/**
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

package org.firstinspires.ftc.robotcore.external.tfod;

import static org.firstinspires.ftc.robotcore.internal.system.AppUtil.TFLITE_MODELS_DIR;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaBase;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

/**
 * A class that provides simplified access to TensorFlow Object Detection.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class Tfod {
  final TFObjectDetector.Parameters parameters;
  private String assetName;
  private String fileName;
  private String[] labels;
  private TFObjectDetector tfod;

  public Tfod() {
    parameters = new TFObjectDetector.Parameters();

    // If the user doesn't explicitly set a model, we'll use the default one.
    useDefaultModel();
  }

  public void useDefaultModel() {
    assetName = TfodCurrentGame.TFOD_MODEL_ASSET;
    fileName = null;
    labels = TfodCurrentGame.LABELS;
    parameters.isModelTensorFlow2 = TfodCurrentGame.IS_MODEL_TENSOR_FLOW_2;
    parameters.isModelQuantized = TfodCurrentGame.IS_MODEL_QUANTIZED;
    parameters.inputSize = TfodCurrentGame.INPUT_SIZE;
  }

  // Blocks-to-Java generated code uses List<String>
  public void useModelFromAsset(String assetName, List<String> labels,
      boolean isModelTensorFlow2, boolean isModelQuantized, int inputSize) {
    useModelFromAsset(assetName, labels.toArray(new String[labels.size()]),
        isModelTensorFlow2, isModelQuantized, inputSize);
  }

  // Blocks-to-Java generated code uses List<String>
  // This is for the legacy block tfodLegacy_setModelFromAsset.
  public void useModelFromAsset(String assetName, List<String> labels) {
    useModelFromAsset(assetName, labels.toArray(new String[labels.size()]));
  }

  // Blocks code uses String[]
  public void useModelFromAsset(String assetName, String[] labels,
      boolean isModelTensorFlow2, boolean isModelQuantized, int inputSize) {
    if (tfod != null) {
      throw new IllegalStateException("You may not call Tfod.useModelFromAsset after Tfod.initialize!");
    }
    // Here we just check that the asset exists so we can give an error message.
    try {
      AssetManager assetManager = AppUtil.getDefContext().getAssets();
      AssetFileDescriptor afd = assetManager.openFd(assetName);
      afd.close();
    } catch (IOException e) {
      throw new IllegalStateException("Could not find asset named " + assetName);
    }
    this.assetName = assetName;
    this.fileName = null;
    this.labels = labels;
    parameters.isModelTensorFlow2 = isModelTensorFlow2;
    parameters.isModelQuantized = isModelQuantized;
    parameters.inputSize = inputSize;
  }

  // Blocks code uses String[]
  // This is for the legacy block tfodLegacy_setModelFromAsset.
  public void useModelFromAsset(String assetName, String[] labels) {
    if (tfod != null) {
      throw new IllegalStateException("You may not call Tfod.useModelFromAsset after Tfod.initialize!");
    }
    // Here we just check that the asset exists so we can give an error message.
    try {
      AssetManager assetManager = AppUtil.getDefContext().getAssets();
      AssetFileDescriptor afd = assetManager.openFd(assetName);
      afd.close();
    } catch (IOException e) {
      throw new IllegalStateException("Could not find asset named " + assetName);
    }
    this.assetName = assetName;
    this.fileName = null;
    this.labels = labels;
  }

  // Blocks-to-Java generated code uses List<String>
  public void useModelFromFile(String fileName, List<String> labels,
      boolean isModelTensorFlow2, boolean isModelQuantized, int inputSize) {
    useModelFromFile(fileName, labels.toArray(new String[labels.size()]),
        isModelTensorFlow2, isModelQuantized, inputSize);
  }

  // Blocks-to-Java generated code uses List<String>
  // This is for the legacy block tfodLegacy_setModelFromFile.
  public void useModelFromFile(String fileName, List<String> labels) {
    useModelFromFile(fileName, labels.toArray(new String[labels.size()]));
  }

  // Blocks code uses String[]
  public void useModelFromFile(String fileName, String[] labels,
      boolean isModelTensorFlow2, boolean isModelQuantized, int inputSize) {
    if (tfod != null) {
      throw new IllegalStateException("You may not call Tfod.useModelFromFile after Tfod.initialize!");
    }
    // Here we just check that the file exists so we can give an error message.
    File file = new File(TFLITE_MODELS_DIR, fileName);
    if (!file.exists()) {
      file = new File(fileName);
      if (!file.exists()) {
        throw new IllegalStateException("Could not find file named " + fileName);
      }
    }
    this.assetName = null;
    this.fileName = fileName;
    this.labels = labels;
    parameters.isModelTensorFlow2 = isModelTensorFlow2;
    parameters.isModelQuantized = isModelQuantized;
    parameters.inputSize = inputSize;
  }

  // Blocks code uses String[]
  // This is for the legacy block tfodLegacy_setModelFromFile.
  public void useModelFromFile(String fileName, String[] labels) {
    if (tfod != null) {
      throw new IllegalStateException("You may not call Tfod.useModelFromFile after Tfod.initialize!");
    }
    // Here we just check that the file exists so we can give an error message.
    File file = new File(TFLITE_MODELS_DIR, fileName);
    if (!file.exists()) {
      file = new File(fileName);
      if (!file.exists()) {
        throw new IllegalStateException("Could not find file named " + fileName);
      }
    }
    this.assetName = null;
    this.fileName = fileName;
    this.labels = labels;
  }

  /**
   * Initializes TensorFlow Object Detection.
   */
  public void initialize(VuforiaBase vuforiaBase, float minimumConfidence, boolean useObjectTracker,
      boolean enableCameraMonitoring) {
    if (assetName != null && fileName != null) {
      throw new IllegalStateException("You must call Tfod.useDefaultModel, " +
          "Tfod.useModelFromAsset, or Tfod.useModelFromFile before Tfod.initialize!");
    }
    parameters.minimumConfidence = minimumConfidence;
    parameters.minResultConfidence = minimumConfidence;
    parameters.useObjectTracker = useObjectTracker;
    setEnableCameraMonitoring(enableCameraMonitoring);
    initialize(vuforiaBase.getVuforiaLocalizer());
  }

  /**
   * Initializes TensorFlow Object Detection.
   * This is for the legacy block tfodLegacy_initialize_withIsModelTensorFlow2.
   */
  public void initialize(VuforiaBase vuforiaBase, float minimumConfidence, boolean useObjectTracker,
      boolean enableCameraMonitoring, boolean isModelTensorFlow2) {
    if (assetName != null && fileName != null) {
      throw new IllegalStateException("You must call Tfod.useDefaultModel, " +
          "Tfod.useModelFromAsset, or Tfod.useModelFromFile before Tfod.initialize!");
    }
    parameters.minimumConfidence = minimumConfidence;
    parameters.minResultConfidence = minimumConfidence;
    parameters.useObjectTracker = useObjectTracker;
    setEnableCameraMonitoring(enableCameraMonitoring);
    parameters.isModelTensorFlow2 = isModelTensorFlow2;
    initialize(vuforiaBase.getVuforiaLocalizer());
  }

  private void setEnableCameraMonitoring(boolean enableCameraMonitoring) {
    if (enableCameraMonitoring) {
      Context context = AppUtil.getInstance().getRootActivity();
      parameters.tfodMonitorViewIdParent = context.getResources().getIdentifier(
          "tfodMonitorViewId", "id", context.getPackageName());
    }
  }

  /**
   * Initializes TensorFlow Object Detection.
   */
  public void initialize(VuforiaBase vuforiaBase, float minimumConfidence, boolean useObjectTracker,
      boolean enableCameraMonitoring, int numInterpreterThreads, int numExecutorThreads,
      int maxNumDetections, int timingBufferSize, double maxFrameRate,
      float trackerMaxOverlap, float trackerMinSize,
      float trackerMarginalCorrelation, float trackerMinCorrelation) {
    parameters.minimumConfidence = minimumConfidence;
    parameters.minResultConfidence = minimumConfidence;
    parameters.useObjectTracker = useObjectTracker;
    setEnableCameraMonitoring(enableCameraMonitoring);
    parameters.numInterpreterThreads = numInterpreterThreads;
    parameters.numExecutorThreads = numExecutorThreads;
    parameters.maxNumDetections = maxNumDetections;
    parameters.timingBufferSize = timingBufferSize;
    parameters.maxFrameRate = maxFrameRate;
    parameters.trackerMaxOverlap = trackerMaxOverlap;
    parameters.trackerMinSize = trackerMinSize;
    parameters.trackerMarginalCorrelation = trackerMarginalCorrelation;
    parameters.trackerMinCorrelation = trackerMinCorrelation;
    initialize(vuforiaBase.getVuforiaLocalizer());
  }

  /**
   * Initializes TensorFlow Object Detection.
   * This is for the legacy block tfodLegacy_initialize_withAllArgs.
   */
  public void initialize(VuforiaBase vuforiaBase,
      float minimumConfidence, boolean useObjectTracker, boolean enableCameraMonitoring,
      boolean isModelTensorFlow2, boolean isModelQuantized, int inputSize,
      int numInterpreterThreads, int numExecutorThreads,
      int maxNumDetections, int timingBufferSize, double maxFrameRate,
      float trackerMaxOverlap, float trackerMinSize,
      float trackerMarginalCorrelation, float trackerMinCorrelation) {
    parameters.minimumConfidence = minimumConfidence;
    parameters.minResultConfidence = minimumConfidence;
    parameters.useObjectTracker = useObjectTracker;
    setEnableCameraMonitoring(enableCameraMonitoring);
    parameters.isModelTensorFlow2 = isModelTensorFlow2;
    parameters.isModelQuantized = isModelQuantized;
    parameters.inputSize = inputSize;
    parameters.numInterpreterThreads = numInterpreterThreads;
    parameters.numExecutorThreads = numExecutorThreads;
    parameters.maxNumDetections = maxNumDetections;
    parameters.timingBufferSize = timingBufferSize;
    parameters.maxFrameRate = maxFrameRate;
    parameters.trackerMaxOverlap = trackerMaxOverlap;
    parameters.trackerMinSize = trackerMinSize;
    parameters.trackerMarginalCorrelation = trackerMarginalCorrelation;
    parameters.trackerMinCorrelation = trackerMinCorrelation;
    initialize(vuforiaBase.getVuforiaLocalizer());
  }

  private void initialize(VuforiaLocalizer vuforiaLocalizer) {
    tfod = ClassFactory.getInstance().createTFObjectDetector(parameters, vuforiaLocalizer);
    if (assetName != null) {
      tfod.loadModelFromAsset(assetName, labels);
    } else {
      tfod.loadModelFromFile(fileName, labels);
    }
  }

  /**
   * Activates object detection.
   *
   * @throws IllegalStateException if initialized has not been called yet.
   */
  public void activate() {
    if (tfod == null) {
      throw new IllegalStateException("You forgot to call Tfod.initialize!");
    }
    tfod.activate();
  }

  /**
   * Deactivates object detection.
   *
   * @throws IllegalStateException if initialized has not been called yet.
   */
  public void deactivate() {
    if (tfod == null) {
      throw new IllegalStateException("You forgot to call Tfod.initialize!");
    }
    tfod.deactivate();
  }

  /**
   * Sets the number of pixels to obscure on the left, top, right, and bottom edges of each image
   * passed to the TensorFlow object detector. The size of the images are not changed, but the
   * pixels in the margins are colored black.
   */
  public void setClippingMargins(int left, int top, int right, int bottom) {
    if (tfod == null) {
      throw new IllegalStateException("You forgot to call Tfod.initialize!");
    }
    tfod.setClippingMargins(left, top, right, bottom);
  }

  /**
   * Indicates that only the zoomed center area of each image will be passed to the TensorFlow
   * object detector. For no zooming, set magnification to 1.0. For best results, the aspect ratio
   * should match the aspect ratio of the images that were used to train the TensorFlow model
   * (1.7777 for 16/9).
   */
  public void setZoom(double magnification, double aspectRatio) {
    if (tfod == null) {
      throw new IllegalStateException("You forgot to call Tfod.initialize!");
    }
    tfod.setZoom(magnification, aspectRatio);
  }

  /**
   * Returns the list of recognitions, but only if they are different than the last call to {@link #getUpdatedRecognitions()}.
   */
  public List<Recognition> getUpdatedRecognitions() {
    if (tfod == null) {
      throw new IllegalStateException("You forgot to call Tfod.initialize!");
    }
    return tfod.getUpdatedRecognitions();
  }

  /**
   * Returns the list of recognitions.
   */
  public List<Recognition> getRecognitions() {
    if (tfod == null) {
      throw new IllegalStateException("You forgot to call Tfod.initialize!");
    }
    return tfod.getRecognitions();
  }

  /**
   * Deactivates object detection and cleans up.
   */
  public void close() {
    if (tfod != null) {
      tfod.deactivate();
      tfod.shutdown();
      tfod = null;
    }
  }
}
