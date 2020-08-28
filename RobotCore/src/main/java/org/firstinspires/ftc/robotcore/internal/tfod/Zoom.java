/*
 * Copyright (C) 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.firstinspires.ftc.robotcore.internal.tfod;

import android.graphics.Rect;

class Zoom {
  double magnification;
  double aspectRatio;

  Zoom(double magnification, double aspectRatio) {
    validateArguments(magnification, aspectRatio);
    this.magnification = magnification;
    this.aspectRatio = aspectRatio;
  }

  static void validateArguments(double magnification, double aspectRatio) {
    if (magnification < 0.9999) {
      throw new IllegalArgumentException("magnification must be greater than or equal to 1.0");
    }
    if (aspectRatio <= 0.0001) {
      throw new IllegalArgumentException("aspectRatio must be greater than 0");
    }
  }

  boolean isZoomed() {
    return isZoomed(magnification);
  }

  static boolean isZoomed(double magnification) {
    return !areEqual(magnification, 1.0);
  }

  Rect getZoomArea(int frameWidth, int frameHeight) {
    return getZoomArea(magnification, aspectRatio, frameWidth, frameHeight);
  }

  static Rect getZoomArea(double magnification, double aspectRatio, int frameWidth, int frameHeight) {
    // TODO(lizlooney): Figure out what to do if the rectangle ends up wider or taller than the
    // frame. I think that might happen if the frame is rotated.
    double centerWidth = frameWidth / magnification;
    double centerHeight = centerWidth / aspectRatio;
    double left = (frameWidth - centerWidth) / 2;
    double top = (frameHeight - centerHeight) / 2;
    return new Rect(
        (int) Math.round(left), (int) Math.round(top),
        (int) Math.round(left + centerWidth), (int) Math.round(top + centerHeight));
  }

  static boolean areEqual(double a, double b) {
    return Math.abs(a - b) <= 0.0001;
  }
}
