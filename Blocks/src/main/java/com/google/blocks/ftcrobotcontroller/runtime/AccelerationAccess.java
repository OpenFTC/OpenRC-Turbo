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
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * A class that provides JavaScript access to {@link Acceleration}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class AccelerationAccess extends Access {
  AccelerationAccess(BlocksOpMode blocksOpMode, String identifier) {
    super(blocksOpMode, identifier, "Acceleration");
  }

  private Acceleration checkAcceleration(Object accelerationArg) {
    return checkArg(accelerationArg, Acceleration.class, "acceleration");
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Acceleration.class, fieldName = "unit")
  public String getDistanceUnit(Object accelerationArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".DistanceUnit");
      Acceleration acceleration = checkAcceleration(accelerationArg);
      if (acceleration != null) {
        DistanceUnit distanceUnit = acceleration.unit;
        if (distanceUnit != null) {
          return distanceUnit.toString();
        }
      }
      return "";
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Acceleration.class, fieldName = "xAccel")
  public double getXAccel(Object accelerationArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".XAccel");
      Acceleration acceleration = checkAcceleration(accelerationArg);
      if (acceleration != null) {
        return acceleration.xAccel;
      }
      return 0;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Acceleration.class, fieldName = "yAccel")
  public double getYAccel(Object accelerationArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".YAccel");
      Acceleration acceleration = checkAcceleration(accelerationArg);
      if (acceleration != null) {
        return acceleration.yAccel;
      }
      return 0;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Acceleration.class, fieldName = "zAccel")
  public double getZAccel(Object accelerationArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".ZAccel");
      Acceleration acceleration = checkAcceleration(accelerationArg);
      if (acceleration != null) {
        return acceleration.zAccel;
      }
      return 0;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Acceleration.class, fieldName = "acquisitionTime")
  public long getAcquisitionTime(Object accelerationArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".AcquisitionTime");
      Acceleration acceleration = checkAcceleration(accelerationArg);
      if (acceleration != null) {
        return acceleration.acquisitionTime;
      }
      return 0;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Acceleration.class, constructor = true)
  public Acceleration create() {
    try {
      startBlockExecution(BlockType.CREATE, "");
      return new Acceleration();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Acceleration.class, constructor = true)
  public Acceleration create_withArgs(
      String distanceUnitString, double xAccel, double yAccel, double zAccel, long acquisitionTime) {
    try {
      startBlockExecution(BlockType.CREATE, "");
      DistanceUnit distanceUnit = checkArg(distanceUnitString, DistanceUnit.class, "distanceUnit");
      if (distanceUnit != null) {
        return new Acceleration(distanceUnit, xAccel, yAccel, zAccel, acquisitionTime);
      }
      return null;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Acceleration.class, methodName = "fromGravity")
  public Acceleration fromGravity(double gx, double gy, double gz, long acquisitionTime) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".fromGravity");
      return Acceleration.fromGravity(gx, gy, gz, acquisitionTime);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Acceleration.class, methodName = "toUnit")
  public Acceleration toDistanceUnit(Object accelerationArg, String distanceUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".toDistanceUnit");
      Acceleration acceleration = checkAcceleration(accelerationArg);
      DistanceUnit distanceUnit = checkArg(distanceUnitString, DistanceUnit.class, "distanceUnit");
      if (acceleration != null && distanceUnit != null) {
        return acceleration.toUnit(distanceUnit);
      }
      return null;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Acceleration.class, methodName = "toString")
  public String toText(Object accelerationArg) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".toText");
      Acceleration acceleration = checkAcceleration(accelerationArg);
      if (acceleration != null) {
        return acceleration.toString();
      }
      return "";
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }
}
