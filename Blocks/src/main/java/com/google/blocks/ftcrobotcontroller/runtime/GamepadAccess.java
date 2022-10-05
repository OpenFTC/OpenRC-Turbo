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

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Gamepad.LedEffect;
import com.qualcomm.robotcore.hardware.Gamepad.RumbleEffect;

/**
 * A class that provides JavaScript access to a {@link Gamepad}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class GamepadAccess extends Access {
  private final Gamepad gamepad;

  GamepadAccess(BlocksOpMode blocksOpMode, String identifier, Gamepad gamepad) {
    super(blocksOpMode, identifier, identifier);
    this.gamepad = gamepad;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "left_stick_x")
  public float getLeftStickX() {
    try {
      startBlockExecution(BlockType.GETTER, ".LeftStickX");
      if (gamepad != null) {
        return gamepad.left_stick_x;
      }
      return 0f;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "left_stick_y")
  public float getLeftStickY() {
    try {
      startBlockExecution(BlockType.GETTER, ".LeftStickY");
      if (gamepad != null) {
        return gamepad.left_stick_y;
      }
      return 0f;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "right_stick_x")
  public float getRightStickX() {
    try {
      startBlockExecution(BlockType.GETTER, ".RightStickX");
      if (gamepad != null) {
        return gamepad.right_stick_x;
      }
      return 0f;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "right_stick_y")
  public float getRightStickY() {
    try {
      startBlockExecution(BlockType.GETTER, ".RightStickY");
      if (gamepad != null) {
        return gamepad.right_stick_y;
      }
      return 0f;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "dpad_up")
  public boolean getDpadUp() {
    try {
      startBlockExecution(BlockType.GETTER, ".DpadUp");
      if (gamepad != null) {
        return gamepad.dpad_up;
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
  @Block(classes = {Gamepad.class}, fieldName = "dpad_down")
  public boolean getDpadDown() {
    try {
      startBlockExecution(BlockType.GETTER, ".DpadDown");
      if (gamepad != null) {
        return gamepad.dpad_down;
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
  @Block(classes = {Gamepad.class}, fieldName = "dpad_left")
  public boolean getDpadLeft() {
    try {
      startBlockExecution(BlockType.GETTER, ".DpadLeft");
      if (gamepad != null) {
        return gamepad.dpad_left;
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
  @Block(classes = {Gamepad.class}, fieldName = "dpad_right")
  public boolean getDpadRight() {
    try {
      startBlockExecution(BlockType.GETTER, ".DpadRight");
      if (gamepad != null) {
        return gamepad.dpad_right;
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
  @Block(classes = {Gamepad.class}, fieldName = "a")
  public boolean getA() {
    try {
      startBlockExecution(BlockType.GETTER, ".A");
      if (gamepad != null) {
        return gamepad.a;
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
  @Block(classes = {Gamepad.class}, fieldName = "b")
  public boolean getB() {
    try {
      startBlockExecution(BlockType.GETTER, ".B");
      if (gamepad != null) {
        return gamepad.b;
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
  @Block(classes = {Gamepad.class}, fieldName = "x")
  public boolean getX() {
    try {
      startBlockExecution(BlockType.GETTER, ".X");
      if (gamepad != null) {
        return gamepad.x;
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
  @Block(classes = {Gamepad.class}, fieldName = "y")
  public boolean getY() {
    try {
      startBlockExecution(BlockType.GETTER, ".Y");
      if (gamepad != null) {
        return gamepad.y;
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
  @Block(classes = {Gamepad.class}, fieldName = "guide")
  public boolean getGuide() {
    try {
      startBlockExecution(BlockType.GETTER, ".Guide");
      if (gamepad != null) {
        return gamepad.guide;
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
  @Block(classes = {Gamepad.class}, fieldName = "start")
  public boolean getStart() {
    try {
      startBlockExecution(BlockType.GETTER, ".Start");
      if (gamepad != null) {
        return gamepad.start;
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
  @Block(classes = {Gamepad.class}, fieldName = "back")
  public boolean getBack() {
    try {
      startBlockExecution(BlockType.GETTER, ".Back");
      if (gamepad != null) {
        return gamepad.back;
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
  @Block(classes = {Gamepad.class}, fieldName = "left_bumper")
  public boolean getLeftBumper() {
    try {
      startBlockExecution(BlockType.GETTER, ".LeftBumper");
      if (gamepad != null) {
        return gamepad.left_bumper;
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
  @Block(classes = {Gamepad.class}, fieldName = "right_bumper")
  public boolean getRightBumper() {
    try {
      startBlockExecution(BlockType.GETTER, ".RightBumper");
      if (gamepad != null) {
        return gamepad.right_bumper;
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
  @Block(classes = {Gamepad.class}, fieldName = "left_stick_button")
  public boolean getLeftStickButton() {
    try {
      startBlockExecution(BlockType.GETTER, ".LeftStickButton");
      if (gamepad != null) {
        return gamepad.left_stick_button;
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
  @Block(classes = {Gamepad.class}, fieldName = "right_stick_button")
  public boolean getRightStickButton() {
    try {
      startBlockExecution(BlockType.GETTER, ".RightStickButton");
      if (gamepad != null) {
        return gamepad.right_stick_button;
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
  @Block(classes = {Gamepad.class}, fieldName = "left_trigger")
  public float getLeftTrigger() {
    try {
      startBlockExecution(BlockType.GETTER, ".LeftTrigger");
      if (gamepad != null) {
        return gamepad.left_trigger;
      }
      return 0f;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "right_trigger")
  public float getRightTrigger() {
    try {
      startBlockExecution(BlockType.GETTER, ".RightTrigger");
      if (gamepad != null) {
        return gamepad.right_trigger;
      }
      return 0f;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, methodName = "atRest")
  public boolean getAtRest() {
    try {
      startBlockExecution(BlockType.GETTER, ".AtRest");
      if (gamepad != null) {
        return gamepad.atRest();
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
  @Block(classes = {Gamepad.class}, fieldName = "circle")
  public boolean getCircle() {
    try {
      startBlockExecution(BlockType.GETTER, ".Circle");
      if (gamepad != null) {
        return gamepad.circle;
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
  @Block(classes = {Gamepad.class}, fieldName = "cross")
  public boolean getCross() {
    try {
      startBlockExecution(BlockType.GETTER, ".Cross");
      if (gamepad != null) {
        return gamepad.cross;
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
  @Block(classes = {Gamepad.class}, fieldName = "options")
  public boolean getOptions() {
    try {
      startBlockExecution(BlockType.GETTER, ".Options");
      if (gamepad != null) {
        return gamepad.options;
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
  @Block(classes = {Gamepad.class}, fieldName = "ps")
  public boolean getPS() {
    try {
      startBlockExecution(BlockType.GETTER, ".PS");
      if (gamepad != null) {
        return gamepad.ps;
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
  @Block(classes = {Gamepad.class}, fieldName = "share")
  public boolean getShare() {
    try {
      startBlockExecution(BlockType.GETTER, ".Share");
      if (gamepad != null) {
        return gamepad.share;
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
  @Block(classes = {Gamepad.class}, fieldName = "square")
  public boolean getSquare() {
    try {
      startBlockExecution(BlockType.GETTER, ".Square");
      if (gamepad != null) {
        return gamepad.square;
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
  @Block(classes = {Gamepad.class}, fieldName = "touchpad")
  public boolean getTouchpad() {
    try {
      startBlockExecution(BlockType.GETTER, ".Touchpad");
      if (gamepad != null) {
        return gamepad.touchpad;
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
  @Block(classes = {Gamepad.class}, fieldName = "touchpad_finger_1")
  public boolean getTouchpadFinger1() {
    try {
      startBlockExecution(BlockType.GETTER, ".TouchpadFinger1");
      if (gamepad != null) {
        return gamepad.touchpad_finger_1;
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
  @Block(classes = {Gamepad.class}, fieldName = "touchpad_finger_1_x")
  public float getTouchpadFinger1X() {
    try {
      startBlockExecution(BlockType.GETTER, ".TouchpadFinger1X");
      if (gamepad != null) {
        return gamepad.touchpad_finger_1_x;
      }
      return 0f;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "touchpad_finger_1_y")
  public float getTouchpadFinger1Y() {
    try {
      startBlockExecution(BlockType.GETTER, ".TouchpadFinger1Y");
      if (gamepad != null) {
        return gamepad.touchpad_finger_1_y;
      }
      return 0f;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "touchpad_finger_2")
  public boolean getTouchpadFinger2() {
    try {
      startBlockExecution(BlockType.GETTER, ".TouchpadFinger2");
      if (gamepad != null) {
        return gamepad.touchpad_finger_2;
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
  @Block(classes = {Gamepad.class}, fieldName = "touchpad_finger_2_x")
  public float getTouchpadFinger2X() {
    try {
      startBlockExecution(BlockType.GETTER, ".TouchpadFinger2X");
      if (gamepad != null) {
        return gamepad.touchpad_finger_2_x;
      }
      return 0f;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "touchpad_finger_2_y")
  public float getTouchpadFinger2Y() {
    try {
      startBlockExecution(BlockType.GETTER, ".TouchpadFinger2Y");
      if (gamepad != null) {
        return gamepad.touchpad_finger_2_y;
      }
      return 0f;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "triangle")
  public boolean getTriangle() {
    try {
      startBlockExecution(BlockType.GETTER, ".Triangle");
      if (gamepad != null) {
        return gamepad.triangle;
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
  @Block(classes = {Gamepad.class}, methodName = "rumble")
  public void rumble_with1(int millis) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".rumble");
      if (gamepad != null) {
        gamepad.rumble(millis);
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
  @Block(classes = {Gamepad.class}, methodName = "rumble")
  public void rumble_with3(double rumble1, double rumble2, int millis) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".rumble");
      if (gamepad != null) {
        gamepad.rumble(rumble1, rumble2, millis);
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
  @Block(classes = {Gamepad.class}, methodName = "stopRumble")
  public void stopRumble() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".stopRumble");
      if (gamepad != null) {
        gamepad.stopRumble();
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
  @Block(classes = {Gamepad.class}, methodName = "rumbleBlips")
  public void rumbleBlips(int count) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".rumbleBlips");
      if (gamepad != null) {
        gamepad.rumbleBlips(count);
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
  @Block(classes = {Gamepad.class}, methodName = "isRumbling")
  public boolean isRumbling() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isRumbling");
      if (gamepad != null) {
        return gamepad.isRumbling();
      }
      return false;
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  private RumbleEffect checkRumbleEffect(Object rumbleEffectArg) {
    return checkArg(rumbleEffectArg, RumbleEffect.class, "rumbleEffect");
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, methodName = "runRumbleEffect")
  public void runRumbleEffect(Object rumbleEffectArg) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".runRumbleEffect");
      RumbleEffect rumbleEffect = checkRumbleEffect(rumbleEffectArg);
      if (gamepad != null && rumbleEffect != null) {
        gamepad.runRumbleEffect(rumbleEffect);
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
  @Block(classes = {Gamepad.class}, methodName = "setLedColor")
  public void setLedColor(double r, double g, double b, int millis) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setLedColor");
      if (gamepad != null) {
        gamepad.setLedColor(r, g, b, millis);
      }
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  private LedEffect checkLedEffect(Object ledEffectArg) {
    return checkArg(ledEffectArg, LedEffect.class, "ledEffect");
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, methodName = "runLedEffect")
  public void runLedEffect(Object ledEffectArg) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".runLedEffect");
      LedEffect ledEffect = checkLedEffect(ledEffectArg);
      if (gamepad != null && ledEffect != null) {
        gamepad.runLedEffect(ledEffect);
      }
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }
}
