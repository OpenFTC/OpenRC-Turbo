/*
 * Copyright 2018 Google LLC
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
import java.io.File;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaBase;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

/**
 * An abstract base class that provides simplified access to TensorFlow Object Detection.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class TfodBase {
  private String assetName;
  private String tfliteModelFilename;
  private String[] labels;
  private boolean isModelTensorFlow2;

  private TFObjectDetector tfod;

  protected TfodBase(String assetName, String[] labels) {
    this.setModelFromAsset(assetName, labels);
  }

  protected TfodBase(String assetName, String[] labels, boolean isModelTensorFlow2) {
    this.setModelFromAsset(assetName, labels);
    this.isModelTensorFlow2 = isModelTensorFlow2;
  }

  protected TfodBase() {
  }

  public void setModelFromAsset(String assetName, List<String> labels) {
    setModelFromAsset(assetName, labels.toArray(new String[labels.size()]));
  }

  public void setModelFromAsset(String assetName, String[] labels) {
    if (tfod != null) {
      throw new IllegalStateException("You may not call setModelFromAsset after Tfod.initialize!");
    }
    this.assetName = assetName;
    this.tfliteModelFilename = null;
    this.labels = labels;
  }

  public void setModelFromFile(String tfliteModelFilename, List<String> labels) {
    setModelFromFile(tfliteModelFilename, labels.toArray(new String[labels.size()]));
  }

  public void setModelFromFile(String tfliteModelFilename, String[] labels) {
    if (tfod != null) {
      throw new IllegalStateException("You may not call setModelFromFile after Tfod.initialize!");
    }
    this.assetName = null;
    this.tfliteModelFilename = tfliteModelFilename;
    this.labels = labels;
  }

  /**
   * Initializes TensorFlow Object Detection.
   */
  public void initialize(VuforiaBase vuforiaBase, float minimumConfidence, boolean useObjectTracker,
      boolean enableCameraMonitoring) {
    if (assetName != null && tfliteModelFilename != null) {
      throw new IllegalStateException("assetName and tfliteModelFilename are both non-null!");
    }

    TFObjectDetector.Parameters parameters = TfodBase.makeParameters(minimumConfidence, useObjectTracker, enableCameraMonitoring);
    parameters.isModelTensorFlow2 = this.isModelTensorFlow2;
    initializeWithParameters(vuforiaBase.getVuforiaLocalizer(), parameters);
  }

  /**
   * Initializes TensorFlow Object Detection.
   */
  public void initialize(VuforiaLocalizer vuforiaLocalizer, float minimumConfidence, boolean useObjectTracker,
      boolean enableCameraMonitoring) {
    if (assetName != null && tfliteModelFilename != null) {
      throw new IllegalStateException("assetName and tfliteModelFilename are both non-null!");
    }

    TFObjectDetector.Parameters parameters = TfodBase.makeParameters(minimumConfidence, useObjectTracker, enableCameraMonitoring);
    parameters.isModelTensorFlow2 = this.isModelTensorFlow2;
    initializeWithParameters(vuforiaLocalizer, parameters);
  }

  /**
   * Initializes TensorFlow Object Detection.
   * For blocks, this is only used for custom models.
   */
  public void initializeWithIsModelTensorFlow2(VuforiaBase vuforiaBase, float minimumConfidence,
      boolean useObjectTracker, boolean enableCameraMonitoring, boolean isModelTensorFlow2) {
    if (assetName != null && tfliteModelFilename != null) {
      throw new IllegalStateException("assetName and tfliteModelFilename are both non-null!");
    }

    TFObjectDetector.Parameters parameters = TfodBase.makeParameters(minimumConfidence, useObjectTracker, enableCameraMonitoring);
    parameters.isModelTensorFlow2 = isModelTensorFlow2;
    initializeWithParameters(vuforiaBase.getVuforiaLocalizer(), parameters);
  }

  /**
   * Initializes TensorFlow Object Detection with all parameters.
   * For blocks, this is only used for custom models.
   */
  public void initializeWithAllArgs(VuforiaBase vuforiaBase,
      float minimumConfidence, boolean useObjectTracker, boolean enableCameraMonitoring,
      boolean isModelTensorFlow2, boolean isModelQuantized, int inputSize,
      int numInterpreterThreads, int numExecutorThreads,
      int maxNumDetections, int timingBufferSize, double maxFrameRate,
      float trackerMaxOverlap, float trackerMinSize,
      float trackerMarginalCorrelation, float trackerMinCorrelation) {
    TFObjectDetector.Parameters parameters = TfodBase.makeParameters(minimumConfidence, useObjectTracker, enableCameraMonitoring);
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
    initializeWithParameters(vuforiaBase.getVuforiaLocalizer(), parameters);
  }

  /**
   * Initializes TensorFlow Object Detection.
   */
  public void initializeWithParameters(VuforiaLocalizer vuforiaLocalizer, TFObjectDetector.Parameters parameters) {
    tfod = ClassFactory.getInstance().createTFObjectDetector(parameters, vuforiaLocalizer);

    if (assetName != null) {
      tfod.loadModelFromAsset(assetName, labels);
    } else {
      String filename;
      File file = new File(TFLITE_MODELS_DIR, tfliteModelFilename);
      if (file.exists()) {
        filename = file.getAbsolutePath();
      } else {
        filename = tfliteModelFilename;
      }
      tfod.loadModelFromFile(filename, labels);
    }
  }

  public static TFObjectDetector.Parameters makeParameters(float minimumConfidence, boolean useObjectTracker,
      boolean enableCameraMonitoring) {
    TFObjectDetector.Parameters parameters = new TFObjectDetector.Parameters();
    parameters.minimumConfidence = minimumConfidence;
    parameters.minResultConfidence = minimumConfidence;
    parameters.useObjectTracker = useObjectTracker;
    if (enableCameraMonitoring) {
      Context context = AppUtil.getInstance().getRootActivity();
      parameters.tfodMonitorViewIdParent = context.getResources().getIdentifier(
          "tfodMonitorViewId", "id", context.getPackageName());
    }
    return parameters;
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
