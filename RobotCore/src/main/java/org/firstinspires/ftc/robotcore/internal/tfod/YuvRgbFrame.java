/*
 * Copyright (C) 2018 Google LLC
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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import androidx.annotation.NonNull;
import android.util.Log;
import com.google.ftcresearch.tfod.util.ImageUtils;
import com.google.ftcresearch.tfod.util.Size;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Threadsafe class to handle a frame.
 *
 * All data here is intended to be immutable by the user. However, for efficiency, the user is
 * given back the internal buffer, and is expected to not modify it. Furthermore, conversion
 * between data formats is done lazily, but is cached.
 *
 * @author Vasu Agrawal
 * @author Liz Looney
 */
class YuvRgbFrame {
  private static final String TAG = "YuvRgbFrame";

  private static final Paint paint = new Paint(); // Used to paint clipped edges.
  static {
    paint.setColor(Color.BLACK);
    paint.setStyle(Paint.Style.FILL);
  }

  private final long frameTimeNanos;

  private final String tag;
  private final Size size;
  private final Bitmap rgb565Bitmap;

  private final Object luminosityLock = new Object();
  private Size luminositySize;
  private byte[] luminosityArray;

  private final Object argb8888Lock = new Object();
  private int argb8888Size;
  private int[] argb8888Array;
  private Double argb8888ZoomMagnification;
  private Double argb8888ZoomAspectRatio;

  /**
   * Construct a YuvRgbFrame from RGB565 data.
   */
  YuvRgbFrame(long frameTimeNanos, Size size, @NonNull ByteBuffer rgb565ByteBuffer,
      ClippingMargins clippingMargins) {

    this.frameTimeNanos = frameTimeNanos;
    this.size = size;

    tag = "YuvRgbFrame." + frameTimeNanos;

    rgb565Bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.RGB_565);
    rgb565Bitmap.copyPixelsFromBuffer(rgb565ByteBuffer.duplicate());

    synchronized (clippingMargins) {
      if (clippingMargins.left > 0 || clippingMargins.top > 0 || clippingMargins.right > 0 || clippingMargins.bottom > 0) {
        Canvas canvas = new Canvas(rgb565Bitmap);
        canvas.clipRect(clippingMargins.left, clippingMargins.top,
            size.width - clippingMargins.right, size.height - clippingMargins.bottom,
            Region.Op.DIFFERENCE);
        canvas.drawPaint(paint);
      }
    }
  }

  /**
   * Get a bitmap that is safe to modify (e.g. draw on) as desired.
   */
  Bitmap getCopiedBitmap() {
    Timer timer = new Timer(tag);
    timer.start("YuvRgbFrame.getCopiedBitmap");
    Bitmap copiedBitmap = rgb565Bitmap.copy(rgb565Bitmap.getConfig(), true);
    timer.end();

    return copiedBitmap;
  }

  byte[] getLuminosityArray(Size newSize) {
    synchronized (luminosityLock) {
      if (newSize.equals(luminositySize)) {
        return luminosityArray;
      }
      if (luminositySize != null) {
        Log.w(TAG, "getLuminosityArray called for multiple sizes " + luminositySize + " and " + newSize);
      }

      Timer timer = new Timer(tag);
      timer.start("YuvRgbFrame.getLuminosityArray");

      // Scale rgb565Bitmap to the new size.
      Bitmap rgb565ScaledBitmap =
          Bitmap.createScaledBitmap(rgb565Bitmap, newSize.width, newSize.height, false);
      // Convert to ARGB_8888.
      Bitmap argb8888ScaledBitmap = rgb565ScaledBitmap.copy(Bitmap.Config.ARGB_8888, false);
      rgb565ScaledBitmap = null;

      // Get ARGB_8888 IntBuffer.
      IntBuffer argb8888ScaledIntBuffer = IntBuffer.allocate(newSize.width * newSize.height);
      argb8888ScaledBitmap.copyPixelsToBuffer(argb8888ScaledIntBuffer.duplicate());
      argb8888ScaledBitmap = null;

      // Convert to YUV
      ByteBuffer yuv420spByteBuffer = ByteBuffer.allocate(3 * newSize.width * newSize.height);
      ImageUtils.convertBuffersARGB8888ToYuv420SP(argb8888ScaledIntBuffer, yuv420spByteBuffer, newSize.width, newSize.height);
      argb8888ScaledIntBuffer = null;

      // Get luminosity bytes.
      luminosityArray = new byte[newSize.width * newSize.height];
      yuv420spByteBuffer.get(luminosityArray, 0, newSize.width * newSize.height);
      yuv420spByteBuffer = null;

      luminositySize = newSize;
      timer.end();
      return luminosityArray;
    }
  }

  int[] getArgb8888Array(int newSize, double zoomMagnification, double zoomAspectRatio) {
    synchronized (argb8888Lock) {
      if (newSize == argb8888Size &&
          argb8888ZoomMagnification != null &&
          Zoom.areEqual(zoomMagnification, argb8888ZoomMagnification) &&
          argb8888ZoomAspectRatio != null &&
          Zoom.areEqual(zoomAspectRatio, argb8888ZoomAspectRatio)) {
        return argb8888Array;
      }
      if (argb8888Size != 0) {
        Log.w(TAG, "getArgb8888Array called for multiple sizes " +
            argb8888Size + " and " + newSize);
      }
      if (argb8888ZoomMagnification != null) {
        Log.w(TAG, "getArgb8888Array called for multiple zoom magnifications " +
            argb8888ZoomMagnification + " and " + zoomMagnification);
      }
      if (argb8888ZoomAspectRatio != null) {
        Log.w(TAG, "getArgb8888Array called for multiple zoom aspect ratios " +
            argb8888ZoomAspectRatio + " and " + zoomAspectRatio);
      }

      Timer timer = new Timer(tag);
      timer.start("YuvRgbFrame.getArgb8888Array");

      Bitmap rgb565Bitmap;
      if (Zoom.isZoomed(zoomMagnification)) {
        Rect rect = Zoom.getZoomArea(zoomMagnification, zoomAspectRatio, size.width, size.height);
        rgb565Bitmap = Bitmap.createBitmap(this.rgb565Bitmap,
            rect.left, rect.top, rect.width(), rect.height());
      } else {
        rgb565Bitmap = this.rgb565Bitmap;
      }

      // Scale rgb565Bitmap to the new size.
      Bitmap rgb565ScaledBitmap = Bitmap.createScaledBitmap(rgb565Bitmap, newSize, newSize, false);
      rgb565Bitmap = null;

      // Convert to ARGB_8888.
      Bitmap argb8888ScaledBitmap = rgb565ScaledBitmap.copy(Bitmap.Config.ARGB_8888, false);
      rgb565ScaledBitmap = null;

      // Get ARGB_8888 array.
      int[] argb8888ScaledArray = new int[4 * newSize * newSize];
      argb8888ScaledBitmap.getPixels(argb8888ScaledArray, 0, newSize, 0, 0, newSize, newSize);
      argb8888ScaledBitmap = null;
      timer.end();

      // Save it in case we are asked for it again.
      argb8888Array = argb8888ScaledArray;
      argb8888Size = newSize;
      argb8888ZoomMagnification = zoomMagnification;
      argb8888ZoomAspectRatio = zoomAspectRatio;
      return argb8888Array;
    }
  }

  long getFrameTimeNanos() {
    return frameTimeNanos;
  }

  String getTag() {
    return tag;
  }

  Size getSize() {
    return size;
  }

  int getWidth() {
    return size.width;
  }

  int getHeight() {
    return size.height;
  }
}
