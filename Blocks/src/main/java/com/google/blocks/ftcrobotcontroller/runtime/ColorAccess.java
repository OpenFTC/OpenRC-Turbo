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

import android.app.Activity;
import android.graphics.Color;
import android.webkit.JavascriptInterface;
import org.firstinspires.ftc.robotcore.external.JavaUtil;

/**
 * A class that provides JavaScript access to {@link Color}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class ColorAccess extends Access {
  private final Activity activity;

  ColorAccess(BlocksOpMode blocksOpMode, String identifier, Activity activity) {
    super(blocksOpMode, identifier, "Color");
    this.activity = activity;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getRed(int color) {
    try {
      startBlockExecution(BlockType.GETTER, ".Red");
      return Color.red(color);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getGreen(int color) {
    try {
      startBlockExecution(BlockType.GETTER, ".Green");
      return Color.green(color);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getBlue(int color) {
    try {
      startBlockExecution(BlockType.GETTER, ".Blue");
      return Color.blue(color);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public double getAlpha(int color) {
    try {
      startBlockExecution(BlockType.GETTER, ".Alpha");
      return Color.alpha(color);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public float getHue(int color) {
    try {
      startBlockExecution(BlockType.GETTER, ".Hue");
      return JavaUtil.colorToHue(color);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public float getSaturation(int color) {
    try {
      startBlockExecution(BlockType.GETTER, ".Saturation");
      return JavaUtil.colorToSaturation(color);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public float getValue(int color) {
    try {
      startBlockExecution(BlockType.GETTER, ".Value");
      return JavaUtil.colorToValue(color);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public int rgbToColor(int red, int green, int blue) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".rgbToColor");
      return Color.rgb(red, green, blue);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public int argbToColor(int alpha, int red, int green, int blue) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".argbToColor");
      return Color.argb(alpha, red, green, blue);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public int hsvToColor(float hue, float saturation, float value) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".hsvToColor");
      float[] array = new float[3];
      array[0] = hue;
      array[1] = saturation;
      array[2] = value;
      return Color.HSVToColor(array);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public int ahsvToColor(int alpha, float hue, float saturation, float value) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".ahsvToColor");
      float[] array = new float[3];
      array[0] = hue;
      array[1] = saturation;
      array[2] = value;
      return Color.HSVToColor(alpha, array);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public int textToColor(String text) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".textToColor");
      return Color.parseColor(text);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public float rgbToHue(int red, int green, int blue) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".rgbToHue");
      return JavaUtil.rgbToHue(red, green, blue);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public float rgbToSaturation(int red, int green, int blue) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".rgbToSaturation");
      return JavaUtil.rgbToSaturation(red, green, blue);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public float rgbToValue(int red, int green, int blue) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".rgbToValue");
      return JavaUtil.rgbToValue(red, green, blue);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public String toText(int color) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".toText");
      return JavaUtil.colorToText(color);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void showColor(int color) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".showColor");
      JavaUtil.showColor(activity, color);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }
}
