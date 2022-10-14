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
import com.qualcomm.hardware.bosch.BNO055IMU.AccelUnit;
import com.qualcomm.hardware.bosch.BNO055IMU.AccelerationIntegrator;
import com.qualcomm.hardware.bosch.BNO055IMU.AngleUnit;
import com.qualcomm.hardware.bosch.BNO055IMU.Parameters;
import com.qualcomm.hardware.bosch.BNO055IMU.SensorMode;
import com.qualcomm.hardware.bosch.BNO055IMU.TempUnit;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.hardware.bosch.NaiveAccelerationIntegrator;
import com.qualcomm.robotcore.hardware.I2cAddr;

/**
 * A class that provides JavaScript access to {@link BNO055IMU#Parameters}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class BNO055IMUParametersAccess extends Access {

  BNO055IMUParametersAccess(BlocksOpMode blocksOpMode, String identifier) {
    super(blocksOpMode, identifier, "IMU-BNO055.Parameters");
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Parameters.class, constructor = true)
  public Parameters create() {
    try {
      startBlockExecution(BlockType.CREATE, "");
      return new Parameters();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Parameters.class, fieldName = "accelUnit")
  public void setAccelUnit(Object parametersArg, String accelUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setAccelUnit");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      AccelUnit accelUnit = checkArg(accelUnitString, AccelUnit.class, "accelUnit");
      if (parameters != null && accelUnit != null) {
        parameters.accelUnit = accelUnit;
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
  @Block(classes = Parameters.class, fieldName = "accelUnit")
  public String getAccelUnit(Object parametersArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".AccelUnit");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        AccelUnit accelUnit = parameters.accelUnit;
        if (accelUnit != null) {
          return accelUnit.toString();
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

  enum Algorithm {
    NAIVE,
    JUST_LOGGING
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = Parameters.class, fieldName = "accelerationIntegrationAlgorithm")
  public void setAccelerationIntegrationAlgorithm(Object parametersArg, String algorithmString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setAccelerationIntegrationAlgorithm");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      Algorithm algorithm = checkArg(algorithmString, Algorithm.class, "accelerationIntegrationAlgorithm");
      if (parameters != null && algorithm != null) {
        switch (algorithm) {
          case NAIVE:
            parameters.accelerationIntegrationAlgorithm = null;
            break;
          case JUST_LOGGING:
            parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
            break;
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
  @Block(classes = Parameters.class, fieldName = "accelerationIntegrationAlgorithm")
  public String getAccelerationIntegrationAlgorithm(Object parametersArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".AccelerationIntegrationAlgorithm");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        AccelerationIntegrator accelerationIntegrator = parameters.accelerationIntegrationAlgorithm;
        if (accelerationIntegrator == null ||
            accelerationIntegrator instanceof NaiveAccelerationIntegrator) {
          return "NAIVE";
        } else if (accelerationIntegrator instanceof JustLoggingAccelerationIntegrator) {
          return "JUST_LOGGING";
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
  @Block(classes = Parameters.class, fieldName = "angleUnit")
  public void setAngleUnit(Object parametersArg, String angleUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setAngleUnit");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      AngleUnit angleUnit = checkArg(angleUnitString, AngleUnit.class, "angleUnit");
      if (parameters != null && angleUnit != null) {
        parameters.angleUnit = angleUnit;
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
  @Block(classes = Parameters.class, fieldName = "angleUnit")
  public String getAngleUnit(Object parametersArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".AngleUnit");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        AngleUnit angleUnit = parameters.angleUnit;
        if (angleUnit != null) {
          return angleUnit.toString();
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
  @Block(classes = Parameters.class, fieldName = "calibrationDataFile")
  public void setCalibrationDataFile(Object parametersArg, String calibrationDataFile) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setCalibrationDataFile");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        parameters.calibrationDataFile = calibrationDataFile;
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
  @Block(classes = Parameters.class, fieldName = "calibrationDataFile")
  public String getCalibrationDataFile(Object parametersArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".CalibrationDataFile");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        String calibrationDataFile = parameters.calibrationDataFile;
        if (calibrationDataFile != null) {
          return calibrationDataFile;
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
  @Block(classes = Parameters.class, fieldName = "i2cAddr")
  public void setI2cAddress7Bit(Object parametersArg, int i2cAddr7Bit) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setI2cAddress7Bit");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        parameters.i2cAddr = I2cAddr.create7bit(i2cAddr7Bit);
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
  @Block(classes = Parameters.class, fieldName = "i2cAddr")
  public int getI2cAddress7Bit(Object parametersArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".I2cAddress7Bit");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        I2cAddr i2cAddr = parameters.i2cAddr;
        if (i2cAddr != null) {
          return i2cAddr.get7Bit();
        }
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
  @Block(classes = Parameters.class, fieldName = "i2cAddr")
  public void setI2cAddress8Bit(Object parametersArg, int i2cAddr8Bit) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setI2cAddress8Bit");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        parameters.i2cAddr = I2cAddr.create8bit(i2cAddr8Bit);
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
  @Block(classes = Parameters.class, fieldName = "i2cAddr")
  public int getI2cAddress8Bit(Object parametersArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".I2cAddress8Bit");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        I2cAddr i2cAddr = parameters.i2cAddr;
        if (i2cAddr != null) {
          return i2cAddr.get8Bit();
        }
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
  @Block(classes = Parameters.class, fieldName = "loggingEnabled")
  public void setLoggingEnabled(Object parametersArg, boolean loggingEnabled) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setLoggingEnabled");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        parameters.loggingEnabled = loggingEnabled;
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
  @Block(classes = Parameters.class, fieldName = "loggingEnabled")
  public boolean getLoggingEnabled(Object parametersArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".LoggingEnabled");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        return parameters.loggingEnabled;
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
  @Block(classes = Parameters.class, fieldName = "loggingTag")
  public void setLoggingTag(Object parametersArg, String loggingTag) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setLoggingTag");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        parameters.loggingTag = loggingTag;
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
  @Block(classes = Parameters.class, fieldName = "loggingTag")
  public String getLoggingTag(Object parametersArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".LoggingTag");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        String loggingTag = parameters.loggingTag;
        if (loggingTag != null) {
          return loggingTag;
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
  @Block(classes = Parameters.class, fieldName = "mode")
  public void setSensorMode(Object parametersArg, String sensorModeString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setSensorMode");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      SensorMode sensorMode = checkArg(sensorModeString, SensorMode.class, "sensorMode");
      if (parameters != null && sensorMode != null) {
        parameters.mode = sensorMode;
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
  @Block(classes = Parameters.class, fieldName = "mode")
  public String getSensorMode(Object parametersArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".SensorMode");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        SensorMode sensorMode = parameters.mode;
        if (sensorMode != null) {
          return sensorMode.toString();
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
  @Block(classes = Parameters.class, fieldName = "temperatureUnit")
  public void setTempUnit(Object parametersArg, String tempUnitString) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setTempUnit");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      TempUnit tempUnit = checkArg(tempUnitString, TempUnit.class, "tempUnit");
      if (parameters != null && tempUnit != null) {
        parameters.temperatureUnit = tempUnit;
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
  @Block(classes = Parameters.class, fieldName = "temperatureUnit")
  public String getTempUnit(Object parametersArg) {
    try {
      startBlockExecution(BlockType.GETTER, ".TempUnit");
      Parameters parameters = checkBNO055IMUParameters(parametersArg);
      if (parameters != null) {
        TempUnit tempUnit = parameters.temperatureUnit;
        if (tempUnit != null) {
          return tempUnit.toString();
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
}
