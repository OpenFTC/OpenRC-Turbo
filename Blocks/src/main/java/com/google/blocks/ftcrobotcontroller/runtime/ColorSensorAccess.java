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
import com.qualcomm.hardware.adafruit.AdafruitI2cColorSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.Light;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.SwitchableLight;

/**
 * A class that provides JavaScript access to a {@link ColorSensor}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class ColorSensorAccess extends HardwareAccess<ColorSensor> {
  private final ColorSensor colorSensor;

  ColorSensorAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, ColorSensor.class);
    this.colorSensor = hardwareDevice;
  }

  // Properties

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitI2cColorSensor.class, ColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "red")
  public int getRed() {
    try {
      startBlockExecution(BlockType.GETTER, ".Red");
      return colorSensor.red();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitI2cColorSensor.class, ColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "green")
  public int getGreen() {
    try {
      startBlockExecution(BlockType.GETTER, ".Green");
      return colorSensor.green();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitI2cColorSensor.class, ColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "blue")
  public int getBlue() {
    try {
      startBlockExecution(BlockType.GETTER, ".Blue");
      return colorSensor.blue();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitI2cColorSensor.class, ColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "alpha")
  public int getAlpha() {
    try {
      startBlockExecution(BlockType.GETTER, ".Alpha");
      return colorSensor.alpha();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitI2cColorSensor.class, ColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "argb")
  public int getArgb() {
    try {
      startBlockExecution(BlockType.GETTER, ".Argb");
      return colorSensor.argb();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitI2cColorSensor.class, ColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "enableLed")
  public void enableLed(boolean enable) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".enableLed");
      colorSensor.enableLed(enable);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {ModernRoboticsI2cColorSensor.class},
      methodName = "enableLight")
  public void enableLight(boolean enable) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".enableLight");
      if (colorSensor instanceof SwitchableLight) {
        ((SwitchableLight) colorSensor).enableLight(enable);
      } else {
        reportWarning("This ColorSensor is not a SwitchableLight.");
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
  @Block(classes = {AdafruitI2cColorSensor.class, Light.class, ModernRoboticsI2cColorSensor.class},
      methodName = "isLightOn")
  public boolean isLightOn() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isLightOn");
      if (colorSensor instanceof Light) {
        return ((Light) colorSensor).isLightOn();
      } else {
        reportWarning("This ColorSensor is not a Light.");
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
  @Block(classes = {AdafruitI2cColorSensor.class, ColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "setI2cAddress")
  public void setI2cAddress7Bit(int i2cAddr7Bit) {
    try {
      startBlockExecution(BlockType.SETTER, ".I2cAddress7Bit");
      colorSensor.setI2cAddress(I2cAddr.create7bit(i2cAddr7Bit));
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitI2cColorSensor.class, ColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "getI2cAddress")
  public int getI2cAddress7Bit() {
    try {
      startBlockExecution(BlockType.GETTER, ".I2cAddress7Bit");
      I2cAddr i2cAddr = colorSensor.getI2cAddress();
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
  @Block(classes = {AdafruitI2cColorSensor.class, ColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "setI2cAddress")
  public void setI2cAddress8Bit(int i2cAddr8Bit) {
    try {
      startBlockExecution(BlockType.SETTER, ".I2cAddress8Bit");
      colorSensor.setI2cAddress(I2cAddr.create8bit(i2cAddr8Bit));
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitI2cColorSensor.class, ColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "getI2cAddress")
  public int getI2cAddress8Bit() {
    try {
      startBlockExecution(BlockType.GETTER, ".I2cAddress8Bit");
      I2cAddr i2cAddr = colorSensor.getI2cAddress();
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
  @Block(classes = {AdafruitI2cColorSensor.class, ModernRoboticsI2cColorSensor.class},
      methodName = "toString")
  public String toText() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".toText");
      return colorSensor.toString();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitI2cColorSensor.class, ModernRoboticsI2cColorSensor.class, NormalizedColorSensor.class},
      methodName = "getNormalizedColors")
  public String getNormalizedColors() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getNormalizedColors");
      if (colorSensor instanceof NormalizedColorSensor) {
        NormalizedRGBA color = ((NormalizedColorSensor) colorSensor).getNormalizedColors();
        return "{ \"Red\":" + color.red +
            ", \"Green\":" + color.green +
            ", \"Blue\":" + color.blue +
            ", \"Alpha\":" + color.alpha +
            ", \"Color\":" + color.toColor() + " }";
      }
      return "{ \"Red\":0" +
          ", \"Green\":0" +
          ", \"Blue\":0" +
          ", \"Alpha\":0" +
          ", \"Color\":0 }";
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {AdafruitI2cColorSensor.class, ModernRoboticsI2cColorSensor.class, NormalizedColorSensor.class},
      methodName = "setGain")
  public void setGain(float gain) {
    try {
      startBlockExecution(BlockType.SETTER, ".Gain");
      if (colorSensor instanceof NormalizedColorSensor) {
        ((NormalizedColorSensor) colorSensor).setGain(gain);
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
  @Block(classes = {AdafruitI2cColorSensor.class, ModernRoboticsI2cColorSensor.class, NormalizedColorSensor.class},
      methodName = "getGain")
  public float getGain() {
    try {
      startBlockExecution(BlockType.GETTER, ".Gain");
      if (colorSensor instanceof NormalizedColorSensor) {
        return ((NormalizedColorSensor) colorSensor).getGain();
      }
      return 0;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }
}
