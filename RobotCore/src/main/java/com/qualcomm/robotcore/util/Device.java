/*
 * Copyright (c) 2015 Qualcomm Technologies Inc
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * (subject to the limitations in the disclaimer below) provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of Qualcomm Technologies Inc nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS
 * SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.qualcomm.robotcore.util;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.Nullable;

import com.qualcomm.robotcore.hardware.configuration.LynxConstants;

import org.firstinspires.ftc.robotcore.internal.network.WifiUtil;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.SystemProperties;

/**
 * A few constants that help us detect which device we are running on
 */
@SuppressWarnings("WeakerAccess")
public final class Device {
  public final static String TAG = "Device";

  private final static boolean DISABLE_FALLBACK_SERIAL_NUMBER_RETRIEVAL = false;
  private final static String SERIAL_NUMBER_PROPERTY = "ro.serialno";
  private final static String SERIAL_NUMBER_RETRIEVAL_COMMAND = "getprop " + SERIAL_NUMBER_PROPERTY;

  public final static String MANUFACTURER_REV = "REV Robotics";
  public final static String MANUFACTURER_MOTOROLA = "motorola";
  public final static String MODEL_E5_PLAY = "moto e5 play";
  public final static String MODEL_E4 = "Moto E (4)";

  @Nullable private static String cachedSerialNumberOrUnknown;

  public static boolean isMotorola() {
    return Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_MOTOROLA);
  }

  public static boolean isMotorolaE5Play() {
    return Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_MOTOROLA) && Build.MODEL.equalsIgnoreCase(MODEL_E5_PLAY);
  }

  public static boolean isMotorolaE4() {
    return Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_MOTOROLA) && Build.MODEL.equalsIgnoreCase(MODEL_E4);
  }

  public static boolean deviceHasBackButton() {
    UiModeManager uiManager = (UiModeManager) AppUtil.getDefContext().getSystemService(Context.UI_MODE_SERVICE);
    return uiManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_NORMAL;
  }

  /*
   * Motorola phones that have both 2.4 and 5Ghz bands will monopolize the radio for extended
   * periods of time when scanning the channels.  This can result in 2+ second radio blackouts
   * and peer disconnects.  Motorola engineering says that if a device sends packets at a fast
   * enough rate the scanning will not monopolize the radio for extended periods of time.
   *
   * Here we detect if a certain phone in use implements this aggressive form of scanning.
   *
   * See KeepAlive messaging for further context.
   */
  public static boolean phoneImplementsAggressiveWifiScanning() {
    return (isMotorola()) && (WifiUtil.is5GHzAvailable());
  }

  /**
   * Is it possible to remote channel change from a driver station to this device?
   */
  public static boolean wifiP2pRemoteChannelChangeWorks() {
    // TODO(Noah): Add support for the CH to the in-settings channel and get rid of this method
    return !isRevControlHub();
  }

  /**
   * Answers whether this is any sort of REV Control Hub
   */
  public static boolean isRevControlHub() {
    return LynxConstants.isRevControlHub();
  }

  // The methods for determining which type of Control Hub this is live in AndroidBoard.java, because
  // we should not be checking which Control Hub type we are at random points in the code.
  // If you need to change behavior depending on the Control Hub type, add an abstract method to AndroidBoard

  /**
   * Get the Android device's serial number, as defined by Build.SERIAL
   * Uses a fallback method if that API fails
   *
   * @throws AndroidSerialNumberNotFoundException if the serial number could not be determined
   */
  public static String getSerialNumber() throws AndroidSerialNumberNotFoundException {
    if (cachedSerialNumberOrUnknown == null) {
      try {
        cachedSerialNumberOrUnknown = internalGetSerialNumber();
      } catch (AndroidSerialNumberNotFoundException e) {
        cachedSerialNumberOrUnknown = Build.UNKNOWN;
      }
    }

    if (cachedSerialNumberOrUnknown.isEmpty() || cachedSerialNumberOrUnknown.equals(Build.UNKNOWN)) {
      throw new AndroidSerialNumberNotFoundException();
    }

    return cachedSerialNumberOrUnknown;
  }

  /**
   * Get the Android device's serial number, as defined by Build.SERIAL
   * Returns "unknown" if the serial number cannot be determined.
   */
  public static String getSerialNumberOrUnknown() {
    try {
      return getSerialNumber();
    } catch (AndroidSerialNumberNotFoundException e) {
      return Build.UNKNOWN;
    }
  }

  private static String internalGetSerialNumber() throws AndroidSerialNumberNotFoundException {
    String serialNumber;
    if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
      serialNumber = Build.SERIAL;
    } else {
      try {
        serialNumber = Build.getSerial();
      } catch (SecurityException e) {
        serialNumber = Build.UNKNOWN;
      }
    }

    if (!serialNumber.isEmpty() && !serialNumber.equals(Build.UNKNOWN)) return serialNumber;
    serialNumber = SystemProperties.get(SERIAL_NUMBER_PROPERTY, Build.UNKNOWN);
    if (!serialNumber.equals(Build.UNKNOWN)) return serialNumber;

    if (DISABLE_FALLBACK_SERIAL_NUMBER_RETRIEVAL) throw new AndroidSerialNumberNotFoundException();

    // If we can't get the serial number through the API, try the command line
    RobotLog.ww(TAG, "Failed to find Android serial number through Android API. Using fallback method.");
    RunShellCommand shell = new RunShellCommand();
    RunShellCommand.ProcessResult result = shell.run(SERIAL_NUMBER_RETRIEVAL_COMMAND);
    String output = result.getOutput().trim();

    if (result.getReturnCode() == 0 && !output.isEmpty() && !output.equals(Build.UNKNOWN)) {
      serialNumber = output;
    } else {
      throw new AndroidSerialNumberNotFoundException();
    }
    return serialNumber;
  }
}
