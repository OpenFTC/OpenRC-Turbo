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
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImpl;
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A class that provides JavaScript access to a {@link CRServo}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class CRServoAccess extends HardwareAccess<CRServo> {
  private final CRServo crServo;

  CRServoAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, CRServo.class);
    this.crServo = hardwareDevice;
  }

  // From com.qualcomm.robotcore.hardware.CRServo

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {CRServoImpl.class}, methodName = "setDirection")
  public void setDirection(String directionString) {
    startBlockExecution(BlockType.SETTER, ".Direction");
    Direction direction = checkArg(directionString, Direction.class, "");
    if (direction != null) {
      crServo.setDirection(direction);
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {CRServoImpl.class}, methodName = "getDirection")
  public String getDirection() {
    startBlockExecution(BlockType.GETTER, ".Direction");
    Direction direction = crServo.getDirection();
    if (direction != null) {
      return direction.toString();
    }
    return "";
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {CRServoImpl.class}, methodName = "setPower")
  public void setPower(double power) {
    startBlockExecution(BlockType.SETTER, ".Power");
    crServo.setPower(power);
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {CRServoImpl.class}, methodName = "getPower")
  public double getPower() {
    startBlockExecution(BlockType.GETTER, ".Power");
    return crServo.getPower();
  }
}
