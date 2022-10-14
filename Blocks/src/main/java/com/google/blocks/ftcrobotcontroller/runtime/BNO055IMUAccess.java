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
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.bosch.BNO055IMU.CalibrationStatus;
import com.qualcomm.hardware.bosch.BNO055IMU.Parameters;
import com.qualcomm.hardware.bosch.BNO055IMU.SystemError;
import com.qualcomm.hardware.bosch.BNO055IMU.SystemStatus;
import com.qualcomm.hardware.bosch.BNO055IMUImpl;
import com.qualcomm.hardware.lynx.LynxEmbeddedIMU;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.util.ReadWriteFile;
import java.util.Set;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Axis;
import org.firstinspires.ftc.robotcore.external.navigation.MagneticFlux;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Quaternion;
import org.firstinspires.ftc.robotcore.external.navigation.Temperature;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

/**
 * A class that provides JavaScript access to {@link BNO055IMUImpl}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class BNO055IMUAccess extends HardwareAccess<BNO055IMUImpl> {
  private final BNO055IMUImpl imu;

  BNO055IMUAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, BNO055IMUImpl.class);
    this.imu = hardwareDevice;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getAcceleration")
  public Acceleration getAcceleration() {
    try {
      startBlockExecution(BlockType.GETTER, ".Acceleration");
      return imu.getAcceleration();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getAngularOrientation")
  public Orientation getAngularOrientation() {
    try {
      startBlockExecution(BlockType.GETTER, ".AngularOrientation");
      return imu.getAngularOrientation();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getAngularVelocity")
  public AngularVelocity getAngularVelocity() {
    try {
      startBlockExecution(BlockType.GETTER, ".AngularVelocity");
      return imu.getAngularVelocity();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getCalibrationStatus")
  public String getCalibrationStatus() {
    try {
      startBlockExecution(BlockType.GETTER, ".CalibrationStatus");
      CalibrationStatus calibrationStatus = imu.getCalibrationStatus();
      if (calibrationStatus != null) {
        return calibrationStatus.toString();
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
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getGravity")
  public Acceleration getGravity() {
    try {
      startBlockExecution(BlockType.GETTER, ".Gravity");
      return imu.getGravity();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getLinearAcceleration")
  public Acceleration getLinearAcceleration() {
    try {
      startBlockExecution(BlockType.GETTER, ".LinearAcceleration");
      return imu.getLinearAcceleration();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getMagneticFieldStrength")
  public MagneticFlux getMagneticFieldStrength() {
    try {
      startBlockExecution(BlockType.GETTER, ".MagneticFieldStrength");
      return imu.getMagneticFieldStrength();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getOverallAcceleration")
  public Acceleration getOverallAcceleration() {
    try {
      startBlockExecution(BlockType.GETTER, ".OverallAcceleration");
      return imu.getOverallAcceleration();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getParameters")
  public Parameters getParameters() {
    try {
      startBlockExecution(BlockType.GETTER, ".Parameters");
      return imu.getParameters();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getPosition")
  public Position getPosition() {
    try {
      startBlockExecution(BlockType.GETTER, ".Position");
      return imu.getPosition();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getQuaternionOrientation")
  public Quaternion getQuaternionOrientation() {
    try {
      startBlockExecution(BlockType.GETTER, ".QuaternionOrientation");
      return imu.getQuaternionOrientation();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getSystemError")
  public String getSystemError() {
    try {
      startBlockExecution(BlockType.GETTER, ".SystemError");
      SystemError systemError = imu.getSystemError();
      if (systemError != null) {
        return systemError.toString();
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
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getSystemStatus")
  public String getSystemStatus() {
    try {
      startBlockExecution(BlockType.GETTER, ".SystemStatus");
      SystemStatus systemStatus = imu.getSystemStatus();
      if (systemStatus != null) {
        return systemStatus.toString();
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
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getTemperature")
  public Temperature getTemperature() {
    try {
      startBlockExecution(BlockType.GETTER, ".Temperature");
      return imu.getTemperature();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getVelocity")
  public Velocity getVelocity() {
    try {
      startBlockExecution(BlockType.GETTER, ".Velocity");
      return imu.getVelocity();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "initialize")
  public void initialize(Object parametersArg) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".initialize");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        imu.initialize(parameters);
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
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "startAccelerationIntegration")
  public void startAccelerationIntegration_with1(int msPollInterval) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".startAccelerationIntegration");
      imu.startAccelerationIntegration(
          null /* initialPosition */, null /* initialVelocity */, msPollInterval);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "startAccelerationIntegration")
  public void startAccelerationIntegration_with3(
      Object initialPositionArg, Object initialVelocityArg, int msPollInterval) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".startAccelerationIntegration");
      Position initialPosition = checkArg(initialPositionArg, Position.class, "initialPosition");
      Velocity initialVelocity = checkArg(initialVelocityArg, Velocity.class, "initialVelocity");
      if (initialPosition != null && initialVelocity != null) {
        imu.startAccelerationIntegration(initialPosition, initialVelocity, msPollInterval);
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
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "stopAccelerationIntegration")
  public void stopAccelerationIntegration() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".stopAccelerationIntegration");
      imu.stopAccelerationIntegration();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "isSystemCalibrated")
  public boolean isSystemCalibrated() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isSystemCalibrated");
      return imu.isSystemCalibrated();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "isGyroCalibrated")
  public boolean isGyroCalibrated() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isGyroCalibrated");
      return imu.isGyroCalibrated();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "isAccelerometerCalibrated")
  public boolean isAccelerometerCalibrated() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isAccelerometerCalibrated");
      return imu.isAccelerometerCalibrated();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "isMagnetometerCalibrated")
  public boolean isMagnetometerCalibrated() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isMagnetometerCalibrated");
      return imu.isMagnetometerCalibrated();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "readCalibrationData")
  public void saveCalibrationData(String absoluteFileName) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".saveCalibrationData");
      ReadWriteFile.writeFile(
          AppUtil.getInstance().getSettingsFile(absoluteFileName),
          imu.readCalibrationData().serialize());
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "setI2cAddress")
  public void setI2cAddress7Bit(int i2cAddr7Bit) {
    try {
      startBlockExecution(BlockType.SETTER, ".I2cAddress7Bit");
      imu.setI2cAddress(I2cAddr.create7bit(i2cAddr7Bit));
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getI2cAddress")
  public int getI2cAddress7Bit() {
    try {
      startBlockExecution(BlockType.GETTER, ".I2cAddress7Bit");
      I2cAddr i2cAddr = imu.getI2cAddress();
      if (i2cAddr != null) {
        return i2cAddr.get7Bit();
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
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "setI2cAddress")
  public void setI2cAddress8Bit(int i2cAddr8Bit) {
    try {
      startBlockExecution(BlockType.SETTER, ".I2cAddress8Bit");
      imu.setI2cAddress(I2cAddr.create8bit(i2cAddr8Bit));
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getI2cAddress")
  public int getI2cAddress8Bit() {
    try {
      startBlockExecution(BlockType.GETTER, ".I2cAddress8Bit");
      I2cAddr i2cAddr = imu.getI2cAddress();
      if (i2cAddr != null) {
        return i2cAddr.get8Bit();
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
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getAngularVelocityAxes")
  public String getAngularVelocityAxes() {
    try {
      startBlockExecution(BlockType.GETTER, ".AngularVelocityAxes");
      Set<Axis> axes = imu.getAngularVelocityAxes();
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      String delimiter = "";
      for (Axis axis : axes) {
        sb.append(delimiter).append("\"").append(axis.toString()).append("\"");
        delimiter = ",";
      }
      sb.append("]");
      return sb.toString();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getAngularVelocity")
  public AngularVelocity getAngularVelocity(String angleUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getAngularVelocity");
      AngleUnit angleUnit = checkAngleUnit(angleUnitString);
      if (angleUnit != null) {
        return imu.getAngularVelocity(angleUnit);
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
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getAngularOrientationAxes")
  public String getAngularOrientationAxes() {
    try {
      startBlockExecution(BlockType.GETTER, ".AngularOrientationAxes");
      Set<Axis> axes = imu.getAngularOrientationAxes();
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      String delimiter = "";
      for (Axis axis : axes) {
        sb.append(delimiter).append("\"").append(axis.toString()).append("\"");
        delimiter = ",";
      }
      sb.append("]");
      return sb.toString();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitBNO055IMU.class, LynxEmbeddedIMU.class}, methodName = "getAngularOrientation")
  public Orientation getAngularOrientation(String axesReferenceString, String axesOrderString, String angleUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getAngularOrientation");
      AxesReference axesReference = checkAxesReference(axesReferenceString);
      AxesOrder axesOrder = checkAxesOrder(axesOrderString);
      AngleUnit angleUnit = checkAngleUnit(angleUnitString);
      if (axesReference != null && axesOrder != null && angleUnit != null) {
        return imu.getAngularOrientation(axesReference, axesOrder, angleUnit);
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
