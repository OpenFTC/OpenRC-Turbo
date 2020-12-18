/*
Copyright (c) 2018 Noah Andrews

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Noah Andrews nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.robotcore.internal.hardware.android;

import androidx.annotation.Nullable;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.RunShellCommand;

import java.io.File;

public class Rev3328 extends AndroidBoard {
    private static final String TAG = "Rev3328";
    private static final int OS_1_1_0_VERSION_NUM = 3;
    private static final int OS_1_1_1_VERSION_NUM = 4;
    private static final int OS_1_1_2_BETA_VERSION_NUM = 5;
    private static final int OS_1_1_2_VERSION_NUM = 6;

    // Don't allow instantiation outside of our package
    protected Rev3328() {}

    /**
     * To convert 96boards pin numbers to raw GPIO numbers for the REV3328 board
     *
     *  1. Navigate to the final page of the hardware schematic
     *  2. Find the desired pin on CON8600
     *  3. See what GPIO address it maps to (e.g. GPIO1_C2)
     *  4. Use this formula: X1*32 + X2 + A*8 where:
     *          X1 is the first number (1),
     *          X2 is the second number (2),
     *          A is the zero-indexed numerical representation of the letter (C becomes 2)
     *     So, the raw GPIO number for GPIO1_C2 is 50.
     */

    // GPIO pins
    private static final DigitalChannel ANDROID_BOARD_IS_PRESENT_PIN =
            new GpioPin(50, true, GpioPin.Active.LOW, ANDROID_BOARD_IS_PRESENT_PIN_NAME);

    private static final DigitalChannel USER_BUTTON_PIN = new GpioPin(51, USER_BUTTON_PIN_NAME);

    private static final DigitalChannel PROGRAMMING_PIN =
            new GpioPin(66, false, GpioPin.Active.LOW, PROGRAMMING_PIN_NAME);

    private static final DigitalChannel LYNX_MODULE_RESET_PIN =
            new GpioPin(87, false, GpioPin.Active.LOW, LYNX_MODULE_RESET_PIN_NAME);

    // UART file
    private static final File UART_FILE = new File("/dev/ttyS1");

    // Public Methods

    @Override
    public String getDeviceType() {
        return "REV3328";
    }

    @Override
    public DigitalChannel getAndroidBoardIsPresentPin() {
        return ANDROID_BOARD_IS_PRESENT_PIN;
    }

    @Override
    public DigitalChannel getProgrammingPin() {
        return PROGRAMMING_PIN;
    }

    @Override
    public DigitalChannel getLynxModuleResetPin() {
        return LYNX_MODULE_RESET_PIN;
    }

    @Override
    public DigitalChannel getUserButtonPin() {
        return USER_BUTTON_PIN;
    }

    @Override
    public File getUartLocation() {
        return UART_FILE;
    }

    @Override public WifiDataRate getWifiApBeaconRate() {
        if (LynxConstants.getControlHubOsVersionNum() < OS_1_1_0_VERSION_NUM) {
            return WifiDataRate.CCK_1Mb; // OS versions prior to 1.1.0 used a 1Mb beacon rate
        }
        String rawBeaconRateString = new RunShellCommand().run("cat /sys/module/wlan/parameters/rev_beacon_rate").getOutput().trim();
        RealtekWifiDataRate rtkDataRate = null;
        try {
            rtkDataRate = RealtekWifiDataRate.fromRawValue(Integer.parseInt(rawBeaconRateString));
        } catch (RuntimeException e) {
            RobotLog.ee(TAG, e, "Error obtaining WiFi AP beacon rate");
        }
        if (rtkDataRate == null) {
            return WifiDataRate.UNKNOWN;
        }
        return rtkDataRate.wifiDataRate;
    }

    @Override public void setWifiApBeaconRate(WifiDataRate beaconRate) {
        if (LynxConstants.getControlHubOsVersionNum() < OS_1_1_1_VERSION_NUM) {
            RobotLog.ww(TAG, "Unable to set the WiFi AP beacon rate on Control Hub OS version" + LynxConstants.getControlHubOsVersion());
            RobotLog.ww(TAG, "Control Hub OS version 1.1.1 or higher is required for this feature.");
            return;
        }
        int rawBeaconRate = RealtekWifiDataRate.fromWifiDataRate(beaconRate).rawValue;
        new RunShellCommand().run("echo " + rawBeaconRate + " > /sys/module/wlan/parameters/rev_beacon_rate");
    }

    @Override public boolean supports5GhzAp() {
        return true;
    }

    @Override public boolean supports5GhzAutoSelection() {
        return LynxConstants.getControlHubOsVersionNum() >= OS_1_1_2_BETA_VERSION_NUM;
    }

    @Override public boolean supportsBulkNetworkSettings() {
        return LynxConstants.getControlHubOsVersionNum() >= OS_1_1_2_BETA_VERSION_NUM;
    }

    @Override public boolean supportsGetChannelInfoIntent() {
        return LynxConstants.getControlHubOsVersionNum() >= OS_1_1_2_BETA_VERSION_NUM;
    }

    @Override public boolean hasControlHubUpdater() {
        return true;
    }

    @Override public boolean hasRcAppWatchdog() {
        return LynxConstants.getControlHubOsVersionNum() >= OS_1_1_2_VERSION_NUM;
    }

    private enum RealtekWifiDataRate {
        // Values taken from https://github.com/REVrobotics/kernel-controlhub-android/blob/806e038dadebd9fd95b9604446d2ea440c9f86a0/drivers/net/wireless/rockchip_wlan/rtl8821cu/include/ieee80211.h#L702-L714
        RTK_CCK_1Mb(0x02, WifiDataRate.CCK_1Mb),
        RTK_CCK_2Mb(0x04, WifiDataRate.CCK_2Mb),
        RTK_CCK_5Mb(0x0B, WifiDataRate.CCK_5Mb),
        RTK_CCK_11Mb(0x16, WifiDataRate.CCK_11Mb),
        RTK_OFDM_6Mb(0x0C, WifiDataRate.OFDM_6Mb),
        RTK_OFDM_9Mb(0x12, WifiDataRate.OFDM_9Mb),
        RTK_OFDM_12Mb(0x18, WifiDataRate.OFDM_12Mb),
        RTK_OFDM_18Mb(0x24, WifiDataRate.OFDM_18Mb),
        RTK_OFDM_24Mb(0x30, WifiDataRate.OFDM_24Mb),
        RTK_OFDM_36Mb(0x48, WifiDataRate.OFDM_36Mb),
        RTK_OFDM_48Mb(0x60, WifiDataRate.OFDM_48Mb),
        RTK_OFDM_54Mb(0x6C, WifiDataRate.OFDM_54Mb);

        private final int rawValue;
        private final AndroidBoard.WifiDataRate wifiDataRate;
        RealtekWifiDataRate(int rawValue, AndroidBoard.WifiDataRate wifiDataRate) {
            this.rawValue = rawValue;
            this.wifiDataRate = wifiDataRate;
        }

        public static RealtekWifiDataRate fromWifiDataRate(AndroidBoard.WifiDataRate wifiDataRate) {
            for (RealtekWifiDataRate rtkWifiDataRate: values()) {
                if (rtkWifiDataRate.wifiDataRate == wifiDataRate) return rtkWifiDataRate;
            }
            throw new IllegalArgumentException("Unsupported data rate for Realtek WiFi: " + wifiDataRate);
        }

        public static @Nullable RealtekWifiDataRate fromRawValue(int rawValue) {
            for (RealtekWifiDataRate rtkWifiDataRate: values()) {
                if (rtkWifiDataRate.rawValue == rawValue) return rtkWifiDataRate;
            }
            return null;
        }
    }
}
