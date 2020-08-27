/**
 * @license
 * Copyright 2020 Google LLC
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

/**
 * @fileoverview Methods included when we run a BlocksOpMode.
 * @author lizlooney@google.com (Liz Looney)
 */

function callRunOpMode() {
  blocksOpMode.scriptStarting();
  try {
    // Call the runOpMode method in the generated javascript.
    runOpMode();
  } catch (e) {
    blocksOpMode.caughtException(String(e));
  }
  blocksOpMode.scriptFinished();
}

function telemetryAddTextData(key, data) {
  switch (typeof data) {
    case 'string':
      telemetry.addTextData(key, data);
      break;
    case 'object':
      if (data instanceof Array) {
        telemetry.addTextData(key, String(data));
      } else {
        telemetry.addObjectData(key, data);
      }
      break;
    default:
      telemetry.addTextData(key, String(data));
      break;
  }
}

function telemetrySpeak(data, languageCode, countryCode) {
  switch (typeof data) {
    case 'string':
      telemetry.speakTextData(data, languageCode, countryCode);
      break;
    case 'object':
      if (data instanceof Array) {
        telemetry.speakTextData(String(data), languageCode, countryCode);
      } else {
        telemetry.speakObjectData(data, languageCode, countryCode);
      }
      break;
    default:
      telemetry.speakTextData(String(data), languageCode, countryCode);
      break;
  }
}

function callJava(miscIdentifierForJavaScript, returnType, accessMethod, convertReturnValue,
    methodLookupString, ...rest) {
  var newRest = Array.prototype.slice.call(rest);
  for (var i = 0; i < newRest.length; i++) {
    if (typeof newRest[i] == 'number') {
      newRest[i] = String(newRest[i]);
    }
  }
  var newArgs = Array.prototype.slice.call(rest);
  newArgs.unshift(methodLookupString, JSON.stringify(newRest))
  while (newArgs.length < 23) {
    newArgs.push(null);
  }
  var result = miscIdentifierForJavaScript[accessMethod].apply(miscIdentifierForJavaScript, newArgs);
  switch (convertReturnValue) {
    case 'Number':
      result = Number(result);
      break;
  }
  return result;
}
