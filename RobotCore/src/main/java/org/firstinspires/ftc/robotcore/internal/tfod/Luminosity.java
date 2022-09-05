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

package org.firstinspires.ftc.robotcore.internal.tfod;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import com.google.ftcresearch.tfod.util.ImageUtils;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

class Luminosity {
  private final Bitmap bitmap;
  private final Canvas canvas;
  private final Rect rect;
  private final IntBuffer intBuffer;
  private final ByteBuffer yuv420spByteBuffer;

  Luminosity(int width, int height) {
    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(bitmap);
    rect = new Rect(0, 0, width, height);
    intBuffer = IntBuffer.allocate(width * height);
    yuv420spByteBuffer = ByteBuffer.allocate(3 * width * height);
  }

  synchronized void bitmapToLuminosity(Bitmap src, byte[] dest) {
    canvas.drawBitmap(src, null, rect, null /* paint */);
    bitmap.copyPixelsToBuffer(intBuffer.duplicate());
    ImageUtils.convertBuffersARGB8888ToYuv420SP(intBuffer.duplicate(),
        yuv420spByteBuffer.duplicate(), rect.width(), rect.height());
    yuv420spByteBuffer.duplicate().get(dest, 0, rect.width() * rect.height());
  }
}
