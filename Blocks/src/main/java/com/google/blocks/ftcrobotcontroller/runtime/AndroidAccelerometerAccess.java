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
import org.firstinspires.ftc.robotcore.external.android.AndroidAccelerometer;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * A class that provides JavaScript access to the Android Accelerometer.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class AndroidAccelerometerAccess extends Access {
  private final AndroidAccelerometer androidAccelerometer;

  AndroidAccelerometerAccess(BlocksOpMode blocksOpMode, String identifier) {
    super(blocksOpMode, identifier, "AndroidAccelerometer");
    androidAccelerometer = new AndroidAccelerometer();
  }

  // Access methods

  @Override
  void close() {
    androidAccelerometer.stopListening();
  }

  // Javascript methods

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setDistanceUnit(String distanceUnitString) {
    startBlockExecution(BlockType.SETTER, ".DistanceUnit");
    DistanceUnit distanceUnit = checkArg(distanceUnitString, DistanceUnit.class, "");
    if (distanceUnit != null) {
      androidAccelerometer.setDistanceUnit(distanceUnit);
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getX() {
    startBlockExecution(BlockType.GETTER, ".X");
    return androidAccelerometer.getX();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getY() {
    startBlockExecution(BlockType.GETTER, ".Y");
    return androidAccelerometer.getY();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getZ() {
    startBlockExecution(BlockType.GETTER, ".Z");
    return androidAccelerometer.getZ();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public Acceleration getAcceleration() {
    startBlockExecution(BlockType.GETTER, ".Acceleration");
    return androidAccelerometer.getAcceleration();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public String getDistanceUnit() {
    startBlockExecution(BlockType.GETTER, ".DistanceUnit");
    return androidAccelerometer.getDistanceUnit().toString();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public boolean isAvailable() {
    return androidAccelerometer.isAvailable();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void startListening() {
    startBlockExecution(BlockType.FUNCTION, ".startListening");
    androidAccelerometer.startListening();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void stopListening() {
    startBlockExecution(BlockType.FUNCTION, ".stopListening");
    androidAccelerometer.stopListening();
  }
}
