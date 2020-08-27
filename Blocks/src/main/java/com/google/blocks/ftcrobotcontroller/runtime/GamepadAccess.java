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
    startBlockExecution(BlockType.GETTER, ".LeftStickX");
    if (gamepad != null) {
      return gamepad.left_stick_x;
    }
    return 0f;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "left_stick_y")
  public float getLeftStickY() {
    startBlockExecution(BlockType.GETTER, ".LeftStickY");
    if (gamepad != null) {
      return gamepad.left_stick_y;
    }
    return 0f;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "right_stick_x")
  public float getRightStickX() {
    startBlockExecution(BlockType.GETTER, ".RightStickX");
    if (gamepad != null) {
      return gamepad.right_stick_x;
    }
    return 0f;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "right_stick_y")
  public float getRightStickY() {
    startBlockExecution(BlockType.GETTER, ".RightStickY");
    if (gamepad != null) {
      return gamepad.right_stick_y;
    }
    return 0f;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "dpad_up")
  public boolean getDpadUp() {
    startBlockExecution(BlockType.GETTER, ".DpadUp");
    if (gamepad != null) {
      return gamepad.dpad_up;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "dpad_down")
  public boolean getDpadDown() {
    startBlockExecution(BlockType.GETTER, ".DpadDown");
    if (gamepad != null) {
      return gamepad.dpad_down;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "dpad_left")
  public boolean getDpadLeft() {
    startBlockExecution(BlockType.GETTER, ".DpadLeft");
    if (gamepad != null) {
      return gamepad.dpad_left;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "dpad_right")
  public boolean getDpadRight() {
    startBlockExecution(BlockType.GETTER, ".DpadRight");
    if (gamepad != null) {
      return gamepad.dpad_right;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "a")
  public boolean getA() {
    startBlockExecution(BlockType.GETTER, ".A");
    if (gamepad != null) {
      return gamepad.a;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "b")
  public boolean getB() {
    startBlockExecution(BlockType.GETTER, ".B");
    if (gamepad != null) {
      return gamepad.b;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "x")
  public boolean getX() {
    startBlockExecution(BlockType.GETTER, ".X");
    if (gamepad != null) {
      return gamepad.x;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "y")
  public boolean getY() {
    startBlockExecution(BlockType.GETTER, ".Y");
    if (gamepad != null) {
      return gamepad.y;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "guide")
  public boolean getGuide() {
    startBlockExecution(BlockType.GETTER, ".Guide");
    if (gamepad != null) {
      return gamepad.guide;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "start")
  public boolean getStart() {
    startBlockExecution(BlockType.GETTER, ".Start");
    if (gamepad != null) {
      return gamepad.start;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "back")
  public boolean getBack() {
    startBlockExecution(BlockType.GETTER, ".Back");
    if (gamepad != null) {
      return gamepad.back;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "left_bumper")
  public boolean getLeftBumper() {
    startBlockExecution(BlockType.GETTER, ".LeftBumper");
    if (gamepad != null) {
      return gamepad.left_bumper;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "right_bumper")
  public boolean getRightBumper() {
    startBlockExecution(BlockType.GETTER, ".RightBumper");
    if (gamepad != null) {
      return gamepad.right_bumper;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "left_stick_button")
  public boolean getLeftStickButton() {
    startBlockExecution(BlockType.GETTER, ".LeftStickButton");
    if (gamepad != null) {
      return gamepad.left_stick_button;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "right_stick_button")
  public boolean getRightStickButton() {
    startBlockExecution(BlockType.GETTER, ".RightStickButton");
    if (gamepad != null) {
      return gamepad.right_stick_button;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "left_trigger")
  public float getLeftTrigger() {
    startBlockExecution(BlockType.GETTER, ".LeftTrigger");
    if (gamepad != null) {
      return gamepad.left_trigger;
    }
    return 0f;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "right_trigger")
  public float getRightTrigger() {
    startBlockExecution(BlockType.GETTER, ".RightTrigger");
    if (gamepad != null) {
      return gamepad.right_trigger;
    }
    return 0f;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, methodName = "atRest")
  public boolean getAtRest() {
    startBlockExecution(BlockType.GETTER, ".AtRest");
    if (gamepad != null) {
      return gamepad.atRest();
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "circle")
  public boolean getCircle() {
    startBlockExecution(BlockType.GETTER, ".Circle");
    if (gamepad != null) {
      return gamepad.circle;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "cross")
  public boolean getCross() {
    startBlockExecution(BlockType.GETTER, ".Cross");
    if (gamepad != null) {
      return gamepad.cross;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "options")
  public boolean getOptions() {
    startBlockExecution(BlockType.GETTER, ".Options");
    if (gamepad != null) {
      return gamepad.options;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "ps")
  public boolean getPS() {
    startBlockExecution(BlockType.GETTER, ".PS");
    if (gamepad != null) {
      return gamepad.ps;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "share")
  public boolean getShare() {
    startBlockExecution(BlockType.GETTER, ".Share");
    if (gamepad != null) {
      return gamepad.share;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "square")
  public boolean getSquare() {
    startBlockExecution(BlockType.GETTER, ".Square");
    if (gamepad != null) {
      return gamepad.square;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "touchpad")
  public boolean getTouchpad() {
    startBlockExecution(BlockType.GETTER, ".Touchpad");
    if (gamepad != null) {
      return gamepad.touchpad;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {Gamepad.class}, fieldName = "triangle")
  public boolean getTriangle() {
    startBlockExecution(BlockType.GETTER, ".Triangle");
    if (gamepad != null) {
      return gamepad.triangle;
    }
    return false;
  }
}
