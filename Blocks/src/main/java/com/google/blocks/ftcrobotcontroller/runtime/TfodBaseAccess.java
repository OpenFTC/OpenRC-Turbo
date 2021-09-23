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

package com.google.blocks.ftcrobotcontroller.runtime;

import android.webkit.JavascriptInterface;
import com.qualcomm.robotcore.hardware.HardwareMap;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaBase;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TfodBase;

/**
 * An abstract class for classes that provides JavaScript access to a {@link TfodBase}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
abstract class TfodBaseAccess<T extends TfodBase> extends Access {
  private final HardwareMap hardwareMap;
  private T tfodBase;

  TfodBaseAccess(BlocksOpMode blocksOpMode, String identifier, HardwareMap hardwareMap, String blockFirstName) {
    super(blocksOpMode, identifier, blockFirstName);
    this.hardwareMap = hardwareMap;
  }

  private boolean checkAndSetTfodBase() {
    if (tfodBase != null) {
      reportWarning("Tfod.initialize has already been called!");
      return false;
    }
    tfodBase = createTfod();
    return true;
  }

  protected abstract T createTfod();

  // Access methods

  @Override
  void close() {
    if (tfodBase != null) {
      tfodBase.close();
      tfodBase = null;
    }
  }

  // Javascript methods

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void initialize(VuforiaBaseAccess vuforiaBaseAccess, float minimumConfidence,
      boolean useObjectTracker, boolean enableCameraMonitoring) {
    startBlockExecution(BlockType.FUNCTION, ".initialize");
    VuforiaLocalizer vuforiaLocalizer = vuforiaBaseAccess.getVuforiaBase().getVuforiaLocalizer();
    if (checkAndSetTfodBase() && vuforiaLocalizer != null) {
      try {
        tfodBase.initialize(vuforiaLocalizer, minimumConfidence, useObjectTracker, enableCameraMonitoring);
      } catch (IllegalStateException e) {
        reportWarning(e.getMessage());
      }
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void initializeWithIsModelTensorFlow2(VuforiaBaseAccess vuforiaBaseAccess, float minimumConfidence,
      boolean useObjectTracker, boolean enableCameraMonitoring, boolean isModelTensorFlow2) {
    startBlockExecution(BlockType.FUNCTION, ".initialize");
    VuforiaLocalizer vuforiaLocalizer = vuforiaBaseAccess.getVuforiaBase().getVuforiaLocalizer();
    if (checkAndSetTfodBase() && vuforiaLocalizer != null) {
      try {
        tfodBase.initializeWithIsModelTensorFlow2(vuforiaBaseAccess.getVuforiaBase(),
            minimumConfidence, useObjectTracker, enableCameraMonitoring, isModelTensorFlow2);
      } catch (IllegalStateException e) {
        reportWarning(e.getMessage());
      }
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void initializeWithAllArgs(VuforiaBaseAccess vuforiaBaseAccess,
      float minimumConfidence, boolean useObjectTracker, boolean enableCameraMonitoring,
      boolean isModelTensorFlow2, boolean isModelQuantized, int inputSize,
      int numInterpreterThreads, int numExecutorThreads,
      int maxNumDetections, int timingBufferSize, double maxFrameRate,
      float trackerMaxOverlap, float trackerMinSize,
      float trackerMarginalCorrelation, float trackerMinCorrelation) {
    startBlockExecution(BlockType.FUNCTION, ".initialize");
    VuforiaLocalizer vuforiaLocalizer = vuforiaBaseAccess.getVuforiaBase().getVuforiaLocalizer();
    if (checkAndSetTfodBase()&& vuforiaLocalizer != null) {
      try {
        tfodBase.initializeWithAllArgs(vuforiaBaseAccess.getVuforiaBase(),
            minimumConfidence, useObjectTracker, enableCameraMonitoring,
            isModelTensorFlow2, isModelQuantized, inputSize,
            numInterpreterThreads, numExecutorThreads,
            maxNumDetections, timingBufferSize, maxFrameRate,
            trackerMaxOverlap, trackerMinSize,
            trackerMarginalCorrelation, trackerMinCorrelation);
      } catch (IllegalStateException e) {
        reportWarning(e.getMessage());
      }
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void activate() {
    startBlockExecution(BlockType.FUNCTION, ".activate");
    if (tfodBase == null) {
      reportWarning("You forgot to call " + blockFirstName + ".initialize!");
      return;
    }
    try {
      tfodBase.activate();
    } catch (IllegalStateException e) {
      reportWarning(e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void deactivate() {
    startBlockExecution(BlockType.FUNCTION, ".deactivate");
    if (tfodBase == null) {
      reportWarning("You forgot to call " + blockFirstName + ".initialize!");
      return;
    }
    try {
      tfodBase.deactivate();
    } catch (IllegalStateException e) {
      reportWarning(e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setClippingMargins(int left, int top, int right, int bottom) {
    startBlockExecution(BlockType.FUNCTION, ".setClippingMargins");
    if (tfodBase == null) {
      reportWarning("You forgot to call " + blockFirstName + ".initialize!");
      return;
    }
    try {
      tfodBase.setClippingMargins(left, top, right, bottom);
    } catch (IllegalStateException e) {
      reportWarning(e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setZoom(double magnification, double aspectRatio) {
    startBlockExecution(BlockType.FUNCTION, ".setZoom");
    if (tfodBase == null) {
      reportWarning("You forgot to call " + blockFirstName + ".initialize!");
      return;
    }
    try {
      tfodBase.setZoom(magnification, aspectRatio);
    } catch (IllegalStateException e) {
      reportWarning(e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public String getRecognitions() {
    startBlockExecution(BlockType.FUNCTION, ".getRecognitions");
    if (tfodBase == null) {
      reportWarning("You forgot to call " + blockFirstName + ".initialize!");
      return "[]";
    }
    try {
      return toJson(tfodBase.getRecognitions());
    } catch (IllegalStateException e) {
      reportWarning(e.getMessage());
    }
    return "[]";
  }

  private static String toJson(List<Recognition> recognitions) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    String delimiter = "";
    for (Recognition recognition : recognitions) {
      sb.append(delimiter).append(toJson(recognition));
      delimiter = ",";
    }
    sb.append("]");
    return sb.toString();
  }

  private static String toJson(Recognition recognition) {
    return "{ \"Label\":\"" + recognition.getLabel() + "\"" +
        ", \"Confidence\":" + recognition.getConfidence() +
        ", \"Left\":" + recognition.getLeft() +
        ", \"Right\":" + recognition.getRight() +
        ", \"Top\":" + recognition.getTop() +
        ", \"Bottom\":" + recognition.getBottom() +
        ", \"Width\":" + recognition.getWidth() +
        ", \"Height\":" + recognition.getHeight() +
        ", \"ImageWidth\":" + recognition.getImageWidth() +
        ", \"ImageHeight\":" + recognition.getImageHeight() +
        ", \"estimateAngleToObject\":" + recognition.estimateAngleToObject(AngleUnit.RADIANS) +
        " }";
  }
}
