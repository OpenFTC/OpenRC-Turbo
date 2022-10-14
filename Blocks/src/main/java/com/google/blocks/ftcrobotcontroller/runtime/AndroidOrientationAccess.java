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
import org.firstinspires.ftc.robotcore.external.android.AndroidOrientation;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * A class that provides JavaScript access to the Android sensors for Orientation.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class AndroidOrientationAccess extends Access {
  private final AndroidOrientation androidOrientation;

  AndroidOrientationAccess(BlocksOpMode blocksOpMode, String identifier) {
    super(blocksOpMode, identifier, "AndroidOrientation");
    androidOrientation = new AndroidOrientation();
  }

  // Access methods

  @Override
  void close() {
    androidOrientation.stopListening();
  }

  // Javascript methods

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setAngleUnit(String angleUnitString) {
    try {
      startBlockExecution(BlockType.SETTER, ".AngleUnit");
      AngleUnit angleUnit = checkArg(angleUnitString, AngleUnit.class, "");
      if (angleUnit != null) {
        androidOrientation.setAngleUnit(angleUnit);
      }
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getAzimuth() {
    try {
      startBlockExecution(BlockType.GETTER, ".Azimuth");
      return androidOrientation.getAzimuth();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getPitch() {
    try {
      startBlockExecution(BlockType.GETTER, ".Pitch");
      return androidOrientation.getPitch();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getRoll() {
    try {
      startBlockExecution(BlockType.GETTER, ".Roll");
      return androidOrientation.getRoll();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getAngle() {
    try {
      startBlockExecution(BlockType.GETTER, ".Angle");
      return androidOrientation.getAngle();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getMagnitude() {
    try {
      startBlockExecution(BlockType.GETTER, ".Magnitude");
      return androidOrientation.getMagnitude();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public String getAngleUnit() {
    try {
      startBlockExecution(BlockType.GETTER, ".AngleUnit");
      return androidOrientation.getAngleUnit().toString();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public boolean isAvailable() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isAvailable");
      return androidOrientation.isAvailable();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void startListening() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".startListening");
      androidOrientation.startListening();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void stopListening() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".stopListening");
      androidOrientation.stopListening();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }
}
