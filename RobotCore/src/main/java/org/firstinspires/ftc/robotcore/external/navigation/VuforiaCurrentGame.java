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
  private static final String ASSET_NAME = "PowerPlay";
  private static final String RED_AUDIENCE_WALL = "Red Audience Wall";
  private static final String RED_REAR_WALL = "Red Rear Wall";
  private static final String BLUE_AUDIENCE_WALL = "Blue Audience Wall";
  private static final String BLUE_REAR_WALL = "Blue Rear Wall";

  // The height of the center of the target image above the floor.
  private static final float mmTargetHeight = 6 * MM_PER_INCH;

  // Constants for perimeter targets
  private static final float halfField = 72 * MM_PER_INCH;
  private static final float halfTile = 12 * MM_PER_INCH;
  private static final float oneAndHalfTile = 36 * MM_PER_INCH;

  /**
   * The names of the trackables, ordered to match the Vuforia data files.
   */
  public static final String[] TRACKABLE_NAMES = {
    RED_AUDIENCE_WALL,
    RED_REAR_WALL,
    BLUE_AUDIENCE_WALL,
    BLUE_REAR_WALL,
  };
  private static final Map<String, OpenGLMatrix> LOCATIONS_ON_FIELD = new HashMap<>();
  static {
    // Set the position of each trackable object.
    setPositionOfTrackable(RED_AUDIENCE_WALL,  -halfField,  -oneAndHalfTile, mmTargetHeight, 90, 0,  90);
    setPositionOfTrackable(RED_REAR_WALL,       halfField,  -oneAndHalfTile, mmTargetHeight, 90, 0, -90);
    setPositionOfTrackable(BLUE_AUDIENCE_WALL, -halfField,   oneAndHalfTile, mmTargetHeight, 90, 0,  90);
    setPositionOfTrackable(BLUE_REAR_WALL,      halfField,   oneAndHalfTile, mmTargetHeight, 90, 0, -90);
  };

  private static void setPositionOfTrackable(String targetName, float dx, float dy, float dz, float rx, float ry, float rz) {
    LOCATIONS_ON_FIELD.put(targetName,
        OpenGLMatrix.translation(dx, dy, dz)
            .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, rx, ry, rz)));
  }

  public VuforiaCurrentGame() {
    super(ASSET_NAME, TRACKABLE_NAMES, LOCATIONS_ON_FIELD);
  }
}
