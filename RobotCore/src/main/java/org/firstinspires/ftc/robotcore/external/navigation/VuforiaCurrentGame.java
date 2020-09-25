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
  private static final String ASSET_NAME = "UltimateGoal";
  private static final String RED_TOWER_GOAL_TARGET = "Red Tower Goal Target";
  private static final String RED_ALLIANCE_TARGET = "Red Alliance Target";
  private static final String BLUE_TOWER_GOAL_TARGET = "Blue Tower Goal Target";
  private static final String BLUE_ALLIANCE_TARGET = "Blue Alliance Target";
  private static final String FRONT_WALL_TARGET = "Front Wall Target";

  // The height of the center of the target image above the floor.
  private static final float mmTargetHeight = 6 * MM_PER_INCH;

  // Constants for perimeter targets
  private static final float halfField = 72 * MM_PER_INCH;
  private static final float quadField  = 36 * MM_PER_INCH;

  /**
   * The names of the trackables, ordered to match the Vuforia data files.
   */
  public static final String[] TRACKABLE_NAMES = {
    BLUE_TOWER_GOAL_TARGET,
    RED_TOWER_GOAL_TARGET,
    RED_ALLIANCE_TARGET,
    BLUE_ALLIANCE_TARGET,
    FRONT_WALL_TARGET,
  };
  private static final Map<String, OpenGLMatrix> LOCATIONS_ON_FIELD = new HashMap<>();
  static {
    // Set the position of the perimeter targets with relation to origin (center of field).
    LOCATIONS_ON_FIELD.put(RED_ALLIANCE_TARGET,
        OpenGLMatrix
            .translation(0, -halfField, mmTargetHeight)
            .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

    LOCATIONS_ON_FIELD.put(BLUE_ALLIANCE_TARGET,
        OpenGLMatrix
            .translation(0, halfField, mmTargetHeight)
            .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));

    LOCATIONS_ON_FIELD.put(FRONT_WALL_TARGET,
        OpenGLMatrix
            .translation(-halfField, 0, mmTargetHeight)
            .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0 , 90)));


    // The tower goal targets are located a quarter field length from the ends of the back perimeter wall.
    LOCATIONS_ON_FIELD.put(BLUE_TOWER_GOAL_TARGET,
        OpenGLMatrix
            .translation(halfField, quadField, mmTargetHeight)
            .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0 , -90)));

    LOCATIONS_ON_FIELD.put(RED_TOWER_GOAL_TARGET,
        OpenGLMatrix
            .translation(halfField, -quadField, mmTargetHeight)
            .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));
  };

  public VuforiaCurrentGame() {
    super(ASSET_NAME, TRACKABLE_NAMES, LOCATIONS_ON_FIELD);
  }
}
