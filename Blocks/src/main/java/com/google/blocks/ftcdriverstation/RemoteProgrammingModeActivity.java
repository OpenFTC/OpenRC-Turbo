// Copyright 2016 Google Inc.

package com.google.blocks.ftcdriverstation;

import com.google.blocks.AbstractProgrammingModeActivity;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.RobocolDatagram;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable.RecvLoopCallback;
import org.firstinspires.ftc.robotcore.internal.webserver.RobotControllerWebInfo;
import org.firstinspires.ftc.robotserver.internal.webserver.PingDetails;

/**
 * Activity that runs on the driver station in order to start and stop ProgrammingModeActivity on
 * the robot controller.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class RemoteProgrammingModeActivity extends AbstractProgrammingModeActivity
    implements RecvLoopCallback
  {
  @Override public String getTag() { return this.getClass().getSimpleName(); }

  private static final boolean DEBUG = false;

  private NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();

  @Override
  public void onStart() {
    super.onStart();

    networkConnectionHandler.pushReceiveLoopCallback(this);

    String extra =
        getIntent().getStringExtra(LaunchActivityConstantsList.RC_WEB_INFO);
    updateDisplay(RobotControllerWebInfo.fromJson(extra));
  }

  @Override
  public void onPause() {
    super.onPause();

  }

  @Override protected void onStop() {
    super.onStop();
    networkConnectionHandler.removeReceiveLoopCallback(this);
    }

  // RecvLoopCallback methods

  @Override
  public CallbackResult commandEvent(Command command) {
    CallbackResult result = CallbackResult.NOT_HANDLED;
    try {
      String name = command.getName();
      String extra = command.getExtra();

      if (name.equals(CommandList.CMD_PROGRAMMING_MODE_LOG_NOTIFICATION)) {
        addMessageToTextViewLog(extra);
        result = CallbackResult.HANDLED;
      }

      if (name.equals(CommandList.CMD_PROGRAMMING_MODE_PING_NOTIFICATION)) {
        addPing(PingDetails.fromJson(extra));
        result = CallbackResult.HANDLED;
      }

    } catch (Exception e) {
      RobotLog.logStackTrace(e);
    }
    return result;
  }

  @Override
  public CallbackResult telemetryEvent(RobocolDatagram packet) {
    return CallbackResult.NOT_HANDLED;
  }

  @Override
  public CallbackResult reportGlobalError(String error, boolean recoverable) {
    return CallbackResult.NOT_HANDLED;
  }

  @Override
  public CallbackResult packetReceived(RobocolDatagram packet) {
    return CallbackResult.NOT_HANDLED;
  }

  @Override
  public CallbackResult peerDiscoveryEvent(RobocolDatagram packet) {
    return CallbackResult.NOT_HANDLED;
  }

  @Override
  public CallbackResult heartbeatEvent(RobocolDatagram packet, long tReceived) {
    return CallbackResult.NOT_HANDLED;
  }

  @Override
  public CallbackResult gamepadEvent(RobocolDatagram packet) {
    return CallbackResult.NOT_HANDLED;
  }

  @Override
  public CallbackResult emptyEvent(RobocolDatagram packet) {
    return CallbackResult.NOT_HANDLED;
  }
}
