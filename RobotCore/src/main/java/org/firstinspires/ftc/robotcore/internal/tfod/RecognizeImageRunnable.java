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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

/**
 * Run a single frame through a TensorFlow Lite object detector and return the recognitions.
 *
 * <p>This runnable is invoked as a series of tasks submitted to an ExecutorService by the frame
 * manager. This runnable performs all pre- and post-processing work required to transform a raw
 * image, directly out of a {@link org.firstinspires.ftc.robotcore.internal.tfod.FrameGenerator}
 * into a list of {@link Recognition}.
 *
 * @author Vasu Agrawal
 */
abstract class RecognizeImageRunnable implements Runnable {

  private static final String TAG = "RecognizeImageRunnable";

  protected final AnnotatedYuvRgbFrame annotatedFrame;
  protected final CameraInformation cameraInformation;
  protected final TfodParameters params;
  protected final double zoomMagnification;
  protected final double zoomAspectRatio;
  protected final List<String> labels;

  /**
   * Callback providing a way to return the list of recognitions to the frame manager.
   *
   * Note that the annotatedFrame returned through this callback will be the same one which was
   * originally given to this object, with the List of Recognitions filled in. Any modifications
   * to the returned annotatedFrame will be visible to all other objects which hold a reference
   * to the annotatedFrame.
   * */
  private final AnnotatedFrameCallback callback;

  protected RecognizeImageRunnable(
      AnnotatedYuvRgbFrame annotatedFrame,
      CameraInformation cameraInformation,
      TfodParameters params,
      double zoomMagnification,
      double zoomAspectRatio,
      List<String> labels,
      AnnotatedFrameCallback callback) {
    this.annotatedFrame = annotatedFrame;
    this.cameraInformation = cameraInformation;
    this.params = params;
    this.zoomMagnification = zoomMagnification;
    this.zoomAspectRatio = zoomAspectRatio;
    this.labels = labels;
    this.callback = callback;
  }

  protected abstract void processFrame();

  @Override
  public void run() {
    processFrame();

    // The recognitions need to be in sorted order, decreasing by confidence.
    Collections.sort(annotatedFrame.getRecognitions(), new Comparator<Recognition>() {
      @Override
      public int compare(Recognition a, Recognition b) {
        return Float.compare(b.getConfidence(), a.getConfidence());
      }
    });

    // Uncomment this if you want to make the detector take longer than it does, to be able to
    // better understand the timing characteristics of the rest of the system.

    //    try {
    //      TimeUnit.MILLISECONDS.sleep(750);
    //    } catch (InterruptedException e) {
    //      Log.e(TAG, "Interrupted while sleeping for fake reasons", e);
    //    }

    callback.onResult(annotatedFrame);
  }
}
