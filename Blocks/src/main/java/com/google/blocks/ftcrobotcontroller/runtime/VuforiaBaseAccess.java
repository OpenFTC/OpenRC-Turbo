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

import static com.google.blocks.ftcrobotcontroller.hardware.HardwareUtil.SWITCHABLE_CAMERA_NAME;

import android.util.Pair;
import android.webkit.JavascriptInterface;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.Parameters.CameraMonitorFeedback;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaBase;

/**
 * An abstract class for classes that provides JavaScript access to a {@link VuforiaBase}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
abstract class VuforiaBaseAccess<T extends VuforiaBase> extends Access {
  private final HardwareMap hardwareMap;
  private T vuforiaBase;

  VuforiaBaseAccess(BlocksOpMode blocksOpMode, String identifier, HardwareMap hardwareMap,
      String blockFirstName) {
    super(blocksOpMode, identifier, blockFirstName);
    this.hardwareMap = hardwareMap;
  }

  private boolean checkAndSetVuforiaBase() {
    if (vuforiaBase != null) {
      reportWarning(blockFirstName + ".initialize has already been called!");
      return false;
    }
    vuforiaBase = createVuforia();
    return true;
  }

  protected abstract T createVuforia();

  T getVuforiaBase() {
    if (vuforiaBase == null) {
      reportWarning("You forgot to call " + blockFirstName + ".initialize!");
      return null;
    }
    return vuforiaBase;
  }

  // Access methods

  @Override
  void close() {
    if (vuforiaBase != null) {
      vuforiaBase.close();
      vuforiaBase = null;
    }
  }

  // Javascript methods

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void initialize_withCameraDirection(String vuforiaLicenseKey, String cameraDirectionString,
      boolean useExtendedTracking, boolean enableCameraMonitoring, String cameraMonitorFeedbackString,
      float dx, float dy, float dz, float xAngle, float yAngle, float zAngle,
      boolean useCompetitionFieldTargetLocations) {
    initialize_withCameraDirection_2(cameraDirectionString,
        useExtendedTracking, enableCameraMonitoring, cameraMonitorFeedbackString,
        dx, dy, dz, "XYZ", xAngle, yAngle, zAngle,
        useCompetitionFieldTargetLocations);
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void initialize_withCameraDirection_2(String cameraDirectionString,
      boolean useExtendedTracking, boolean enableCameraMonitoring, String cameraMonitorFeedbackString,
      float dx, float dy, float dz,
      String axesOrderString, float firstAngle, float secondAngle, float thirdAngle,
      boolean useCompetitionFieldTargetLocations) {
    startBlockExecution(BlockType.FUNCTION, ".initialize");
    CameraDirection cameraDirection = checkVuforiaLocalizerCameraDirection(cameraDirectionString);
    AxesOrder axesOrder = checkAxesOrder(axesOrderString);
    Pair<Boolean, CameraMonitorFeedback> cameraMonitorFeedback =
        checkCameraMonitorFeedback(cameraMonitorFeedbackString);
    if (cameraDirection != null && axesOrder != null && cameraMonitorFeedback.first && checkAndSetVuforiaBase()) {
      String vuforiaLicenseKey = "";
      try {
        vuforiaBase.initialize(vuforiaLicenseKey, cameraDirection,
            useExtendedTracking, enableCameraMonitoring, cameraMonitorFeedback.second,
            dx, dy, dz, axesOrder, firstAngle, secondAngle, thirdAngle, useCompetitionFieldTargetLocations);
      } catch (Exception e) {
        blocksOpMode.throwException(e);
      }
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void initialize_withWebcam(String cameraNameString, String webcamCalibrationFilename,
      boolean useExtendedTracking, boolean enableCameraMonitoring, String cameraMonitorFeedbackString,
      float dx, float dy, float dz, float xAngle, float yAngle, float zAngle,
      boolean useCompetitionFieldTargetLocations) {
    initialize_withWebcam_2(cameraNameString, webcamCalibrationFilename,
        useExtendedTracking, enableCameraMonitoring, cameraMonitorFeedbackString,
        dx, dy, dz, "XYZ", xAngle, yAngle, zAngle,
        useCompetitionFieldTargetLocations);
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void initialize_withWebcam_2(String cameraNameString, String webcamCalibrationFilename,
      boolean useExtendedTracking, boolean enableCameraMonitoring, String cameraMonitorFeedbackString,
      float dx, float dy, float dz,
      String axesOrderString, float firstAngle, float secondAngle, float thirdAngle,
      boolean useCompetitionFieldTargetLocations) {
    startBlockExecution(BlockType.FUNCTION, ".initialize");
    CameraName cameraName;
    if (cameraNameString.equals(SWITCHABLE_CAMERA_NAME)) {
      cameraName = blocksOpMode.getSwitchableCamera();
    } else {
      cameraName = checkCameraNameFromString(hardwareMap, cameraNameString);
    }
    AxesOrder axesOrder = checkAxesOrder(axesOrderString);
    Pair<Boolean, CameraMonitorFeedback> cameraMonitorFeedback =
        checkCameraMonitorFeedback(cameraMonitorFeedbackString);
    if (cameraName != null && axesOrder != null && cameraMonitorFeedback.first && checkAndSetVuforiaBase()) {
      String vuforiaLicenseKey = "";
      try {
        vuforiaBase.initialize(vuforiaLicenseKey, cameraName, webcamCalibrationFilename,
            useExtendedTracking, enableCameraMonitoring, cameraMonitorFeedback.second,
            dx, dy, dz, axesOrder, firstAngle, secondAngle, thirdAngle, useCompetitionFieldTargetLocations);
      } catch (Exception e) {
        blocksOpMode.throwException(e);
      }
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void activate() {
    startBlockExecution(BlockType.FUNCTION, ".activate");
    try {
      vuforiaBase.activate();
    } catch (IllegalStateException e) {
      reportWarning(e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void deactivate() {
    startBlockExecution(BlockType.FUNCTION, ".deactivate");
    try {
      vuforiaBase.deactivate();
    } catch (IllegalStateException e) {
      reportWarning(e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setActiveCamera(String cameraNameString) {
    startBlockExecution(BlockType.FUNCTION, ".deactivate");
    CameraName cameraName = checkCameraNameFromString(hardwareMap, cameraNameString);
    if (cameraName != null) {
      try {
        vuforiaBase.setActiveCamera(cameraName);
      } catch (IllegalStateException e) {
        reportWarning(e.getMessage());
      }
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public String track(String name) {
    startBlockExecution(BlockType.FUNCTION, ".track");
    try {
      return vuforiaBase.track(name).toJson();
    } catch (IllegalStateException e) {
      reportWarning(e.getMessage());
    } catch (IllegalArgumentException e) {
      reportInvalidArg("name", vuforiaBase.printTrackableNames());
    }
    return vuforiaBase.emptyTrackingResults(name).toJson();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public String trackPose(String name) {
    startBlockExecution(BlockType.FUNCTION, ".trackPose");
    try {
      return vuforiaBase.trackPose(name).toJson();
    } catch (IllegalStateException e) {
      reportWarning(e.getMessage());
    } catch (IllegalArgumentException e) {
      reportInvalidArg("name", vuforiaBase.printTrackableNames());
    }
    return vuforiaBase.emptyTrackingResults(name).toJson();
  }
}
