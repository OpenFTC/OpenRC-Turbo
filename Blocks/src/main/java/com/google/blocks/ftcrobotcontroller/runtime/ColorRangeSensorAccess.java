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
import com.qualcomm.hardware.lynx.LynxI2cColorRangeSensor;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * A class that provides JavaScript access to a {@link LynxI2cColorRangeSensor} or a
 * {@link RevColorSensorV3}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class ColorRangeSensorAccess extends HardwareAccess<ColorRangeSensor> {
  private final ColorRangeSensor colorRangeSensor;

  ColorRangeSensorAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, ColorRangeSensor.class);
    this.colorRangeSensor = hardwareDevice;
  }

  // Properties

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "red")
  public int getRed() {
    startBlockExecution(BlockType.GETTER, ".Red");
    return colorRangeSensor.red();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "green")
  public int getGreen() {
    startBlockExecution(BlockType.GETTER, ".Green");
    return colorRangeSensor.green();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "blue")
  public int getBlue() {
    startBlockExecution(BlockType.GETTER, ".Blue");
    return colorRangeSensor.blue();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "alpha")
  public int getAlpha() {
    startBlockExecution(BlockType.GETTER, ".Alpha");
    return colorRangeSensor.alpha();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "argb")
  public int getArgb() {
    startBlockExecution(BlockType.GETTER, ".Argb");
    return colorRangeSensor.argb();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "setI2cAddress")
  public void setI2cAddress7Bit(int i2cAddr7Bit) {
    startBlockExecution(BlockType.SETTER, ".I2cAddress7Bit");
    colorRangeSensor.setI2cAddress(I2cAddr.create7bit(i2cAddr7Bit));
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "getI2cAddress")
  public int getI2cAddress7Bit() {
    startBlockExecution(BlockType.GETTER, ".I2cAddress7Bit");
    I2cAddr i2cAddr = colorRangeSensor.getI2cAddress();
    if (i2cAddr != null) {
      return i2cAddr.get7Bit();
    }
    return 0;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "setI2cAddress")
  public void setI2cAddress8Bit(int i2cAddr8Bit) {
    startBlockExecution(BlockType.SETTER, ".I2cAddress8Bit");
    colorRangeSensor.setI2cAddress(I2cAddr.create8bit(i2cAddr8Bit));
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "getI2cAddress")
  public int getI2cAddress8Bit() {
    startBlockExecution(BlockType.GETTER, ".I2cAddress8Bit");
    I2cAddr i2cAddr = colorRangeSensor.getI2cAddress();
    if (i2cAddr != null) {
      return i2cAddr.get8Bit();
    }
    return 0;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "getLightDetected")
  public double getLightDetected() {
    startBlockExecution(BlockType.GETTER, ".LightDetected");
    return colorRangeSensor.getLightDetected();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "getRawLightDetected")
  public double getRawLightDetected() {
    startBlockExecution(BlockType.GETTER, ".RawLightDetected");
    return colorRangeSensor.getRawLightDetected();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "getRawLightDetectedMax")
  public double getRawLightDetectedMax() {
    startBlockExecution(BlockType.GETTER, ".RawLightDetectedMax");
    return colorRangeSensor.getRawLightDetectedMax();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "getDistance")
  public double getDistance(String distanceUnitString) {
    startBlockExecution(BlockType.FUNCTION, ".getDistance");
    DistanceUnit distanceUnit = checkArg(distanceUnitString, DistanceUnit.class, "unit");
    if (distanceUnit != null) {
      return colorRangeSensor.getDistance(distanceUnit);
    }
    return 0.0;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LynxI2cColorRangeSensor.class, RevColorSensorV3.class}, methodName = "getNormalizedColors")
  public String getNormalizedColors() {
    startBlockExecution(BlockType.FUNCTION, ".getNormalizedColors");
    NormalizedRGBA color = colorRangeSensor.getNormalizedColors();
    return "{ \"Red\":" + color.red +
        ", \"Green\":" + color.green +
        ", \"Blue\":" + color.blue +
        ", \"Alpha\":" + color.alpha +
        ", \"Color\":" + color.toColor() + " }";
  }
}
