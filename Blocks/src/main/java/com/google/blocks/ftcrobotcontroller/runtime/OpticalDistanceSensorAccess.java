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
import com.qualcomm.hardware.modernrobotics.ModernRoboticsAnalogOpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

/**
 * A class that provides JavaScript access to a {@link OpticalDistanceSensor}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class OpticalDistanceSensorAccess extends HardwareAccess<OpticalDistanceSensor> {
  private final OpticalDistanceSensor opticalDistanceSensor;

  OpticalDistanceSensorAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, OpticalDistanceSensor.class);
    this.opticalDistanceSensor = hardwareDevice;
  }

  // from com.qualcomm.robotcore.hardware.LightSensor

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {ModernRoboticsAnalogOpticalDistanceSensor.class, OpticalDistanceSensor.class}, methodName = "getLightDetected")
  public double getLightDetected() {
    try {
      startBlockExecution(BlockType.GETTER, ".LightDetected");
      return opticalDistanceSensor.getLightDetected();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {ModernRoboticsAnalogOpticalDistanceSensor.class, OpticalDistanceSensor.class}, methodName = "getRawLightDetected")
  public double getRawLightDetected() {
    try {
      startBlockExecution(BlockType.GETTER, ".RawLightDetected");
      return opticalDistanceSensor.getRawLightDetected();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {ModernRoboticsAnalogOpticalDistanceSensor.class, OpticalDistanceSensor.class}, methodName = "getRawLightDetectedMax")
  public double getRawLightDetectedMax() {
    try {
      startBlockExecution(BlockType.GETTER, ".RawLightDetectedMax");
      return opticalDistanceSensor.getRawLightDetectedMax();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {ModernRoboticsAnalogOpticalDistanceSensor.class}, methodName = "enableLed")
  public void enableLed(boolean enable) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".enableLed");
      opticalDistanceSensor.enableLed(enable);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }
}
