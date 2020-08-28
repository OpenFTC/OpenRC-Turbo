/*
 * Copyright 2018 Google LLC
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

package org.firstinspires.ftc.robotcore.external.android;

import static android.speech.tts.TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;

import android.content.Context;
import android.os.Looper;
import android.speech.tts.TextToSpeech;

import com.qualcomm.robotcore.R;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

/**
 * A class that provides access to the Android TextToSpeech.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class AndroidTextToSpeech {
  private static final String TAG = "AndroidTextToSpeech";

  private TextToSpeech textToSpeech;
  private CountDownLatch initializationLatch = new CountDownLatch(1);
  private volatile boolean initializationFailed = false;

  // public methods

  /**
   * Initialize the TextToSpeech engine.
   */
  public synchronized void initialize() {
    final Context context = AppUtil.getDefContext();

    RobotLog.vv(TAG, "Beginning TTS initialization");
    textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if (status == TextToSpeech.ERROR) {
          initializationFailed = true;
          RobotLog.setGlobalWarningMessage(context.getString(R.string.warningTtsFailedToInitialize));
          RobotLog.ww(TAG, "TTS initialization failed");
        } else {
          RobotLog.vv(TAG, "TTS initialization completed successfully");
        }
        initializationLatch.countDown();
      }
    });
  }

  /**
   * Returns the TextToSpeech initialization status.
   */
  // TODO(Noah): Replace implementation during offseason: https://gist.github.com/NoahAndrews/68978d178721932a6e169c8d60a31a03
  public synchronized String getStatus() {
    if (textToSpeech == null || initializationLatch.getCount() > 0) {
      return "Not initialized";
    }
    if (initializationFailed) {
      return "Error code " + TextToSpeech.ERROR;
    }
    return "Success";
  }

  /**
   * Returns the current language code.
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  @SuppressWarnings("deprecation")
  public synchronized String getLanguageCode() {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    try {
      waitForInitializationToFinish();
      Locale locale = textToSpeech.getLanguage();
      if (locale != null) {
        return locale.getLanguage();
      }
    } catch (InterruptedException e) {
      RobotLog.ww(TAG, e, "InterruptedException thrown while waiting for TTS initialization");
      Thread.currentThread().interrupt();
    }
    return "";
  }

  /**
   * Returns the current country code.
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  @SuppressWarnings("deprecation")
  public synchronized String getCountryCode() {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    try {
      waitForInitializationToFinish();
      Locale locale = textToSpeech.getLanguage();
      if (locale != null) {
        return locale.getCountry();
      }
    } catch (InterruptedException e) {
      RobotLog.ww(TAG, e, "InterruptedException thrown while waiting for TTS initialization");
      Thread.currentThread().interrupt();
    }
    return "";
  }

  /**
   * Returns true if the TextToSpeech engine is busy speaking.
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  public synchronized boolean isSpeaking() {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    return textToSpeech.isSpeaking();
  }

  /**
   * Stop speaking, if necessary.
   */
  public synchronized void stop() {
    if (textToSpeech != null) {
      textToSpeech.stop();
    }
  }

  /**
   * Sets the speech pitch. 1.0 is the normal pitch. Lower values will lower the tone of the
   * synthesized voice. Greater values will increase the tone of the synthesized voice.
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  public synchronized void setPitch(float pitch) {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    try {
      waitForInitializationToFinish();
      textToSpeech.setPitch(pitch);
    } catch (InterruptedException e) {
      RobotLog.ww(TAG, e, "InterruptedException thrown while waiting for TTS initialization");
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Sets the speech rate. 1.0 is the normal speech rate. Lower values will slow down the speech
   * (0.5 is half the normal speech rate). Greater values will accelerate the speech (2.0 is twice
   * the normal speech rate).
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  public synchronized void setSpeechRate(float speechRate) {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    try {
      waitForInitializationToFinish();
      textToSpeech.setSpeechRate(speechRate);
    } catch (InterruptedException e) {
      RobotLog.ww(TAG, e, "InterruptedException thrown while waiting for TTS initialization");
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Returns true if the given language is supported. The languageCode must be an ISO 639 alpha-2
   * or alpha-3 language code, or a language subtag up to 8 characters in length.
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  public synchronized boolean isLanguageAvailable(String languageCode) {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    Locale locale = new Locale(languageCode);
    try {
      waitForInitializationToFinish();
      return textToSpeech.isLanguageAvailable(locale) == LANG_COUNTRY_VAR_AVAILABLE;
    } catch (InterruptedException e) {
      RobotLog.ww(TAG, e, "InterruptedException thrown while waiting for TTS initialization");
      Thread.currentThread().interrupt();
      return false;
    }
  }

  /**
   * Returns true if the given language is supported. The languageCode must be an ISO 639 alpha-2
   * or alpha-3 language code, or a language subtag up to 8 characters in length. The countryCode
   * must be an ISO 3166 alpha-2 country code or a UN M.49 numeric-3 area code.
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  public synchronized boolean isLanguageAndCountryAvailable(String languageCode, String countryCode) {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    try {
      waitForInitializationToFinish();
      Locale locale = new Locale(languageCode, countryCode);
      return textToSpeech.isLanguageAvailable(locale) == LANG_COUNTRY_VAR_AVAILABLE;
    } catch (InterruptedException e) {
      RobotLog.ww(TAG, e, "InterruptedException thrown while waiting for TTS initialization");
      Thread.currentThread().interrupt();
      return false;
    }
  }

  /**
   * Returns true if the given locale is supported.
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  public synchronized boolean isLocaleAvailable(Locale locale) {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    try {
      waitForInitializationToFinish();
      return textToSpeech.isLanguageAvailable(locale) == LANG_COUNTRY_VAR_AVAILABLE;
    } catch (InterruptedException e) {
      RobotLog.ww(TAG, e, "InterruptedException thrown while waiting for TTS initialization");
      Thread.currentThread().interrupt();
      return false;
    }
  }

  /**
   * Sets the language. The languageCode must be an ISO 639 alpha-2 or alpha-3 language code, or a
   * language subtag up to 8 characters in length.
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  public synchronized void setLanguage(String languageCode) {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    try {
      waitForInitializationToFinish();
      textToSpeech.setLanguage(new Locale(languageCode));
    } catch (InterruptedException e) {
      RobotLog.ww(TAG, e, "InterruptedException thrown while waiting for TTS initialization");
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Sets the language and country. The languageCode must be an ISO 639 alpha-2 or alpha-3 language
   * code, or a language subtag up to 8 characters in length. The countryCode must be an ISO 3166
   * alpha-2 country code or a UN M.49 numeric-3 area code.
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  public synchronized void setLanguageAndCountry(String languageCode, String countryCode) {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    try {
      waitForInitializationToFinish();
      textToSpeech.setLanguage(new Locale(languageCode, countryCode));
    } catch (InterruptedException e) {
      RobotLog.ww(TAG, e, "InterruptedException thrown while waiting for TTS initialization");
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Speaks the given text.
   *
   * @throws IllegalStateException if initialize has not been called yet.
   */
  @SuppressWarnings("deprecation")
  public synchronized void speak(String text) {
    if (textToSpeech == null) {
      throw new IllegalStateException("You forgot to call AndroidTextToSpeech.initialize!");
    }
    try {
      waitForInitializationToFinish();
      textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null /* params */);
    } catch (InterruptedException e) {
      RobotLog.ww(TAG, e, "InterruptedException thrown while waiting for TTS initialization");
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Shuts down the TextToSpeech engine.
   */
  public synchronized void close() {
    if (textToSpeech != null) {
      textToSpeech.shutdown();
      textToSpeech = null;
      initializationLatch = new CountDownLatch(1);
    }
  }

  private void waitForInitializationToFinish() throws InterruptedException {
    /* The TextToSpeech.OnInitListener will be called on the main thread. As a result,
       if we wait for initialization to finish on the main thread, it will hang indefinitely. */
    if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
      String errorMessage = "AndroidTextToSpeech used from Android Main Thread. This is not allowed.";
      RobotLog.ee(TAG, errorMessage);
      throw new RuntimeException(errorMessage);
    }
    initializationLatch.await();
  }
}
