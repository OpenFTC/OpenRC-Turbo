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
import org.tensorflow.lite.Interpreter;

/**
 * Subclass of TfodFrameManager that uses {@link Interpreter}.
 *
 * @author Vasu Agrawal
 * @author lizlooney@google.com (Liz Looney)
 */
class TfodFrameManager1 extends TfodFrameManager {

  private final List<Interpreter> interpreters = new ArrayList<>();

  // Output locations for the interpreters, so we're not constantly reallocating
  private final List<float[][][]> outputLocations = new ArrayList<>();
  private final List<float[][]> outputClasses = new ArrayList<>();
  private final List<float[][]> outputScores = new ArrayList<>();
  private final List<float[]> numDetections = new ArrayList<>();

  TfodFrameManager1(FrameGenerator frameGenerator, MappedByteBuffer modelData, List<String> labels,
      TfodParameters params, Zoom zoom, AnnotatedFrameCallback tfodCallback) {
    super(frameGenerator, labels, params, zoom, tfodCallback);

    // Create the interpreters.
    for (int i = 0; i < params.numExecutorThreads; i++) {
      interpreters.add(new Interpreter(modelData, params.numInterpreterThreads));
    }

    // Create the output arrays for the different interpreters.
    for (int i = 0; i < params.numExecutorThreads; i++) {
      outputLocations.add(new float[1][params.maxNumDetections][4]);
      outputClasses.add(new float[1][params.maxNumDetections]);
      outputScores.add(new float[1][params.maxNumDetections]);
      numDetections.add(new float[1]);
    }
  }

  @Override
  protected Runnable createTask(AnnotatedYuvRgbFrame annotatedFrame, int id,
      double zoomMagnification, double zoomAspectRatio,
      AnnotatedFrameCallback annotatedFrameCallback) {
    return new RecognizeImageRunnable1(
        annotatedFrame,
        cameraInformation,
        interpreters.get(id),
        params,
        zoomMagnification,
        zoomAspectRatio,
        labels,
        outputLocations.get(id),
        outputClasses.get(id),
        outputScores.get(id),
        numDetections.get(id),
        annotatedFrameCallback);
  }
}
