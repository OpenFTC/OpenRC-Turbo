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
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.text.TextUtils;
import com.google.ftcresearch.tfod.util.ImageUtils;
import com.google.ftcresearch.tfod.util.Size;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.tfod.CameraInformation;
import org.firstinspires.ftc.robotcore.external.tfod.FrameConsumer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.internal.tfod.LabeledObject.CoordinateSystem;

/**
 * Class to process frames and pass the most recent recognitions to a callback.
 *
 * @author Vasu Agrawal
 * @author lizlooney@google.com (Liz Looney)
 */
abstract class TfodFrameManager {

  private static final String TAG = "TfodFrameManager";

  protected static final Paint boxPaint = new Paint(); // Used to draw recognitions without tracker
  static {
    boxPaint.setColor(Color.RED);
    boxPaint.setStyle(Style.STROKE);
    boxPaint.setStrokeWidth(12.0f);
  }
  protected static final BorderedText borderedText = new BorderedText(60);

  // Parameters passed in to the constructor
  private MappedByteBuffer modelData;
  protected final List<String> labels;
  protected final TfodParameters params;
  protected final CameraInformation cameraInformation;
  private final ClippingMargins clippingMargins;
  private final Zoom zoom;
  private final ResultsCallback resultsCallback;
  private final Consumer<Bitmap> annotatedFrameCallback;

  private final MainPipeline mainPipeline;

  private final Object lastResultsFrameTimeNanosLock = new Object();
  private volatile long lastResultsFrameTimeNanos;

  private volatile boolean active;
  private volatile Results lastResultsPublished;

  protected TfodFrameManager(
      MappedByteBuffer modelData,
      List<String> labels,
      TfodParameters params,
      CameraInformation cameraInformation,
      ClippingMargins clippingMargins,
      Zoom zoom,
      ResultsCallback resultsCallback,
      Consumer<Bitmap> annotatedFrameCallback) {
    this.modelData = modelData;
    this.labels = labels;
    this.params = params;
    this.cameraInformation = cameraInformation;
    this.clippingMargins = clippingMargins;
    this.zoom = zoom;
    this.resultsCallback = resultsCallback;
    this.annotatedFrameCallback = annotatedFrameCallback;

    mainPipeline = new MainPipeline();
  }

  public FrameConsumer getFrameConsumer() {
    return mainPipeline;
  }

  protected abstract RecognizerPipeline createRecognizerPipeline(MappedByteBuffer modelData, ZoomHelper zoomHelper, Bitmap bitmap);

  protected void onResultsFromRecognizerPipeline(
      long frameTimeNanos, List<LabeledObject> labeledObjects,
      Bitmap bitmapFromRecognizerPipeline) {
    if (!active) {
      return;
    }
    synchronized (lastResultsFrameTimeNanosLock) {
      if (lastResultsFrameTimeNanos == 0 ||
          frameTimeNanos > lastResultsFrameTimeNanos) {
        lastResultsFrameTimeNanos = frameTimeNanos;
      } else {
        // Received out-of-order results.
        // We don't want to process / send this frame anywhere.
        return;
      }
    }

    if (mainPipeline.trackerPipeline != null) {
      // Send TFOD recognitions to the tracker pipeline.
      mainPipeline.trackerPipeline.onResultsFromRecognizerPipeline(
          frameTimeNanos, labeledObjects,
          bitmapFromRecognizerPipeline.copy(Bitmap.Config.ARGB_8888, false /* mutable */));
    } else {
      // No tracker. Publish the results now.
      publishResults(frameTimeNanos, labeledObjects);
    }
  }

  protected void onResultsFromTrackerPipeline(
      long frameTimeNanos, List<LabeledObject> labeledObjects) {
    if (!active) {
      return;
    }

    publishResults(frameTimeNanos, labeledObjects);
  }

  private void publishResults(long frameTimeNanos, List<LabeledObject> labeledObjects) {
    // The bounding boxes are in zoom area coordinates.  Map them to camera frame coordinates.
    List<LabeledObject> labeledObjectsInCamaraCoordinates = new ArrayList();
    for (LabeledObject labeledObject : labeledObjects) {
      labeledObjectsInCamaraCoordinates.add(labeledObject.convertToCamera());
    }
    Results results = new Results(cameraInformation, frameTimeNanos,
        labeledObjectsInCamaraCoordinates);
    lastResultsPublished = results;
    resultsCallback.onResults(results);
  }

  void activate() {
    active = true;
  }

  void deactivate() {
    active = false;
  }

  void shutdown() {
    if (!mainPipeline.executorService.isShutdown()) {
      mainPipeline.executorService.shutdown();
      try {
        boolean terminated = mainPipeline.executorService.awaitTermination(2, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private class MainPipeline implements FrameConsumer {
    private Bitmap bitmap;
    private Canvas canvas;
    private ClippingMarginsHelper clippingMarginsHelper;
    private ZoomHelper zoomHelper;
    private final Rect srcZoomRect = new Rect();
    private final Rect destZoomRect = new Rect();

    private final ExecutorService executorService;
    private final Queue<Integer> availableRecognizerIndex;
    private final Object[] lockForRecognizerPipeline;
    private final RecognizerPipeline[] recognizerPipeline;

    private TrackerPipeline trackerPipeline;

    MainPipeline() {
      executorService = Executors.newFixedThreadPool(params.numExecutorThreads);
      availableRecognizerIndex = new ConcurrentLinkedQueue<>();
      lockForRecognizerPipeline = new Object[params.numExecutorThreads];
      for (int i = 0; i < params.numExecutorThreads; i++) {
        availableRecognizerIndex.add(i);
        lockForRecognizerPipeline[i] = new Object();
      }
      recognizerPipeline = new RecognizerPipeline[params.numExecutorThreads];
    }

    @Override
    public void init(Bitmap bitmap) {
      this.bitmap = bitmap;
      canvas = new Canvas(bitmap);

      int width = bitmap.getWidth();
      int height = bitmap.getHeight();

      synchronized (clippingMargins) {
        clippingMarginsHelper = new ClippingMarginsHelper(
            clippingMargins.left, clippingMargins.top, clippingMargins.right, clippingMargins.bottom,
            width, height);
      }

      synchronized (zoom) {
        zoomHelper = new ZoomHelper(zoom.magnification, zoom.aspectRatio, width, height);
        srcZoomRect.set(zoomHelper.left(), zoomHelper.top(), zoomHelper.right(), zoomHelper.bottom());
        destZoomRect.set(0, 0, zoomHelper.right(), zoomHelper.bottom());
      }

      for (int i = 0; i < params.numExecutorThreads; i++) {
        synchronized (lockForRecognizerPipeline[i]) {
          recognizerPipeline[i] = createRecognizerPipeline(modelData, zoomHelper,
              Bitmap.createBitmap(zoomHelper.width(), zoomHelper.height(), Bitmap.Config.ARGB_8888));
        }
      }
      // After the RecognizerPipelines are created, we don't need modelData anymore.
      modelData = null;

      if (params.useObjectTracker) {
        trackerPipeline = new TrackerPipeline(zoomHelper,
            Bitmap.createBitmap(zoomHelper.width(), zoomHelper.height(), Bitmap.Config.ARGB_8888));
      }
    }

    @Override
    public void processFrame() {
      if (!active) {
        return;
      }

      final long frameTimeNanos = System.nanoTime();

      int width = bitmap.getWidth();
      int height = bitmap.getHeight();

      synchronized (clippingMargins) {
        if (clippingMarginsHelper.haveClippingMarginsChanged(clippingMargins)) {
          clippingMarginsHelper = new ClippingMarginsHelper(
              clippingMargins.left, clippingMargins.top, clippingMargins.right, clippingMargins.bottom,
              width, height);
        }
      }
      synchronized (zoom) {
        if (zoomHelper.hasZoomChanged(zoom)) {
          ZoomHelper oldZoomHelper = zoomHelper;
          zoomHelper = new ZoomHelper(zoom.magnification, zoom.aspectRatio, width, height);
          srcZoomRect.set(zoomHelper.left(), zoomHelper.top(), zoomHelper.right(), zoomHelper.bottom());
          destZoomRect.set(0, 0, zoomHelper.right(), zoomHelper.bottom());
        }
      }

      clippingMarginsHelper.fillClippingMargins(canvas);
      zoomHelper.blurAroundZoomArea(canvas);

      final Integer i = availableRecognizerIndex.poll();
      if (i != null) {
        synchronized (lockForRecognizerPipeline[i]) {
          executorService.submit(new Runnable() {
            @Override
            public void run() {
              synchronized (lockForRecognizerPipeline[i]) {
                if (!zoomHelper.equals(recognizerPipeline[i].zoomHelper)) {
                  // The zoom has changed. We need to create a new bitmap for this recognizer
                  // pipeline.
                  recognizerPipeline[i].onResized(zoomHelper,
                      Bitmap.createBitmap(zoomHelper.width(), zoomHelper.height(), Bitmap.Config.ARGB_8888));
                }
              }
              // Copy the zoom area onto the recognizerPipeline[i].zoomBitmap.
              Canvas zoomCanvas = new Canvas(recognizerPipeline[i].zoomBitmap);
              zoomCanvas.drawBitmap(bitmap, srcZoomRect, destZoomRect, null);
              recognizerPipeline[i].processFrame(frameTimeNanos);
              availableRecognizerIndex.add(i);
            }
          });
        }
      }

      if (trackerPipeline != null) {
        if (!zoomHelper.equals(trackerPipeline.zoomHelper)) {
          // The zoom has changed. We need to create a new bitmap for the tracker pipeline.
          trackerPipeline.onResized(zoomHelper,
              Bitmap.createBitmap(zoomHelper.width(), zoomHelper.height(), Bitmap.Config.ARGB_8888));
        }
        // Copy the zoom area onto the trackerPipeline.zoomBitmap.
        Canvas zoomCanvas = new Canvas(trackerPipeline.zoomBitmap);
        zoomCanvas.drawBitmap(bitmap, srcZoomRect, destZoomRect, null);
        trackerPipeline.processFrame(frameTimeNanos);
      }

      if (trackerPipeline != null) {
        canvas.save();
        canvas.translate(mainPipeline.zoomHelper.left(), mainPipeline.zoomHelper.top());
        trackerPipeline.draw(canvas);
        canvas.restore();
      } else {
        // Draw the last published results onto the canvas.
        Results results = lastResultsPublished;
        if (results != null) {
          for (Recognition recognition : results.getRecognitions()) {
            RectF location = ((RecognitionImpl) recognition).getLocation();
            canvas.drawRect(location, boxPaint);

            final String labelString =
                !TextUtils.isEmpty(recognition.getLabel())
                ? String.format("%s %.2f", recognition.getLabel(), recognition.getConfidence())
                : String.format("%.2f", recognition.getConfidence());
            borderedText.drawText(canvas, location.left, location.bottom, labelString);
          }
        }
      }

      annotatedFrameCallback.accept(bitmap);
    }
  }

  abstract class AbstractPipeline {
    ZoomHelper zoomHelper;
    Bitmap zoomBitmap;

    AbstractPipeline(ZoomHelper zoomHelper, Bitmap zoomBitmap) {
      this.zoomHelper = zoomHelper;
      this.zoomBitmap = zoomBitmap;
    }

    void onResized(ZoomHelper zoomHelper, Bitmap zoomBitmap) {
      this.zoomHelper = zoomHelper;
      this.zoomBitmap = zoomBitmap;
    }

    protected abstract void processFrame(long frameTimeNanos);
  }

  abstract class RecognizerPipeline extends AbstractPipeline {
    protected final Bitmap bitmapForTfod;
    private final Canvas canvasForTfod;
    private final Rect rectForTfod;
    protected Matrix tfodToZoomAreaMatrix;

    protected RecognizerPipeline(ZoomHelper zoomHelper, Bitmap zoomBitmap) {
      super(zoomHelper, zoomBitmap);

      bitmapForTfod = Bitmap.createBitmap(params.inputSize, params.inputSize, Bitmap.Config.ARGB_8888);
      canvasForTfod = new Canvas(bitmapForTfod);
      rectForTfod = new Rect(0, 0, params.inputSize, params.inputSize);
      tfodToZoomAreaMatrix = ImageUtils.transformBetweenImageSizes(
          new Size(params.inputSize, params.inputSize), new Size(zoomBitmap.getWidth(), zoomBitmap.getHeight()));
    }

    void onResized(ZoomHelper zoomHelper, Bitmap zoomBitmap) {
      super.onResized(zoomHelper, zoomBitmap);
      tfodToZoomAreaMatrix = ImageUtils.transformBetweenImageSizes(
          new Size(params.inputSize, params.inputSize), new Size(zoomBitmap.getWidth(), zoomBitmap.getHeight()));
    }

    protected void updateBitmapForTfod() {
      canvasForTfod.drawBitmap(zoomBitmap, null, rectForTfod, null /* paint */);
    }
  }

  private class TrackerPipeline extends AbstractPipeline {
    private final Size trackerFrameSize;
    private Size zoomAreaSize;
    private Matrix zoomAreaToTrackerMatrix;
    private Matrix trackerToZoomAreaMatrix;
    private final MultiBoxTracker tracker;
    private final Luminosity luminosity;
    private final byte[] luminosityArrayForTracking;
    private final byte[] luminosityArrayFromRecognizerPipeline;

    private TrackerPipeline(ZoomHelper zoomHelper, Bitmap zoomBitmap) {
      super(zoomHelper, zoomBitmap);
      zoomAreaSize = new Size(zoomBitmap.getWidth(), zoomBitmap.getHeight());
      trackerFrameSize = calculateTrackerFrameSize();
      zoomAreaToTrackerMatrix = ImageUtils.transformBetweenImageSizes(
          zoomAreaSize, trackerFrameSize);
      trackerToZoomAreaMatrix = ImageUtils.transformBetweenImageSizes(
          trackerFrameSize, zoomAreaSize);
      tracker = new MultiBoxTracker(params, trackerFrameSize);
      luminosity = new Luminosity(trackerFrameSize.width, trackerFrameSize.height);
      luminosityArrayForTracking = new byte[trackerFrameSize.width * trackerFrameSize.height];
      luminosityArrayFromRecognizerPipeline = new byte[trackerFrameSize.width * trackerFrameSize.height];
    }

    void onResized(ZoomHelper zoomHelper, Bitmap zoomBitmap) {
      super.onResized(zoomHelper, zoomBitmap);
      zoomAreaSize = new Size(zoomBitmap.getWidth(), zoomBitmap.getHeight());
      zoomAreaToTrackerMatrix = ImageUtils.transformBetweenImageSizes(
          zoomAreaSize, trackerFrameSize);
      trackerToZoomAreaMatrix = ImageUtils.transformBetweenImageSizes(
          trackerFrameSize, zoomAreaSize);
    }

    private Size calculateTrackerFrameSize() {
      // The tracker frames should be smaller than the bitmap, but have the same aspect ratio
      // as the bitmap and each dimension should be larger than the input size.
      int smaller = Math.min(zoomAreaSize.width, zoomAreaSize.height);
      int larger = Math.max(zoomAreaSize.width, zoomAreaSize.height);
      long dim1 = params.inputSize + 1;
      while (dim1 < smaller) {
        if (dim1 * larger % smaller == 0) {
          break;
        }
        dim1++;
      }
      long dim2 = dim1 * larger / smaller;
      return (smaller == zoomAreaSize.width)
          ? new Size((int) dim1, (int) dim2)
          : new Size((int) dim2, (int) dim1);
    }

    @Override
    protected void processFrame(long frameTimeNanos) {
      // Fill the luminosity array and submit it to the tracker.
      luminosity.bitmapToLuminosity(zoomBitmap, luminosityArrayForTracking);
      List<LabeledObject> newLabeledObjects =
          tracker.onFrame(frameTimeNanos, luminosityArrayForTracking);
      sendResults(frameTimeNanos, newLabeledObjects);
    }

    private void onResultsFromRecognizerPipeline(
        long frameTimeNanos, List<LabeledObject> labeledObjectsFromRecognizerPipeline,
        Bitmap bitmapFromRecognizerPipeline) {


      // Convert the results to the tracker frame coordinates.
      List<LabeledObject> labeledObjectsInTrackerCoordinates =
          transformLocations(labeledObjectsFromRecognizerPipeline, zoomAreaToTrackerMatrix,
          CoordinateSystem.ZOOM_AREA, CoordinateSystem.TRACKER);

      luminosity.bitmapToLuminosity(
          bitmapFromRecognizerPipeline, luminosityArrayFromRecognizerPipeline);

      // Sending the labeled objects and the luminosity array to the tracker.
      List<LabeledObject> newLabeledObjects = tracker.onResultsFromRecognizer(
          frameTimeNanos, labeledObjectsInTrackerCoordinates,
          luminosityArrayFromRecognizerPipeline);


      sendResults(frameTimeNanos, newLabeledObjects);
    }

    private void sendResults(long frameTimeNanos,
        List<LabeledObject> labeledObjectsInTrackerCoordinates) {
      // Map the labeledObjects back to zoom area coordinates.
      List<LabeledObject> labeledObjectsInZoomAreaCoordinates= transformLocations(
          labeledObjectsInTrackerCoordinates, trackerToZoomAreaMatrix,
          CoordinateSystem.TRACKER, CoordinateSystem.ZOOM_AREA);

      onResultsFromTrackerPipeline(frameTimeNanos, labeledObjectsInZoomAreaCoordinates);
    }

    void draw(Canvas canvas) {
      tracker.draw(canvas, borderedText, trackerToZoomAreaMatrix);
    }

    /** Transform all locations in the source list of labeled objects by m, returning a new list of
     ** new labeled objects */
    private List<LabeledObject> transformLocations(List<LabeledObject> source, Matrix m,
        CoordinateSystem expectedCoordinateSystem, CoordinateSystem newCoordinateSystem) {
      List<LabeledObject> output = new ArrayList<>();
      for (LabeledObject labeledObject : source) {
        labeledObject.checkCoordinateSystem(expectedCoordinateSystem);

        RectF newLocation = labeledObject.getLocation();
        m.mapRect(newLocation);

        // Check that this labeledObject is using the same zoomHelper as the TrackerPipeline.
        // (If the user changes the zoom while tfod is running, each recognizer is updated
        // separately, so a RecognizerPipeline may have a different zoomHelper than the
        // TrackerPipeline.)
        if (labeledObject.zoomHelper.equals(zoomHelper)) {
          output.add(new LabeledObject(labeledObject, newCoordinateSystem,
              newLocation.left, newLocation.top, newLocation.right, newLocation.bottom));
        } else {
          output.add(new LabeledObject(labeledObject.label, labeledObject.confidence,
              zoomHelper, newCoordinateSystem,
              newLocation.left + zoomHelper.left() - labeledObject.zoomHelper.left(),
              newLocation.top + zoomHelper.top() - labeledObject.zoomHelper.top(),
              newLocation.right + zoomHelper.left() - labeledObject.zoomHelper.left(),
              newLocation.bottom + zoomHelper.top() - labeledObject.zoomHelper.top()));
        }
      }
      return output;
    }
  }
}
