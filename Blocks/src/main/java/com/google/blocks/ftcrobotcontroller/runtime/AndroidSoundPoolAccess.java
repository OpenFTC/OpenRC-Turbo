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
import com.qualcomm.ftccommon.SoundPlayer;
import org.firstinspires.ftc.robotcore.external.android.AndroidSoundPool;

/**
 * A class that provides JavaScript access to the Android SoundPool.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class AndroidSoundPoolAccess extends Access {
  private final AndroidSoundPool androidSoundPool;

  AndroidSoundPoolAccess(BlocksOpMode blocksOpMode, String identifier) {
    super(blocksOpMode, identifier, "AndroidSoundPool");
    androidSoundPool = new AndroidSoundPool();
  }

  // Access methods

  @Override
  void close() {
    androidSoundPool.close();
  }

  // Javascript methods

  @SuppressWarnings({"unused", "deprecation"})
  @JavascriptInterface
  public void initialize() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".initialize");
      androidSoundPool.initialize(SoundPlayer.getInstance());
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public boolean preloadSound(String soundName) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".preloadSound");
      try {
        if (androidSoundPool.preloadSound(soundName))  {
          return true;
        }
        reportWarning("Failed to preload " + soundName);
      } catch (IllegalStateException e) {
        reportWarning(e.getMessage());
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
  public void play(String soundName) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".play");
      try {
        if (!androidSoundPool.play(soundName))  {
          reportWarning("Failed to load " + soundName);
        }
      } catch (IllegalStateException e) {
        reportWarning(e.getMessage());
      }
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings({"unused"})
  @JavascriptInterface
  public void stop() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".stop");
      androidSoundPool.stop();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public float getVolume() {
    try {
      startBlockExecution(BlockType.GETTER, ".Volume");
      return androidSoundPool.getVolume();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setVolume(float volume) {
    try {
      startBlockExecution(BlockType.SETTER, ".Volume");
      if (volume >= 0.0f && volume <= 1.0f) {
        androidSoundPool.setVolume(volume);
      } else {
        reportInvalidArg("", "a number between 0.0 and 1.0");
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
  public float getRate() {
    try {
      startBlockExecution(BlockType.GETTER, ".Rate");
      return androidSoundPool.getRate();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setRate(float rate) {
    try {
      startBlockExecution(BlockType.SETTER, ".Rate");
      if (rate >= 0.5f && rate <= 2.0f) {
        androidSoundPool.setRate(rate);
      } else {
        reportInvalidArg("", "a number between 0.5 and 2.0");
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
  public int getLoop() {
    try {
      startBlockExecution(BlockType.GETTER, ".Loop");
      return androidSoundPool.getLoop();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setLoop(int loop) {
    try {
      startBlockExecution(BlockType.SETTER, ".Loop");
      if (loop >= -1) {
        androidSoundPool.setLoop(loop);
      } else {
        reportInvalidArg("", "a number greater than or equal to -1");
      }
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }
}
