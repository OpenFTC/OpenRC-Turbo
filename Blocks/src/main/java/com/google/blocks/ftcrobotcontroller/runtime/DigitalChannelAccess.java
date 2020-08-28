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
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannel.Mode;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A class that provides JavaScript access to a {@link DigitalChannel}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class DigitalChannelAccess extends HardwareAccess<DigitalChannel> {
  private final DigitalChannel digitalChannel;

  DigitalChannelAccess(BlocksOpMode blocksOpMode, HardwareItem hardwareItem, HardwareMap hardwareMap) {
    super(blocksOpMode, hardwareItem, hardwareMap, DigitalChannel.class);
    this.digitalChannel = hardwareDevice;
  }

  // Properties

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DigitalChannel.class}, methodName = "setMode")
  public void setMode(String modeString) {
    startBlockExecution(BlockType.SETTER, ".Mode");
    Mode mode = checkArg(modeString, Mode.class, "");
    if (mode != null) {
      digitalChannel.setMode(mode);
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DigitalChannel.class}, methodName = "getMode")
  public String getMode() {
    startBlockExecution(BlockType.GETTER, ".Mode");
    Mode mode = digitalChannel.getMode();
    if (mode != null) {
      return mode.toString();
    }
    return "";
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DigitalChannel.class}, methodName = "setState")
  public void setState(boolean state) {
    startBlockExecution(BlockType.SETTER, ".State");
    digitalChannel.setState(state);
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {DigitalChannel.class}, methodName = "getState")
  public boolean getState() {
    startBlockExecution(BlockType.GETTER, ".State");
    return digitalChannel.getState();
  }
}
