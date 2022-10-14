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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.firstinspires.ftc.robotcore.internal.system.AppUtil.TFLITE_MODELS_DIR;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.function.Continuation;
import org.firstinspires.ftc.robotcore.external.function.ContinuationResult;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamServer;
import org.firstinspires.ftc.robotcore.external.tfod.CameraInformation;
import org.firstinspires.ftc.robotcore.external.tfod.FrameGenerator;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to convert object detection and tracking system into a simple interface.
 *
 * <p>TFObjectDetector makes it easy to detect and track objects in real time. After initialization,
 * clients simply call getRecognitions() as often as they wish to get the recognitions corresponding
 * to the most recent frame which has been processed. Recognitions contain information about the
 * location, class, and detection confidence of each particular object.
 *
 * <p>Advanced users may wish to tune the performance of the TFObjectDetector by changing parameters
 * away from the defaults in {@link Parameters}. Not all parameters will make a measurable impact
 * on performance.
 *
 * @author Vasu Agrawal
 * @author lizlooney@google.com (Liz Looney)
 */
public class TFObjectDetectorImpl implements TFObjectDetector, OpModeManagerNotifier.Notifications,
    ResultsCallback, Consumer<Bitmap> {

  private final AppUtil appUtil = AppUtil.getInstance();
  private final List<String> labels = new ArrayList<>();

  private final ClippingMargins clippingMargins = new ClippingMargins();
  private final Zoom zoom = new Zoom(1.0, 16.0/9.0);

  private final TfodParameters params;
  private final FrameGenerator frameGenerator;
  private final CameraInformation cameraInformation;

  private final ViewGroup imageViewParent;
  private ImageView imageView;
  private FrameLayout.LayoutParams imageViewLayoutParams;

  private final Object frameManagerLock = new Object();
  private TfodFrameManager frameManager;

  private final Object resultsLock = new Object();
  private Results results; // Do not access directly, use getResults or getUpdatedResults.
  private long lastReturnedFrameTime = 0;

  private final Object annotatedBitmapLock = new Object();
  private Bitmap annotatedBitmap;
  private Canvas annotatedBitmapCanvas;

  private final Object bitmapFrameLock = new Object();
  private Continuation<? extends Consumer<Bitmap>> bitmapContinuation;

  private OpModeManagerImpl opModeManager;
  private final AtomicBoolean shutdownDone = new AtomicBoolean();


  public TFObjectDetectorImpl(Parameters parameters, FrameGenerator frameGenerator) {

    this.params = makeTfodParameters(parameters);

    Activity activity = (parameters.activity != null)
        ? parameters.activity
        : appUtil.getRootActivity();
    opModeManager = OpModeManagerImpl.getOpModeManagerOfActivity(activity);
    if (opModeManager != null) {
      opModeManager.registerListener(this);
    }

    this.frameGenerator = frameGenerator;
    cameraInformation = frameGenerator.getCameraInformation();

    // Create image view if requested.
    ViewGroup imageViewParent = null;
    if (parameters.tfodMonitorViewParent != null) {
      imageViewParent = parameters.tfodMonitorViewParent;
    } else if (parameters.tfodMonitorViewIdParent != 0) {
      imageViewParent = (ViewGroup) activity.findViewById(parameters.tfodMonitorViewIdParent);
    }
    this.imageViewParent = imageViewParent;
    if (imageViewParent != null) {
      createImageView(activity);
    }

    CameraStreamServer.getInstance().setSource(this);

    // Initialize the results to something non-null.
    synchronized (resultsLock) {
      results = new Results(cameraInformation, System.nanoTime(), new ArrayList<LabeledObject>());
    }
  }

  private static TfodParameters makeTfodParameters(Parameters parameters) {
    return new TfodParameters.Builder(parameters.isModelQuantized, parameters.inputSize)
        .tensorFlow2(parameters.isModelTensorFlow2)
        .useObjectTracker(parameters.useObjectTracker)
        .numInterpreterThreads(parameters.numInterpreterThreads)
        .numExecutorThreads(parameters.numExecutorThreads)
        .maxNumDetections(parameters.maxNumDetections)
        .minResultConfidence(parameters.minResultConfidence)
        .trackerMaxOverlap(parameters.trackerMaxOverlap)
        .trackerMinSize(parameters.trackerMinSize)
        .trackerMarginalCorrelation(parameters.trackerMarginalCorrelation)
        .trackerMinCorrelation(parameters.trackerMinCorrelation)
        .build();
  }

  private void createImageView(final Activity activity) {
    appUtil.synchronousRunOnUiThread(new Runnable() {
      @Override
      public void run() {
        imageView = new ImageView(activity);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        imageViewLayoutParams = null;
        if (cameraInformation.rotation != 0) {
          imageView.setRotation(360 - cameraInformation.rotation);
        }
        imageViewParent.addView(imageView);
        imageViewParent.setVisibility(VISIBLE);
      }
    });
  }

  @Override
  public void loadModelFromAsset(String assetName, String... labels) {
    try {
      AssetManager assetManager = AppUtil.getDefContext().getAssets();
      AssetFileDescriptor afd = assetManager.openFd(assetName);
      try (FileInputStream fis = afd.createInputStream()) {
        initialize(fis, afd.getStartOffset(), afd.getDeclaredLength(), labels);
      }
    } catch (IOException e) {
      throw new RuntimeException("TFObjectDetector loadModelFromAsset failed", e);
    }
  }

  @Override
  public void loadModelFromFile(String fileName, String... labels) {
    try {
      File file = new File(TFLITE_MODELS_DIR, fileName);
      if (file.exists()) {
        fileName = file.getAbsolutePath();
      } else {
        file = new File(fileName);
      }

      try (FileInputStream fis = new FileInputStream(fileName)) {
        initialize(fis, 0, file.length(), labels);
      }
    } catch (IOException e) {
      throw new RuntimeException("TFObjectDetector loadModelFromFile failed", e);
    }
  }

  private void initialize(FileInputStream fileInputStream, long startOffset, long declaredLength,
      String... labels) throws IOException {
    // Load the model.
    MappedByteBuffer modelData = fileInputStream.getChannel()
        .map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

    // Add the given labels to our List.
    for (String label : labels) {
      this.labels.add(label);
    }

    // Create a TfodFrameManager, which handles feeding tasks to the executor. Each task consists
    // of processing a single camera frame, passing it through the model, and returning a list of
    // recognitions.
    synchronized (frameManagerLock) {
      frameManager = newTfodFrameManager(modelData);
      // Attach our frame consumer from the frame generator.
      frameGenerator.setFrameConsumer(frameManager.getFrameConsumer());
    }
  }

  private TfodFrameManager newTfodFrameManager(MappedByteBuffer modelData) {
    ResultsCallback resultsCallback = this;
    Consumer<Bitmap> annotatedFrameCallback = this;
    return params.isModelTensorFlow2
        ? new TfodFrameManager2(modelData, labels, params, cameraInformation, clippingMargins, zoom, resultsCallback, annotatedFrameCallback)
        : new TfodFrameManager1(modelData, labels, params, cameraInformation, clippingMargins, zoom, resultsCallback, annotatedFrameCallback);
  }

  // ResultsCallback

  @Override
  public void onResults(Results results) {
    synchronized (resultsLock) {
      this.results = results;
    }
  }

  // Consumer<Bitmap> (annotated frame callback)

  @Override
  public void accept(Bitmap bitmap) {
    synchronized (annotatedBitmapLock) {
      if (annotatedBitmap == null) {
        // Create a bitmap that we can use on the UI thread.
        annotatedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        annotatedBitmapCanvas = new Canvas(annotatedBitmap);
      }
      annotatedBitmapCanvas.drawBitmap(bitmap, 0, 0, null);
    }

    synchronized (bitmapFrameLock) {
      if (bitmapContinuation != null) {
        bitmapContinuation.dispatch(new ContinuationResult<Consumer<Bitmap>>() {
          @Override
          public void handle(Consumer<Bitmap> bitmapConsumer) {
            bitmapConsumer.accept(annotatedBitmap);
          }
        });
        bitmapContinuation = null;
      }
    }

    if (imageViewParent != null) {
      appUtil.synchronousRunOnUiThread(new Runnable() {
        @Override
        public void run() {
          synchronized (annotatedBitmapLock) {
            if (imageViewLayoutParams == null) {
              double width = annotatedBitmap.getWidth();
              double height = annotatedBitmap.getHeight();
              if (cameraInformation.rotation % 180 != 0) {
                double swap = width;
                width = height;
                height = swap;
              }
              double scale = Math.min(imageView.getWidth() / width, imageView.getHeight() / height);
              width *= scale;
              height *= scale;

              if (cameraInformation.rotation % 180 != 0) {
                double swap = width;
                width = height;
                height = swap;
              }

              imageViewLayoutParams = new FrameLayout.LayoutParams((int) width, (int) height, Gravity.CENTER);
              imageView.setLayoutParams(imageViewLayoutParams);
            }
            imageView.setImageBitmap(annotatedBitmap);
            imageView.invalidate();
          }
        }
      });
    }
  }

  // CameraStreamSource

  @Override
  public void getFrameBitmap(Continuation<? extends Consumer<Bitmap>> continuation) {
    synchronized (bitmapFrameLock) {
      bitmapContinuation = continuation;
    }
  }

  /**
   * Activates this TFObjectDetector so it starts recognizing objects.
   */
  @Override
  public void activate() {
    synchronized (frameManagerLock) {
      if (frameManager != null) {
        frameManager.activate();
      }
    }
  }

  /**
   * Deactivates this TFObjectDetector so it stops recognizing objects.
   */
  @Override
  public void deactivate() {
    synchronized (frameManagerLock) {
      if (frameManager != null) {
        frameManager.deactivate();
      }
    }
  }

  @Override
  public void setClippingMargins(int left, int top, int right, int bottom) {
    synchronized (clippingMargins) {
      switch (cameraInformation.rotation) {
        default:
          throw new IllegalStateException("rotation must be 0, 90, 180, or 270.");
        case 0:
          clippingMargins.left = left;
          clippingMargins.top = top;
          clippingMargins.right = right;
          clippingMargins.bottom = bottom;
          break;
        case 90:
          clippingMargins.left = bottom;
          clippingMargins.top = left;
          clippingMargins.right = top;
          clippingMargins.bottom = right;
          break;
        case 180:
          clippingMargins.left = right;
          clippingMargins.top = bottom;
          clippingMargins.right = left;
          clippingMargins.bottom = top;
          break;
        case 270:
          clippingMargins.left = top;
          clippingMargins.top = right;
          clippingMargins.right = bottom;
          clippingMargins.bottom = left;
          break;
      }
    }
  }

  @Override
  public void setZoom(double magnification, double aspectRatio) {
    Zoom.validateArguments(magnification, aspectRatio);
    synchronized (zoom) {
      zoom.magnification = magnification;
      zoom.aspectRatio = aspectRatio;
    }
  }

  private @NonNull Results getResults() {
    synchronized (resultsLock) {
      return results;
    }
  }

  /**
   * Return a new Results or null if a new one isn't available.
   *
   * If a new frame has arrived since the last time this method was called, it will be returned.
   * Otherwise, null will be returned.
   *
   * Note that this method still takes a lock internally, and thus calling this method too
   * frequently may degrade performance of the detector.
   *
   * @return A new frame if one is available, null otherwise.
   */
  private Results getUpdatedResults() {
    synchronized (resultsLock) {
      // Can do this safely because we know the results can never be null after the
      // constructor has happened.
      if (results.getFrameTimeNanos() > lastReturnedFrameTime) {
        lastReturnedFrameTime = results.getFrameTimeNanos();
        return results;
      }
    }

    return null;
  }

  private static List<Recognition> makeRecognitionsList(@NonNull Results results) {
    return new ArrayList<Recognition>(results.getRecognitions());
  }

  @Override
  public List<Recognition> getUpdatedRecognitions() {
    Results updatedResults = getUpdatedResults();
    if (updatedResults == null) {
      return null;
    }
    return makeRecognitionsList(updatedResults);
  }

  @Override
  public List<Recognition> getRecognitions() {
    return makeRecognitionsList(getResults());
  }

  /**
   * Perform whatever cleanup is necessary to release all acquired resources.
   */
  @Override
  public void shutdown() {
    if (shutdownDone.getAndSet(true)) {
      return;
    }

    // Detach our frame consumer from the frame generator.
    frameGenerator.setFrameConsumer(null);

    synchronized (frameManagerLock) {
      if (frameManager != null) {
        frameManager.deactivate();
        frameManager.shutdown();
      }
    }

    // If we've been asked to draw to the screen, remove the image view.
    if (imageViewParent != null) {
      appUtil.synchronousRunOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (imageView != null) {
            imageViewParent.removeView(imageView);
          }
          imageViewParent.setVisibility(GONE);
        }
      });
    }
  }

  //  OpModeManagerNotifier.Notifications
  @Override
  public void onOpModePreInit(OpMode opMode) {
  }

  @Override
  public void onOpModePreStart(OpMode opMode) {
  }

  @Override
  public void onOpModePostStop(OpMode opMode) {
    shutdown();

    if (opModeManager != null) {
      opModeManager.unregisterListener(this);
      opModeManager = null;
    }
  }
}
