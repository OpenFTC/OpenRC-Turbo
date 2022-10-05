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
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro.HeadingMode;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Gyroscope;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.OrientationSensor;
import java.util.Set;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Axis;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

/**
 * A class that provides JavaScript access to a {@link GyroSensor}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class GyroSensorAccess extends HardwareAccess<GyroSensor> {
  private final GyroSensor gyroSensor;

  GyroSensorAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, GyroSensor.class);
    this.gyroSensor = hardwareDevice;
  }

  // Properties

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {GyroSensor.class, ModernRoboticsI2cGyro.class}, methodName = "getHeading")
  public int getHeading() {
    try {
      startBlockExecution(BlockType.GETTER, ".Heading");
      return gyroSensor.getHeading();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {ModernRoboticsI2cGyro.class}, methodName = "setHeadingMode")
  public void setHeadingMode(String headingModeString) {
    try {
      startBlockExecution(BlockType.SETTER, ".HeadingMode");
      HeadingMode headingMode = checkArg(headingModeString, HeadingMode.class, "");
      if (headingMode != null) {
        if (gyroSensor instanceof ModernRoboticsI2cGyro) {
          ((ModernRoboticsI2cGyro) gyroSensor).setHeadingMode(headingMode);
        } else {
          reportWarning("This GyroSensor is not a ModernRoboticsI2cGyro.");
        }
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
  @Block(classes = {ModernRoboticsI2cGyro.class}, methodName = "getHeadingMode")
  public String getHeadingMode() {
    try {
      startBlockExecution(BlockType.GETTER, ".HeadingMode");
      if (gyroSensor instanceof ModernRoboticsI2cGyro) {
        HeadingMode headingMode = ((ModernRoboticsI2cGyro) gyroSensor).getHeadingMode();
        if (headingMode != null) {
          return headingMode.toString();
        }
      } else {
        reportWarning("This GyroSensor is not a ModernRoboticsI2cGyro.");
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
  @Block(classes = {ModernRoboticsI2cGyro.class}, methodName = "setI2cAddress")
  public void setI2cAddress7Bit(int i2cAddr7Bit) {
    try {
      startBlockExecution(BlockType.SETTER, ".I2cAddress7Bit");
      if (gyroSensor instanceof ModernRoboticsI2cGyro) {
        ((ModernRoboticsI2cGyro) gyroSensor).setI2cAddress(I2cAddr.create7bit(i2cAddr7Bit));
      } else {
        reportWarning("This GyroSensor is not a ModernRoboticsI2cGyro.");
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
  @Block(classes = {ModernRoboticsI2cGyro.class}, methodName = "getI2cAddress")
  public int getI2cAddress7Bit() {
    try {
      startBlockExecution(BlockType.GETTER, ".I2cAddress7Bit");
      if (gyroSensor instanceof ModernRoboticsI2cGyro) {
        I2cAddr i2cAddr = ((ModernRoboticsI2cGyro) gyroSensor).getI2cAddress();
        if (i2cAddr != null) {
          return i2cAddr.get7Bit();
        }
      } else {
        reportWarning("This GyroSensor is not a ModernRoboticsI2cGyro.");
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
  @Block(classes = {ModernRoboticsI2cGyro.class}, methodName = "setI2cAddress")
  public void setI2cAddress8Bit(int i2cAddr8Bit) {
    try {
      startBlockExecution(BlockType.SETTER, ".I2cAddress8Bit");
      if (gyroSensor instanceof ModernRoboticsI2cGyro) {
        ((ModernRoboticsI2cGyro) gyroSensor).setI2cAddress(I2cAddr.create8bit(i2cAddr8Bit));
      } else {
        reportWarning("This GyroSensor is not a ModernRoboticsI2cGyro.");
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
  @Block(classes = {ModernRoboticsI2cGyro.class}, methodName = "getI2cAddress")
  public int getI2cAddress8Bit() {
    try {
      startBlockExecution(BlockType.GETTER, ".I2cAddress8Bit");
      if (gyroSensor instanceof ModernRoboticsI2cGyro) {
        I2cAddr i2cAddr = ((ModernRoboticsI2cGyro) gyroSensor).getI2cAddress();
        if (i2cAddr != null) {
          return i2cAddr.get8Bit();
        }
      } else {
        reportWarning("This GyroSensor is not a ModernRoboticsI2cGyro.");
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
  @Block(classes = {ModernRoboticsI2cGyro.class}, methodName = "getIntegratedZValue")
  public int getIntegratedZValue() {
    try {
      startBlockExecution(BlockType.GETTER, ".IntegratedZValue");
      if (gyroSensor instanceof ModernRoboticsI2cGyro) {
        return ((ModernRoboticsI2cGyro) gyroSensor).getIntegratedZValue();
      } else {
        reportWarning("This GyroSensor is not a ModernRoboticsI2cGyro.");
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
  @Block(classes = {GyroSensor.class, ModernRoboticsI2cGyro.class}, methodName = "rawX")
  public int getRawX() {
    try {
      startBlockExecution(BlockType.GETTER, ".RawX");
      return gyroSensor.rawX();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {GyroSensor.class, ModernRoboticsI2cGyro.class}, methodName = "rawY")
  public int getRawY() {
    try {
      startBlockExecution(BlockType.GETTER, ".RawY");
      return gyroSensor.rawY();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {GyroSensor.class, ModernRoboticsI2cGyro.class}, methodName = "rawZ")
  public int getRawZ() {
    try {
      startBlockExecution(BlockType.GETTER, ".RawZ");
      return gyroSensor.rawZ();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {GyroSensor.class, ModernRoboticsI2cGyro.class}, methodName = "getRotationFraction")
  public double getRotationFraction() {
    try {
      startBlockExecution(BlockType.GETTER, ".RotationFraction");
      return gyroSensor.getRotationFraction();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  // Functions

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {GyroSensor.class, ModernRoboticsI2cGyro.class}, methodName = "calibrate")
  public void calibrate() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".calibrate");
      gyroSensor.calibrate();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {GyroSensor.class, ModernRoboticsI2cGyro.class}, methodName = "isCalibrating")
  public boolean isCalibrating() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isCalibrating");
      return gyroSensor.isCalibrating();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {GyroSensor.class, ModernRoboticsI2cGyro.class}, methodName = "resetZAxisIntegrator")
  public void resetZAxisIntegrator() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".resetZAxisIntegrator");
      gyroSensor.resetZAxisIntegrator();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gyroscope.class, ModernRoboticsI2cGyro.class}, methodName = "getAngularVelocityAxes")
  public String getAngularVelocityAxes() {
    try {
      startBlockExecution(BlockType.GETTER, ".AngularVelocityAxes");
      if (gyroSensor instanceof Gyroscope) {
        Set<Axis> axes = ((Gyroscope) gyroSensor).getAngularVelocityAxes();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        String delimiter = "";
        for (Axis axis : axes) {
          sb.append(delimiter).append("\"").append(axis.toString()).append("\"");
          delimiter = ",";
        }
        sb.append("]");
        return sb.toString();
      } else {
        reportWarning("This GyroSensor is not a Gyroscope.");
      }
      return "[]";
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gyroscope.class, ModernRoboticsI2cGyro.class}, methodName = "getAngularVelocity")
  public AngularVelocity getAngularVelocity(String angleUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getAngularVelocity");
      AngleUnit angleUnit = checkAngleUnit(angleUnitString);
      if (angleUnit != null) {
        if (gyroSensor instanceof Gyroscope) {
          return ((Gyroscope) gyroSensor).getAngularVelocity(angleUnit);
        } else {
          reportWarning("This GyroSensor is not a Gyroscope.");
          return new AngularVelocity(angleUnit, 0, 0, 0, 0L);
        }
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
  @Block(classes = {OrientationSensor.class, ModernRoboticsI2cGyro.class}, methodName = "getAngularOrientationAxes")
  public String getAngularOrientationAxes() {
    try {
      startBlockExecution(BlockType.GETTER, ".AngularOrientationAxes");
      if (gyroSensor instanceof OrientationSensor) {
        Set<Axis> axes = ((OrientationSensor) gyroSensor).getAngularOrientationAxes();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        String delimiter = "";
        for (Axis axis : axes) {
          sb.append(delimiter).append("\"").append(axis.toString()).append("\"");
          delimiter = ",";
        }
        sb.append("]");
        return sb.toString();
      } else {
        reportWarning("This GyroSensor is not a OrientationSensor.");
      }
      return "[]";
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {OrientationSensor.class, ModernRoboticsI2cGyro.class}, methodName = "getAngularOrientation")
  public Orientation getAngularOrientation(String axesReferenceString, String axesOrderString, String angleUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getAngularOrientation");
      AxesReference axesReference = checkAxesReference(axesReferenceString);
      AxesOrder axesOrder = checkAxesOrder(axesOrderString);
      AngleUnit angleUnit = checkAngleUnit(angleUnitString);
      if (axesReference != null && axesOrder != null && angleUnit != null) {
        if (gyroSensor instanceof OrientationSensor) {
          return ((OrientationSensor) gyroSensor).getAngularOrientation(axesReference, axesOrder, angleUnit);
        } else {
          reportWarning("This GyroSensor is not a OrientationSensor.");
          return new Orientation(axesReference, axesOrder, angleUnit, 0, 0, 0, 0L);
        }
      }
      return null;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }
}
