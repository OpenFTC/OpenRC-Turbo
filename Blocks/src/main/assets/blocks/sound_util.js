/**
 * @license
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

/**
 * @fileoverview Sound utilities.
 * @author lizlooney@google.com (Liz Looney)
 */

function playSound(i) {
  // files is declared in file_manager.js
  if (i >= 0 && i < files.length) {
    var soundName = files[i].name;
    playSoundFile(soundName);
  }
}

/**
 * Plays the sound with the given name.
 */
function playSoundFile(soundName) {
  if (window.location.protocol === 'http:' || window.location.protocol === 'https:') {
    // html/js is in a browser, loaded as an http:// URL.
    playSoundFileViaHttp(soundName);
  }
}

function playSoundFileViaHttp(soundName) {
  fetchFileMimeTypeViaHttp(soundName, function(mimeType, errorMessage) {
    if (mimeType.startsWith("audio/")) {
      fetchFileContent(soundName, function(base64Content, errorMessage) {
        if (base64Content) {
          var audio = new Audio('data:' + mimeType + ';base64,' + base64Content);
          audio.play()
        } else {
          console.log(errorMessage);
        }
      });
    } else {
      console.log(errorMessage);
    }
  });
}
