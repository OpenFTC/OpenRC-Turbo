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

package com.google.blocks.ftcrobotcontroller.runtime;

import static com.google.blocks.ftcrobotcontroller.util.CurrentGame.VUFORIA_CURRENT_GAME_BLOCKS_FIRST_NAME;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaCurrentGame;

/**
 * A class that provides JavaScript access to Vuforia for the current game.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class VuforiaCurrentGameAccess extends VuforiaBaseAccess<VuforiaCurrentGame> {
  VuforiaCurrentGameAccess(BlocksOpMode blocksOpMode, String identifier, HardwareMap hardwareMap) {
    super(blocksOpMode, identifier, hardwareMap, VUFORIA_CURRENT_GAME_BLOCKS_FIRST_NAME);
  }

  protected VuforiaCurrentGame createVuforia() {
    return new VuforiaCurrentGame();
  }
}
