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
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * A class that provides JavaScript access to a {@link LinearOpMode}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class LinearOpModeAccess extends Access {
  private final BlocksOpMode blocksOpMode;

  LinearOpModeAccess(BlocksOpMode blocksOpMode, String identifier, String projectName) {
    super(blocksOpMode, identifier, projectName);
    this.blocksOpMode = blocksOpMode;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class}, methodName = "waitForStart")
  public void waitForStart() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".waitForStart");
      blocksOpMode.waitForStartForBlocks();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class}, methodName = "idle")
  public void idle() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".idle");
      blocksOpMode.idle();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class}, methodName = "sleep")
  public void sleep(double millis) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".sleep");
      blocksOpMode.sleepForBlocks((long) millis);
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class}, methodName = "opModeInInit")
  public boolean opModeInInit() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".opModeInInit");
      return blocksOpMode.opModeInInit();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class}, methodName = "opModeIsActive")
  public boolean opModeIsActive() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".opModeIsActive");
      return blocksOpMode.opModeIsActive();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class}, methodName = "isStarted")
  public boolean isStarted() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isStarted");
      return blocksOpMode.isStartedForBlocks();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class}, methodName = "isStopRequested")
  public boolean isStopRequested() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isStopRequested");
      return blocksOpMode.isStopRequestedForBlocks();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class, OpMode.class}, methodName = "getRuntime")
  public double getRuntime() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".getRuntime");
      return blocksOpMode.getRuntime();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class, OpMode.class}, methodName = "resetRuntime")
  public void resetRuntime() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".resetRuntime");
      blocksOpMode.resetRuntime();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class, OpMode.class}, methodName = "requestOpModeStop")
  public void requestOpModeStop() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".requestOpModeStop");
      blocksOpMode.requestOpModeStop();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  @Block(classes = {LinearOpMode.class, OpMode.class}, methodName = "terminateOpModeNow")
  public void terminateOpModeNow() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".terminateOpModeNow");
      blocksOpMode.terminateOpModeNowForBlocks();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }
}
