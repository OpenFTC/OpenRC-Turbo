/*
 * Copyright 2021 Google LLC
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

import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions;

/**
 * Subclass of TfodFrameManager that uses {@link ObjectDetector}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class TfodFrameManager2 extends TfodFrameManager {

  private final List<ObjectDetector> objectDetectors = new ArrayList<>();

  TfodFrameManager2(FrameGenerator frameGenerator, MappedByteBuffer modelData, List<String> labels,
      TfodParameters params, Zoom zoom, AnnotatedFrameCallback tfodCallback) {
    super(frameGenerator, labels, params, zoom, tfodCallback);

    // Create the ObjectDetectors.
    ObjectDetectorOptions options = ObjectDetectorOptions.builder()
        .setMaxResults(params.maxNumDetections)
        .setNumThreads(params.numInterpreterThreads)
        .build();
    for (int i = 0; i < params.numExecutorThreads; i++) {
      objectDetectors.add(ObjectDetector.createFromBufferAndOptions(modelData, options));
    }
  }

  @Override
  protected Runnable createTask(AnnotatedYuvRgbFrame annotatedFrame, int id,
      double zoomMagnification, double zoomAspectRatio,
      AnnotatedFrameCallback annotatedFrameCallback) {
    return new RecognizeImageRunnable2(
        annotatedFrame,
        cameraInformation,
        objectDetectors.get(id),
        params,
        zoomMagnification,
        zoomAspectRatio,
        labels,
        annotatedFrameCallback);
  }
}
