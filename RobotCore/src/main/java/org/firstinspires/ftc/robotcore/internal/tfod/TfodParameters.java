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

// TODO(vasuagrawal): Verify that it's easy to see default values for these parameters.

/**
 * Parameters which can be used to configure the performance and behavior of TFObjectDetector.
 *
 * <p>This class is intended to be instantiated through the Builder object, which helps provide
 * reasonable defaults for all of the parameter values. Note that changing the parameter values away
 * from the defaults may in some cases make little to no performance impact, while others may
 * dramatically increase the CPU load of the system.
 *
 * @author Vasu Agrawal
 * @author lizlooney@google.com (Liz Looney)
 */
public class TfodParameters {

  /**
   * Whether the model is a TensorFlow2 model.
   */
  public final boolean isModelTensorFlow2;

  /** Whether the model is quantized or not. True indicates the model is quantized. */
  public final boolean isModelQuantized;

  /**
   * The size in pixels of images input to the network, assuming the input is a square.
   *
   * <p>For example, if the network input size is 300 x 300, this parameter would be 300.
   */
  public final int inputSize;

  /**
   * The maximum number of detections the network will return.
   *
   * <p>This is a parameter set by TOCO, and must be the same one used when exporting the .tflite
   * file. Using a different parameter than that one will result in an error.
   */
  public final int maxNumDetections;

  /**
   * The number of threads to allow each individual TensorFlow Lite interpreter to have.
   *
   * <p>Each interpreter can potentially parallelize its execution across multiple threads. Some
   * models will offer better scaling to multiple threads than others. The default model does not
   * scale linearly, so you are better off increasing numExecutorThreads.
   */
  public final int numInterpreterThreads;

  /**
   * The number of individual worker threads to use.
   *
   * <p>In order to reduce latency, this library offers the ability to create multiple separate
   * interpreters, each of which can process a frame independently. This allows a form of
   * pipelining. While the processing time for each frame remain the same, more frames will be
   * processed overall.
   */
  public final int numExecutorThreads;

  /**
   * Minimum confidence at which to keep detections.
   *
   * <p>Anything higher than about 0.05 or so will filter out all of the extra detections that the
   * network needs to produce so that maxNumDetections are being produced for every frame. This
   * should usually match the threshold that was used at training time to determine a positive
   * result, if the loss function required such a threshold.
   */
  public final float minResultConfidence;

  // TODO(vasuagrawal): Figure out what these things are.
  // Not quite sure what these parameters do, but they were in the tracker and can be adjusted.
  // Any comments here are copied from the original source. See MultiBoxTracker.
  /**
   * Maximum percentage of a box that can be overlapped by another box at detection time.
   *
   * <p>Otherwise, the lower scored box (new or old) will be removed.
   */
  public final float trackerMaxOverlap;

  public final float trackerMinSize;
  /**
   * Allow replacement of the tracked box with new results if correlation has dropped below this.
   */
  public final float trackerMarginalCorrelation;
  /** Consider object to be lost if correlation falls below this threshold. */
  public final float trackerMinCorrelation;

  /**
   * Whether to use the tracker.
   *
   * <p> By default, the tracker is used to improve detection results. However, in the case
   * of static objects, or to use a different processing pipeline, you may want to disable the
   * tracker so that the only recognitions returned from the system are those from the network
   * itself.
   */
  public final boolean useObjectTracker;

  // Private constructor to force clients to use the Builder and get proper argument verification
  private TfodParameters(
      boolean isModelTensorFlow2,
      boolean isModelQuantized,
      int inputSize,
      int numInterpreterThreads,
      int numExecutorThreads,
      int maxNumDetections,
      float minResultConfidence,
      float trackerMaxOverlap,
      float trackerMinSize,
      float trackerMarginalCorrelation,
      float trackerMinCorrelation,
      boolean useObjectTracker) {
    this.isModelTensorFlow2 = isModelTensorFlow2;
    this.isModelQuantized = isModelQuantized;
    this.inputSize = inputSize;
    this.numInterpreterThreads = numInterpreterThreads;
    this.numExecutorThreads = numExecutorThreads;
    this.maxNumDetections = maxNumDetections;
    this.minResultConfidence = minResultConfidence;
    this.trackerMaxOverlap = trackerMaxOverlap;
    this.trackerMinSize = trackerMinSize;
    this.trackerMarginalCorrelation = trackerMarginalCorrelation;
    this.trackerMinCorrelation = trackerMinCorrelation;
    this.useObjectTracker = useObjectTracker;
  }

  public static class Builder {

    private boolean isModelTensorFlow2 = false;
    private boolean isModelQuantized = true;
    private int inputSize = 300; // px

    private int numInterpreterThreads = 1;
    @Deprecated
    private int numExecutorThreads = 2;

    private int maxNumDetections = 10;

    private float minResultConfidence = 0.4f;

    private float trackerMaxOverlap = 0.2f;
    private float trackerMinSize = 16.0f;
    private float trackerMarginalCorrelation = 0.75f;
    private float trackerMinCorrelation = 0.3f;
    private boolean useObjectTracker = true;

    /** Default constructor to use the model included in the library. */
    public Builder() {}

    /** Advanced constructor to provide a custom model with all required parameters. */
    public Builder(boolean isModelQuantized, int inputSize) {
      this.isModelQuantized = isModelQuantized;
      this.inputSize = inputSize;
    }

    public Builder tensorFlow2(boolean isModelTensorFlow2) {
      this.isModelTensorFlow2 = isModelTensorFlow2;
      return this;
    }

    public Builder numInterpreterThreads(int numInterpreterThreads) {
      if (numInterpreterThreads <= 0) {
        throw new IllegalArgumentException("Must have at least 1 thread per interpreter");
      }
      this.numInterpreterThreads = numInterpreterThreads;
      return this;
    }

    @Deprecated
    public Builder numExecutorThreads(int numExecutorThreads) {
      if (numExecutorThreads <= 0) {
        throw new IllegalArgumentException("Must have at least 1 executor worker thread");
      }
      this.numExecutorThreads = numExecutorThreads;
      return this;
    }

    public Builder maxNumDetections(int maxNumDetections) {
      if (maxNumDetections <= 0) {
        throw new IllegalArgumentException("maxNumDetections must be at least 1");
      }
      this.maxNumDetections = maxNumDetections;
      return this;
    }

    public Builder minResultConfidence(float minResultConfidence) {
      if (Float.isNaN(minResultConfidence)) {
        throw new IllegalArgumentException("minResultConfidence cannot be NaN");
      }
      this.minResultConfidence = minResultConfidence;
      return this;
    }

    public Builder trackerMaxOverlap(float trackerMaxOverlap) {
      this.trackerMaxOverlap = trackerMaxOverlap;
      return this;
    }

    public Builder trackerMinSize(float trackerMinSize) {
      this.trackerMinSize = trackerMinSize;
      return this;
    }

    public Builder trackerMarginalCorrelation(float trackerMarginalCorrelation) {
      this.trackerMarginalCorrelation = trackerMarginalCorrelation;
      return this;
    }

    public Builder trackerMinCorrelation(float trackerMinCorrelation) {
      this.trackerMinCorrelation = trackerMinCorrelation;
      return this;
    }

    public Builder useObjectTracker(boolean useObjectTracker) {
      this.useObjectTracker = useObjectTracker;
      return this;
    }

    public TfodParameters build() {
      return new TfodParameters(
          isModelTensorFlow2,
          isModelQuantized,
          inputSize,
          numInterpreterThreads,
          numExecutorThreads,
          maxNumDetections,
          minResultConfidence,
          trackerMaxOverlap,
          trackerMinSize,
          trackerMarginalCorrelation,
          trackerMinCorrelation,
          useObjectTracker);
    }
  }
}
