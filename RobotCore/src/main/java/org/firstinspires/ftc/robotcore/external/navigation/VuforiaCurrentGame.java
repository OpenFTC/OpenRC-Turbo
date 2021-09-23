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

package org.firstinspires.ftc.robotcore.external.navigation;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;

import java.util.Map;
import java.util.HashMap;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;

/**
 * A class that provides simplified access to Vuforia for the current game.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class VuforiaCurrentGame extends VuforiaBase {
  // TODO(lizlooney): This file should be updated when the FTC game changes.
  private static final String ASSET_NAME = "FreightFrenzy";
  private static final String BLUE_STORAGE = "Blue Storage";
  private static final String BLUE_ALLIANCE_WALL = "Blue Alliance Wall";
  private static final String RED_STORAGE = "Red Storage";
  private static final String RED_ALLIANCE_WALL = "Red Alliance Wall";

  // The height of the center of the target image above the floor.
  private static final float mmTargetHeight = 6 * MM_PER_INCH;

  // Constants for perimeter targets
  private static final float halfField = 72 * MM_PER_INCH;
  private static final float quadField  = 36 * MM_PER_INCH;
  private static final float halfTile = 12 * MM_PER_INCH;
  private static final float oneAndHalfTile = 36 * MM_PER_INCH;

  /**
   * The names of the trackables, ordered to match the Vuforia data files.
   */
  public static final String[] TRACKABLE_NAMES = {
    BLUE_STORAGE,
    BLUE_ALLIANCE_WALL,
    RED_STORAGE,
    RED_ALLIANCE_WALL,
  };
  private static final Map<String, OpenGLMatrix> LOCATIONS_ON_FIELD = new HashMap<>();
  static {
    // Set the position of each trackable object.
    LOCATIONS_ON_FIELD.put(BLUE_STORAGE,
        OpenGLMatrix
            .translation(-halfField, oneAndHalfTile, mmTargetHeight)
            .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 90)));

    LOCATIONS_ON_FIELD.put(BLUE_ALLIANCE_WALL,
        OpenGLMatrix
            .translation(halfTile, halfField, mmTargetHeight)
            .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));
    LOCATIONS_ON_FIELD.put(RED_STORAGE,
        OpenGLMatrix
            .translation(-halfField, -oneAndHalfTile, mmTargetHeight)
            .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 90)));

    LOCATIONS_ON_FIELD.put(RED_ALLIANCE_WALL,
        OpenGLMatrix
            .translation(halfTile, -halfField, mmTargetHeight)
            .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

  };

  public VuforiaCurrentGame() {
    super(ASSET_NAME, TRACKABLE_NAMES, LOCATIONS_ON_FIELD);
  }
}
