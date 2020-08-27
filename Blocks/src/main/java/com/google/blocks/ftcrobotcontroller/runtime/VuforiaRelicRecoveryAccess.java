/*
 * Copyright 2017 Google LLC
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
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaRelicRecovery;

/**
 * A class that provides JavaScript access to Vuforia for Relic Recovery (2017-2018).
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class VuforiaRelicRecoveryAccess extends VuforiaBaseAccess<VuforiaRelicRecovery> {
  VuforiaRelicRecoveryAccess(BlocksOpMode blocksOpMode, String identifier, HardwareMap hardwareMap) {
    super(blocksOpMode, identifier, hardwareMap);
  }

  protected VuforiaRelicRecovery createVuforia() {
    return new VuforiaRelicRecovery();
  }

  // We no longer generate javascript code to call this method, but it remains for backwards
  // compatibility.
  @SuppressWarnings("unused")
  @JavascriptInterface
  public void initialize(String vuforiaLicenseKey,
      String cameraDirectionString, boolean enableCameraMonitoring, String cameraMonitorFeedbackString,
      float dx, float dy, float dz, float xAngle, float yAngle, float zAngle) {
    initialize_withCameraDirection(vuforiaLicenseKey, cameraDirectionString, true /* useExtendedTracking */,
        enableCameraMonitoring, cameraMonitorFeedbackString, dx, dy, dz, xAngle, yAngle, zAngle,
        true /* useCompetitionFieldTargetLocations */);
  }

  // We no longer generate javascript code to call this method, but it remains for backwards
  // compatibility.
  @SuppressWarnings("unused")
  @JavascriptInterface
  public void initializeExtended(String vuforiaLicenseKey, String cameraDirectionString,
      boolean useExtendedTracking, boolean enableCameraMonitoring, String cameraMonitorFeedbackString,
      float dx, float dy, float dz, float xAngle, float yAngle, float zAngle,
      boolean useCompetitionFieldTargetLocations) {
    initialize_withCameraDirection(vuforiaLicenseKey, cameraDirectionString,
        useExtendedTracking, enableCameraMonitoring, cameraMonitorFeedbackString,
        dx, dy, dz, xAngle, yAngle, zAngle,
        useCompetitionFieldTargetLocations);
  }
}
