// Copyright 2016 Google Inc.

package org.firstinspires.ftc.robotserver.internal.programmingmode;

import org.firstinspires.ftc.robotserver.internal.webserver.PingDetails;

/**
 * An interface for logging messages during programming mode.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface ProgrammingModeLog {
  /**
   * Called when a message should be added to the log.
   */
  void addToLog(String msg);

  /**
   * Called when a ProgrammingModeHandler receives a ping request.
   */
  void ping(PingDetails pingDetails);
}
