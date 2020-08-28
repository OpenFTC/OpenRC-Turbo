/*
 * Copyright 2016 Google LLC
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
import com.google.blocks.ftcrobotcontroller.hardware.HardwareItem;
import com.qualcomm.hardware.hitechnic.HiTechnicNxtCompassSensor;
import com.qualcomm.robotcore.hardware.CompassSensor;
import com.qualcomm.robotcore.hardware.CompassSensor.CompassMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A class that provides JavaScript access to a {@link CompassSensor}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class CompassSensorAccess extends HardwareAccess<CompassSensor> {
  private final CompassSensor compassSensor;

  CompassSensorAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, CompassSensor.class);
    this.compassSensor = hardwareDevice;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {HiTechnicNxtCompassSensor.class}, methodName = "getDirection")
  public double getDirection() {
    startBlockExecution(BlockType.GETTER, ".Direction");
    return compassSensor.getDirection();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {HiTechnicNxtCompassSensor.class}, methodName = "calibrationFailed")
  public boolean getCalibrationFailed() {
    startBlockExecution(BlockType.GETTER, ".CalibrationFailed");
    return compassSensor.calibrationFailed();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {HiTechnicNxtCompassSensor.class}, methodName = "setMode")
  public void setMode(String compassModeString) {
    startBlockExecution(BlockType.FUNCTION, ".Mode");
    CompassMode compassMode = checkArg(compassModeString, CompassMode.class, "compassMode");
    if (compassMode != null) {
      compassSensor.setMode(compassMode);
    }
  }
}
