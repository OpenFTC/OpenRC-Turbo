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
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * A class that provides JavaScript access to {@link Telemetry}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class TelemetryAccess extends Access {
  private final Telemetry telemetry;

  TelemetryAccess(BlocksOpMode blocksOpMode, String identifier, Telemetry telemetry) {
    super(blocksOpMode, identifier, "Telemetry");
    this.telemetry = telemetry;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void addNumericData(String key, double data) {
    startBlockExecution(BlockType.FUNCTION, ".addData");
    telemetry.addData(key, data);
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void addTextData(String key, String data) {
    startBlockExecution(BlockType.FUNCTION, ".addData");
    if (data != null) {
      telemetry.addData(key, data);
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void addObjectData(String key, Object data) {
    startBlockExecution(BlockType.FUNCTION, ".addData");
    // Avoid calling data.toString() in case data is null.
    telemetry.addData(key, "" + data);
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void update() {
    startBlockExecution(BlockType.FUNCTION, ".update");
    telemetry.update();
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void speakTextData(String data, String languageCode, String countryCode) {
    startBlockExecution(BlockType.FUNCTION, ".speak");
    if (data != null) {
      telemetry.speak(data, languageCode, countryCode);
    }
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void speakObjectData(Object data, String languageCode, String countryCode) {
    startBlockExecution(BlockType.FUNCTION, ".speak");
    // Avoid calling data.toString() in case data is null.
    telemetry.speak("" + data, languageCode, countryCode);
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setDisplayFormat(String displayFormatString) {
    startBlockExecution(BlockType.FUNCTION, ".setDisplayFormat");
    Telemetry.DisplayFormat displayFormat =
        checkArg(displayFormatString, Telemetry.DisplayFormat.class, "displayFormat");
    if (displayFormat != null) {
      telemetry.setDisplayFormat(displayFormat);
    }
  }
}
