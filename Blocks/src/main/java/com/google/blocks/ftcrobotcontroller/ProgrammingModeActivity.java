// Copyright 2016 Google Inc.

package com.google.blocks.ftcrobotcontroller;

import android.os.Bundle;

import com.google.blocks.AbstractProgrammingModeActivity;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;

import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.ui.LocalByRefIntentExtraHolder;
import org.firstinspires.ftc.robotserver.internal.programmingmode.ProgrammingModeLog;
import org.firstinspires.ftc.robotserver.internal.programmingmode.ProgrammingModeManager;
import org.firstinspires.ftc.robotserver.internal.webserver.PingDetails;

/**
 * Activity class for programming mode.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class ProgrammingModeActivity extends AbstractProgrammingModeActivity {

  @Override public String getTag() { return this.getClass().getSimpleName(); }

  protected ProgrammingModeManager programmingModeManager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LocalByRefIntentExtraHolder holder = getIntent().getParcelableExtra(LaunchActivityConstantsList.PROGRAMMING_MODE_ACTIVITY_PROGRAMMING_WEB_HANDLERS);
    programmingModeManager = (ProgrammingModeManager) holder.getTargetAndForget();
    Assert.assertNotNull(programmingModeManager);
  }

  @Override
  public void onResume() {
    super.onResume();

    ProgrammingModeLog programmingModeLog = new ProgrammingModeLog() {
      @Override
      public void addToLog(String msg) {
        addMessageToTextViewLog(msg);
      }

      @Override
      public void ping(PingDetails pingDetails) {
        addPing(pingDetails);
      }
    };

    programmingModeManager.setProgrammingModeLog(programmingModeLog);

    // Update the display.
    updateDisplay(programmingModeManager.getWebServer().getConnectionInformation());
  }
}
