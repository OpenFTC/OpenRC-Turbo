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
import com.qualcomm.hardware.hitechnic.HiTechnicNxtUltrasonicSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

/**
 * A class that provides JavaScript access to an {@link UltrasonicSensor}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class UltrasonicSensorAccess extends HardwareAccess<UltrasonicSensor> {
  private final UltrasonicSensor ultrasonicSensor;

  UltrasonicSensorAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, UltrasonicSensor.class);
    this.ultrasonicSensor = hardwareDevice;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {HiTechnicNxtUltrasonicSensor.class}, methodName = "getUltrasonicLevel")
  public double getUltrasonicLevel() {
    startBlockExecution(BlockType.GETTER, ".UltrasonicLevel");
    return ultrasonicSensor.getUltrasonicLevel();
  }
}
