/*
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

package com.google.blocks.ftcrobotcontroller.util;

/**
 * A class that provides constants related to the current game.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class CurrentGame {
  // TODO(lizlooney): This file should be updated when the FTC game changes.
  // Also, the following other files should be updated:
  // * lib/RobotCore/src/main/java/org/firstinspires/ftc/robotcore/external/navigation/VuforiaCurrentGame.java
  // * lib/RobotCore/src/main/java/org/firstinspires/ftc/robotcore/external/tfod/TfodCurrentGame.java
  // * lib/Blocks/src/main/assets/blocks/samples/ConceptVuforiaNavSwitchableCameras.blk
  // * lib/Blocks/src/main/assets/blocks/samples/ConceptVuforiaNavWebcam.blk
  // * lib/Blocks/src/main/assets/blocks/samples/ConceptVuforiaNav.blk
  // (In the Vuforia blocks samples, the TRACKABLE_NAME field values of
  // vuforiaCurrentGame_typedEnum_trackableName blocks should be updated.)


  // TFOD_CURRENT_GAME_NAME ends up in:
  // toolbox sub-category names in TensorFlow Object Detection
  // tooltip for TensorFlow Object Detection blocks
  public static final String TFOD_CURRENT_GAME_NAME = "Freight Frenzy";
  // TFOD_CURRENT_GAME_BLOCKS_FIRST_NAME ends up in the label shown on TensorFlow Object Detection
  // blocks.
  public static final String TFOD_CURRENT_GAME_BLOCKS_FIRST_NAME = "TensorFlowObjectDetectionFreightFrenzy";
  // TFOD_CURRENT_GAME_IDENTIFIER_FOR_FTCJAVA ends up in the java code generated for TensorFlow
  // Object Detection blocks. It must be a valid java identifier.
  public static final String TFOD_CURRENT_GAME_IDENTIFIER_FOR_FTCJAVA = "tfodFreightFrenzy";

  // VUFORIA_CURRENT_GAME_NAME ends up in:
  // toolbox sub-category names in Vuforia
  // tooltip for Vuforia blocks
  public static final String VUFORIA_CURRENT_GAME_NAME = "Freight Frenzy";
  // VUFORIA_CURRENT_GAME_BLOCKS_FIRST_NAME ends up in the label shown on Vuforia blocks.
  public static final String VUFORIA_CURRENT_GAME_BLOCKS_FIRST_NAME = "VuforiaFreightFrenzy";
  // VUFORIA_CURRENT_GAME_IDENTIFIER_FOR_FTCJAVA ends up in the java code generated for Vuforia
  // blocks. It must be a valid java identifier.
  public static final String VUFORIA_CURRENT_GAME_IDENTIFIER_FOR_FTCJAVA = "vuforiaFreightFrenzy";
}
