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
import org.firstinspires.ftc.robotcore.external.android.AndroidTextToSpeech;

/**
 * A class that provides JavaScript access to the Android TextToSpeech.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class AndroidTextToSpeechAccess extends Access {
  private final AndroidTextToSpeech androidTextToSpeech;

  AndroidTextToSpeechAccess(BlocksOpMode blocksOpMode, String identifier) {
    super(blocksOpMode, identifier, "AndroidTextToSpeech");
    androidTextToSpeech = new AndroidTextToSpeech();
  }

  // Access methods

  @Override
  void close() {
    androidTextToSpeech.close();
  }

  // Javascript methods

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void initialize() {
    try {
      startBlockExecution(BlockType.FUNCTION, ".initialize");
      androidTextToSpeech.initialize();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public String getStatus() {
    try {
      startBlockExecution(BlockType.GETTER, ".Status");
      return androidTextToSpeech.getStatus();
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings({"unused", "deprecation"})
  @JavascriptInterface
  public String getLanguageCode() {
    try {
      startBlockExecution(BlockType.GETTER, ".LanguageCode");
      try {
        return androidTextToSpeech.getLanguageCode();
      } catch (IllegalStateException e) {
        reportWarning(e.getMessage());
      }
      return "";
    } catch (Throwable e) {
      blocksOpMode.handleFatalException(e);
      throw new AssertionError("impossible", e);
    } finally {
      endBlockExecution();
    }
  }

  @SuppressWarnings({"unused", "deprecation"})
  @JavascriptInterface
  public String getCountryCode() {
    try {
      startBlockExecution(BlockType.GETTER, ".CountryCode");
      try {
        return androidTextToSpeech.getCountryCode();
      } catch (IllegalStateException e) {
        reportWarning(e.getMessage());
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
  public boolean getIsSpeaking() {
    try {
      startBlockExecution(BlockType.GETTER, ".IsSpeaking");
      try {
        return androidTextToSpeech.isSpeaking();
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
  public void setPitch(float pitch) {
    try {
      startBlockExecution(BlockType.SETTER, ".Pitch");
      try {
        androidTextToSpeech.setPitch(pitch);
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

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setSpeechRate(float speechRate) {
    try {
      startBlockExecution(BlockType.SETTER, ".SpeechRate");
      try {
        androidTextToSpeech.setSpeechRate(speechRate);
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

  @SuppressWarnings("unused")
  @JavascriptInterface
  public boolean isLanguageAvailable(String languageCode) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isLanguageAvailable");
      try {
        return androidTextToSpeech.isLanguageAvailable(languageCode);
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
  public boolean isLanguageAndCountryAvailable(String languageCode, String countryCode) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".isLanguageAndCountryAvailable");
      try {
        return androidTextToSpeech.isLanguageAndCountryAvailable(languageCode, countryCode);
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
  public void setLanguage(String languageCode) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setLanguage");
      try {
        androidTextToSpeech.setLanguage(languageCode);
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

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setLanguageAndCountry(String languageCode, String countryCode) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".setLanguageAndCountry");
      try {
        androidTextToSpeech.setLanguageAndCountry(languageCode, countryCode);
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

  @SuppressWarnings({"unused", "deprecation"})
  @JavascriptInterface
  public void speak(String text) {
    try {
      startBlockExecution(BlockType.FUNCTION, ".speak");
      try {
        androidTextToSpeech.speak(text);
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
}
