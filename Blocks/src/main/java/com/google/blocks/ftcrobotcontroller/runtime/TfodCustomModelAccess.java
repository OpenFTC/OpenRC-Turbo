/**
 * Copyright 2020 Google LLC
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

package com.google.blocks.ftcrobotcontroller.runtime;

import android.webkit.JavascriptInterface;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.tfod.TfodCustomModel;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;

/**
 * A class that provides JavaScript access to TensorFlow Object Detection for a custom model.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class TfodCustomModelAccess extends TfodBaseAccess<TfodCustomModel> {
  private String assetName;
  private String tfliteModelFilename;
  private String[] labels;

  TfodCustomModelAccess(BlocksOpMode blocksOpMode, String identifier, HardwareMap hardwareMap) {
    super(blocksOpMode, identifier, hardwareMap);
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setModelFromAsset(String assetName, String jsonLabels) {
    startBlockExecution(BlockType.FUNCTION, ".setModelFromAsset");
    String[] labels = SimpleGson.getInstance().fromJson(jsonLabels, String[].class);
    this.assetName = assetName;
    this.tfliteModelFilename = null;
    this.labels = labels;
  }

  @SuppressWarnings("unused")
  @JavascriptInterface
  public void setModelFromFile(String tfliteModelFilename, String jsonLabels) {
    startBlockExecution(BlockType.FUNCTION, ".setModelFromFile");
    String[] labels = SimpleGson.getInstance().fromJson(jsonLabels, String[].class);
    this.assetName = null;
    this.tfliteModelFilename = tfliteModelFilename;
    this.labels = labels;
  }

  protected TfodCustomModel createTfod() {
    TfodCustomModel tfodCustomModel = new TfodCustomModel();
    if (assetName != null) {
      tfodCustomModel.setModelFromAsset(assetName, labels);
    } else {
      tfodCustomModel.setModelFromFile(tfliteModelFilename, labels);
    }
    return tfodCustomModel;
  }
}
