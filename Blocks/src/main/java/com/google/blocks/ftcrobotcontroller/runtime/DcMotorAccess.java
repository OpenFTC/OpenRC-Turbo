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
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction;
import com.qualcomm.robotcore.hardware.DcMotor.RunMode;
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

/**
 * A class that provides JavaScript access to a {@link DcMotor}.
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author austinshalit@gmail.com (Austin Shalit)
 */
class DcMotorAccess extends HardwareAccess<DcMotor> {
  private final DcMotor dcMotor;

  DcMotorAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, DcMotor.class);
    this.dcMotor = hardwareDevice;
  }

  // From com.qualcomm.robotcore.hardware.DcMotorSimple

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "setDirection")
  public void setDirection(String directionString) {
    try {
      startBlockExecution(BlockType.SETTER, ".Direction");
      Direction direction = checkArg(directionString, Direction.class, "");
      if (direction != null) {
        dcMotor.setDirection(direction);
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
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "getDirection")
  public String getDirection() {
    try {
      startBlockExecution(BlockType.GETTER, ".Direction");
      Direction direction = dcMotor.getDirection();
      if (direction != null) {
        return direction.toString();
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
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "setPower")
  public void setPower(double power) {
    try {
      startBlockExecution(BlockType.SETTER, ".Power");
      dcMotor.setPower(power);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "getPower")
  public double getPower() {
    try {
      startBlockExecution(BlockType.GETTER, ".Power");
      return dcMotor.getPower();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  // From com.qualcomm.robotcore.hardware.DcMotor

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Deprecated
  public void setMaxSpeed(double maxSpeed) {
    try {
      startBlockExecution(BlockType.SETTER, ".MaxSpeed");
      // This method does nothing. MaxSpeed is deprecated.
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Deprecated
  public int getMaxSpeed() {
    try {
      startBlockExecution(BlockType.GETTER, ".MaxSpeed");
      // This method always returns 0. MaxSpeed is deprecated.
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
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "setZeroPowerBehavior")
  public void setZeroPowerBehavior(String zeroPowerBehaviorString) {
    try {
      startBlockExecution(BlockType.SETTER, ".ZeroPowerBehavior");
      ZeroPowerBehavior zeroPowerBehavior = checkArg(zeroPowerBehaviorString, ZeroPowerBehavior.class, "");
      if (zeroPowerBehavior != null) {
        dcMotor.setZeroPowerBehavior(zeroPowerBehavior);
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
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "getZeroPowerBehavior")
  public String getZeroPowerBehavior() {
    try {
      startBlockExecution(BlockType.GETTER, ".ZeroPowerBehavior");
      ZeroPowerBehavior zeroPowerBehavior = dcMotor.getZeroPowerBehavior();
      if (zeroPowerBehavior != null) {
        return zeroPowerBehavior.toString();
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
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "getPowerFloat")
  public boolean getPowerFloat() {
    try {
      startBlockExecution(BlockType.GETTER, ".PowerFloat");
      return dcMotor.getPowerFloat();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "setTargetPosition")
  public void setTargetPosition(double position) {
    try {
      startBlockExecution(BlockType.SETTER, ".TargetPosition");
      dcMotor.setTargetPosition((int) position);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "getTargetPosition")
  public int getTargetPosition() {
    try {
      startBlockExecution(BlockType.GETTER, ".TargetPosition");
      return dcMotor.getTargetPosition();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "isBusy")
  public boolean isBusy() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isBusy");
      return dcMotor.isBusy();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "getCurrentPosition")
  public int getCurrentPosition() {
    try {
      startBlockExecution(BlockType.GETTER, ".CurrentPosition");
      return dcMotor.getCurrentPosition();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "setMode")
  public void setMode(String runModeString) {
    try {
      startBlockExecution(BlockType.SETTER, ".Mode");
      RunMode runMode = checkArg(runModeString, RunMode.class, "");
      if (runMode != null) {
        dcMotor.setMode(runMode);
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
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "getMode")
  public String getMode() {
    try {
      startBlockExecution(BlockType.GETTER, ".Mode");
      RunMode runMode = dcMotor.getMode();
      if (runMode != null) {
        return runMode.toString();
      }
      return "";
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  // Dual set property

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Deprecated
  public void setDualMaxSpeed(double maxSpeed1, Object otherArg, double maxSpeed2) {
    try {
      startBlockExecution(BlockType.SETTER, ".MaxSpeed");
      // This method does nothing. MaxSpeed is deprecated.
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "setMode")
  public void setDualMode(String runMode1String, Object otherArg, String runMode2String) {
    try {
      startBlockExecution(BlockType.SETTER, ".Mode");
      RunMode runMode1 = checkArg(runMode1String, RunMode.class, "first");
      RunMode runMode2 = checkArg(runMode2String, RunMode.class, "second");
      if (runMode1 != null && runMode2 != null &&
          otherArg instanceof DcMotorAccess) {
        DcMotorAccess other = (DcMotorAccess) otherArg;
        dcMotor.setMode(runMode1);
        other.dcMotor.setMode(runMode2);
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
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "setPower")
  public void setDualPower(double power1, Object otherArg, double power2) {
    try {
      startBlockExecution(BlockType.SETTER, ".Power");
      if (otherArg instanceof DcMotorAccess) {
        DcMotorAccess other = (DcMotorAccess) otherArg;
        dcMotor.setPower(power1);
        other.dcMotor.setPower(power2);
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
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "setTargetPosition")
  public void setDualTargetPosition(double position1, Object otherArg, double position2) {
    try {
      startBlockExecution(BlockType.SETTER, ".TargetPosition");
      if (otherArg instanceof DcMotorAccess) {
        DcMotorAccess other = (DcMotorAccess) otherArg;
        dcMotor.setTargetPosition((int) position1);
        other.dcMotor.setTargetPosition((int) position2);
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
  @Block(classes = {DcMotorEx.class}, methodName = "setTargetPositionTolerance")
  public void setDualTargetPositionTolerance(double tolerance1, Object otherArg, double tolerance2) {
    try {
      startBlockExecution(BlockType.SETTER, ".TargetPositionTolerance");
      if (otherArg instanceof DcMotorAccess) {
        DcMotorAccess other = (DcMotorAccess) otherArg;
        if (dcMotor instanceof DcMotorEx) {
          ((DcMotorEx) dcMotor).setTargetPositionTolerance((int) tolerance1);
        }
        if (other.dcMotor instanceof DcMotorEx) {
          ((DcMotorEx) other.dcMotor).setTargetPositionTolerance((int) tolerance2);
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
  @Block(classes = {DcMotorEx.class}, methodName = "setVelocity")
  public void setDualVelocity(double velocity1, Object otherArg, double velocity2) {
    try {
      startBlockExecution(BlockType.SETTER, ".Velocity");
      if (otherArg instanceof DcMotorAccess) {
        DcMotorAccess other = (DcMotorAccess) otherArg;
        if (dcMotor instanceof DcMotorEx) {
          ((DcMotorEx) dcMotor).setVelocity(velocity1);
        }
        if (other.dcMotor instanceof DcMotorEx) {
          ((DcMotorEx) other.dcMotor).setVelocity(velocity2);
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
  @Block(classes = {DcMotor.class, DcMotorEx.class}, methodName = "setZeroPowerBehavior")
  public void setDualZeroPowerBehavior(String zeroPowerBehavior1String,
      Object otherArg, String zeroPowerBehavior2String) {
    try {
      startBlockExecution(BlockType.SETTER, ".ZeroPowerBehavior");
      ZeroPowerBehavior zeroPowerBehavior1 = checkArg(zeroPowerBehavior1String, ZeroPowerBehavior.class, "first");
      ZeroPowerBehavior zeroPowerBehavior2 = checkArg(zeroPowerBehavior2String, ZeroPowerBehavior.class, "second");
      if (zeroPowerBehavior1 != null && zeroPowerBehavior2 != null &&
          otherArg instanceof DcMotorAccess) {
        DcMotorAccess other = (DcMotorAccess) otherArg;
        dcMotor.setZeroPowerBehavior(zeroPowerBehavior1);
        other.dcMotor.setZeroPowerBehavior(zeroPowerBehavior2);
      }
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  // From com.qualcomm.robotcore.hardware.DcMotorEx

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotorEx.class}, methodName = "setMotorEnable")
  public void setMotorEnable() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setMotorEnable");
      if (dcMotor instanceof DcMotorEx) {
        ((DcMotorEx) dcMotor).setMotorEnable();
      } else {
        reportWarning("This DcMotor is not a DcMotorEx.");
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
  @Block(classes = {DcMotorEx.class}, methodName = "setMotorDisable")
  public void setMotorDisable() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setMotorDisable");
      if (dcMotor instanceof DcMotorEx) {
        ((DcMotorEx) dcMotor).setMotorDisable();
      } else {
        reportWarning("This DcMotor is not a DcMotorEx.");
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
  @Block(classes = {DcMotorEx.class}, methodName = "isMotorEnabled")
  public boolean isMotorEnabled() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isMotorEnabled");
      if (dcMotor instanceof DcMotorEx) {
        return ((DcMotorEx) dcMotor).isMotorEnabled();
      } else {
        reportWarning("This DcMotor is not a DcMotorEx.");
      }
      return false;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotorEx.class}, methodName = "setVelocity")
  public void setVelocity(double angularRate) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setVelocity");
      if (dcMotor instanceof DcMotorEx) {
        ((DcMotorEx) dcMotor).setVelocity(angularRate);
      } else {
        reportWarning("This DcMotor is not a DcMotorEx.");
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
  @Block(classes = {DcMotorEx.class}, methodName = "setVelocity")
  public void setVelocity_withAngleUnit(double angularRate, String angleUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setVelocity");
      AngleUnit angleUnit = checkArg(angleUnitString, AngleUnit.class, "");
      if (angleUnit != null) {
        if (dcMotor instanceof DcMotorEx) {
          ((DcMotorEx) dcMotor).setVelocity(angularRate, angleUnit);
        } else {
          reportWarning("This DcMotor is not a DcMotorEx.");
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
  @Block(classes = {DcMotorEx.class}, methodName = "getVelocity")
  public double getVelocity() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getVelocity");
      if (dcMotor instanceof DcMotorEx) {
        return ((DcMotorEx) dcMotor).getVelocity();
      } else {
        reportWarning("This DcMotor is not a DcMotorEx.");
      }
      return 0.0;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotorEx.class}, methodName = "getVelocity")
  public double getVelocity_withAngleUnit(String angleUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getVelocity");
      AngleUnit angleUnit = checkArg(angleUnitString, AngleUnit.class, "");
      if (angleUnit != null) {
        if (dcMotor instanceof DcMotorEx) {
          return ((DcMotorEx) dcMotor).getVelocity(angleUnit);
        } else {
          reportWarning("This DcMotor is not a DcMotorEx.");
        }
      }
      return 0.0;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings({"unused", "deprecation"})
  @JavascriptInterface
  @Block(classes = {DcMotorEx.class}, methodName = "setVelocityPIDFCoefficients")
  public void setVelocityPIDFCoefficients(double p, double i, double d, double f) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setVelocityPIDFCoefficients");
      if (dcMotor instanceof DcMotorEx) {
        ((DcMotorEx) dcMotor).setVelocityPIDFCoefficients(p, i, d, f);
      } else {
        reportWarning("This DcMotor is not a DcMotorEx.");
      }
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings({"unused", "deprecation"})
  @JavascriptInterface
  @Block(classes = {DcMotorEx.class}, methodName = "setPositionPIDFCoefficients")
  public void setPositionPIDFCoefficients(double p) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setPositionPIDFCoefficients");
      if (dcMotor instanceof DcMotorEx) {
        ((DcMotorEx) dcMotor).setPositionPIDFCoefficients(p);
      } else {
        reportWarning("This DcMotor is not a DcMotorEx.");
      }
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings({"unused", "deprecation"})
  @JavascriptInterface
  @Block(classes = {DcMotorEx.class}, methodName = "setPIDFCoefficients")
  public void setPIDFCoefficients(String runModeString, Object pidfCoefficientsArg) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setPIDFCoefficients");
      RunMode runMode = checkArg(runModeString, RunMode.class, "");
      PIDFCoefficients pidfCoefficients = checkArg(pidfCoefficientsArg, PIDFCoefficients.class, "");
      if (runMode != null && pidfCoefficients != null) {
        if (dcMotor instanceof DcMotorEx) {
          ((DcMotorEx) dcMotor).setPIDFCoefficients(runMode, pidfCoefficients);
        } else {
          reportWarning("This DcMotor is not a DcMotorEx.");
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
  @Block(classes = {DcMotorEx.class}, methodName = "getPIDFCoefficients")
  public PIDFCoefficients getPIDFCoefficients(String runModeString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getPIDFCoefficients");
      RunMode runMode = checkArg(runModeString, RunMode.class, "");
      if (runMode != null) {
        if (dcMotor instanceof DcMotorEx) {
          return ((DcMotorEx) dcMotor).getPIDFCoefficients(runMode);
        } else {
          reportWarning("This DcMotor is not a DcMotorEx.");
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
  @Block(classes = {DcMotorEx.class}, methodName = "setTargetPositionTolerance")
  public void setTargetPositionTolerance(int tolerance) {
    try {
      startBlockExecution(BlockType.SETTER, ".TargetPositionTolerance");
      if (dcMotor instanceof DcMotorEx) {
        ((DcMotorEx) dcMotor).setTargetPositionTolerance(tolerance);
      } else {
        reportWarning("This DcMotor is not a DcMotorEx.");
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
  @Block(classes = {DcMotorEx.class}, methodName = "getTargetPositionTolerance")
  public int getTargetPositionTolerance() {
    try {
      startBlockExecution(BlockType.GETTER, ".TargetPositionTolerance");
      if (dcMotor instanceof DcMotorEx) {
        return ((DcMotorEx) dcMotor).getTargetPositionTolerance();
      } else {
        reportWarning("This DcMotor is not a DcMotorEx.");
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
  @Block(classes = {DcMotorEx.class}, methodName = "getCurrent")
  public double getCurrent(String currentUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getCurrent");
      CurrentUnit currentUnit = checkArg(currentUnitString, CurrentUnit.class, "");
      if (currentUnit != null) {
        if (dcMotor instanceof DcMotorEx) {
          return ((DcMotorEx) dcMotor).getCurrent(currentUnit);
        } else {
          reportWarning("This DcMotor is not a DcMotorEx.");
        }
      }
      return 0.0;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotorEx.class}, methodName = "getCurrentAlert")
  public double getCurrentAlert(String currentUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getCurrentAlert");
      CurrentUnit currentUnit = checkArg(currentUnitString, CurrentUnit.class, "");
      if (currentUnit != null) {
        if (dcMotor instanceof DcMotorEx) {
          return ((DcMotorEx) dcMotor).getCurrentAlert(currentUnit);
        } else {
          reportWarning("This DcMotor is not a DcMotorEx.");
        }
      }
      return 0.0;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DcMotorEx.class}, methodName = "setCurrentAlert")
  public void setCurrentAlert(double current, String currentUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setCurrentAlert");
      CurrentUnit currentUnit = checkArg(currentUnitString, CurrentUnit.class, "");
      if (currentUnit != null) {
        if (dcMotor instanceof DcMotorEx) {
          ((DcMotorEx) dcMotor).setCurrentAlert(current, currentUnit);
        } else {
          reportWarning("This DcMotor is not a DcMotorEx.");
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
  @Block(classes = {DcMotorEx.class}, methodName = "isOverCurrent")
  public boolean isOverCurrent() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isOverCurrent");
      if (dcMotor instanceof DcMotorEx) {
        return ((DcMotorEx) dcMotor).isOverCurrent();
      } else {
        reportWarning("This DcMotor is not a DcMotorEx.");
      }
      return false;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }
}
