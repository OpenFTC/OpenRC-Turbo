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
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.ServoController.PwmStatus;

/**
 * A class that provides JavaScript access to a {@link ServoController}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class ServoControllerAccess extends HardwareAccess<ServoController> {
  private final ServoController servoController;

  ServoControllerAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, ServoController.class);
    this.servoController = hardwareDevice;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public String getPwmStatus() {
    startBlockExecution(BlockType.GETTER, ".PwmStatus");
    PwmStatus pwmStatus = servoController.getPwmStatus();
    if (pwmStatus != null) {
      return pwmStatus.toString();
    }
    return "";
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void pwmEnable() {
    startBlockExecution(BlockType.FUNCTION, ".pwmEnable");
    servoController.pwmEnable();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void pwmDisable() {
    startBlockExecution(BlockType.FUNCTION, ".pwmDisable");
    servoController.pwmDisable();
  }
}
