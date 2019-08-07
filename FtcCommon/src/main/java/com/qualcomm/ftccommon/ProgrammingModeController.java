package com.qualcomm.ftccommon;

/**
 * Declares the interface for starting and stopping programming mode when requested from the driver
 * station.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface ProgrammingModeController {

  /**
   * Returns true if programming mode is active; false otherwise.
   */
  boolean isActive();

  /**
   * Starts programming mode.
   */
  void startProgrammingMode(FtcEventLoopHandler ftcEventLoopHandler);

  /**
   * Stops programming mode.
   */
  void stopProgrammingMode();
}
