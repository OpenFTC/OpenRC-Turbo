// Copyright 2016 Google Inc.

package com.google.blocks.ftcrobotcontroller;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.FtcEventLoopHandler;
import com.qualcomm.ftccommon.ProgrammingModeController;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import com.qualcomm.robotcore.util.WebServer;
import org.firstinspires.ftc.robotserver.internal.programmingmode.ProgrammingModeLog;
import org.firstinspires.ftc.robotserver.internal.programmingmode.ProgrammingModeManager;
import org.firstinspires.ftc.robotserver.internal.webserver.PingDetails;

/**
 * Class responsible for starting and stopping programming mode without using a new activity. This
 * is used when starting/stopping programming mode is initiated from the driver station.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class ProgrammingModeControllerImpl implements ProgrammingModeController {

  public static final String TAG = "ProgrammingModeControllerImpl";

  private final Activity activity;
  private final TextView textRemoteProgrammingMode;
  private final ProgrammingModeManager programmingModeManager;
  private NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();

  public ProgrammingModeControllerImpl(Activity activity, TextView textRemoteProgrammingMode, ProgrammingModeManager programmingModeManager) {
    this.activity = activity;
    this.textRemoteProgrammingMode = textRemoteProgrammingMode;
    this.programmingModeManager = programmingModeManager;
  }

  @Override
  public boolean isActive() {
    return programmingModeManager != null;
  }

  @Override
  public void startProgrammingMode(final FtcEventLoopHandler ftcEventLoopHandler) {
    // Make a ProgrammingModeLog to send log message back to the driver station.
    ProgrammingModeLog programmingModeLog = new ProgrammingModeLog() {
      @Override
      public void addToLog(String msg) {
        networkConnectionHandler.sendCommand(
            new Command(CommandList.CMD_PROGRAMMING_MODE_LOG_NOTIFICATION, msg));
      }

      @Override
      public void ping(PingDetails pingDetails) {
        String extra = pingDetails.toJson();
        networkConnectionHandler.sendCommand(
            new Command(CommandList.CMD_PROGRAMMING_MODE_PING_NOTIFICATION, extra));
      }
    };

    programmingModeManager.setProgrammingModeLog(programmingModeLog);

    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        textRemoteProgrammingMode.setText("Programming Mode is Active");
        textRemoteProgrammingMode.setVisibility(View.VISIBLE);
      }
    });

    // Send the connection information back to the driver station.
    WebServer webServer = programmingModeManager.getWebServer();
    if (webServer != null) {
      String extra = webServer.getConnectionInformation().toJson();
      networkConnectionHandler.sendCommand(
          new Command(CommandList.CMD_START_PROGRAMMING_MODE_RESP, extra));
    } else {
      RobotLog.ee(TAG, "webServer unexpectedly null");
    }
  }

  @Override
  public void stopProgrammingMode() {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        textRemoteProgrammingMode.setVisibility(View.INVISIBLE);
        textRemoteProgrammingMode.setText("");
      }
    });
  }
}
