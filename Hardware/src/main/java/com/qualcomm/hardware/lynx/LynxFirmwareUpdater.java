/*
 * Copyright (c) 2016-2021 REV Robotics, Robert Atkinson
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
 * Neither the name of REV Robotics nor the names of its contributors may be used to
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
package com.qualcomm.hardware.lynx;

import com.qualcomm.hardware.R;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.usb.ftdi.RobotUsbDeviceFtdi;
import com.qualcomm.robotcore.util.ReadWriteFile;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.stellaris.FlashLoaderManager;
import org.firstinspires.ftc.robotcore.internal.stellaris.FlashLoaderProtocolException;
import org.firstinspires.ftc.robotcore.internal.system.AppAliveNotifier;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;

import java.util.concurrent.atomic.AtomicBoolean;

// package-private. This should ONLY be accessed via LynxUsbDeviceImpl
class LynxFirmwareUpdater {
    private static final String TAG = "LynxFirmwareUpdater";

    // For now, we only allow 1 firmware update at a time across ALL devices, so this is static
    private static final AtomicBoolean firmwareUpdateInProgress = new AtomicBoolean(false);

    private final LynxUsbDeviceImpl device;
    private final boolean isControlHub;

    public LynxFirmwareUpdater(LynxUsbDeviceImpl device) {
        this.device = device;
        this.isControlHub = device.getSerialNumber().isEmbedded();
    }

    public RobotCoreCommandList.LynxFirmwareUpdateResp updateFirmware(final RobotCoreCommandList.FWImage image, String requestId, Consumer<ProgressParameters> progressConsumer) {
        RobotCoreCommandList.LynxFirmwareUpdateResp result = new RobotCoreCommandList.LynxFirmwareUpdateResp();
        result.success = false;
        result.originatorId = requestId;

        RobotLog.vv(TAG, "updateFirmware() serialNumber=%s, fwimage=%s", device.getSerialNumber(), image.getName());

        byte[] firmwareImage = ReadWriteFile.readBytes(image);
        if (firmwareImage.length <= 0) {
            result.errorMessage = AppUtil.getDefContext().getString(R.string.lynxFirmwareFileEmpty);
            RobotLog.vv(TAG, "Firmware update file was empty");
            return result;
        }

        try {
            boolean firmwareUpdateAllowed = firmwareUpdateInProgress.compareAndSet(false, true);
            if (!firmwareUpdateAllowed) {
                result.errorMessage = AppUtil.getDefContext().getString(R.string.lynxFirmwareUpdateAlreadyInProgress);
                RobotLog.vv(TAG, "Cannot update firmware: a firmware update is already in progress");
                return result;
            }

            RobotLog.vv(TAG, "disengaging lynx usb device %s", device.getSerialNumber());
            device.disengage();

            // Try the update a few times, in the hope of mitigating transient errors. Each time
            // we reset the hub and toggle it to enter programming mode, so it will at least
            // pay attention to us and try to cooperate, even after a failed update.
            int cRetryFirmwareUpdate = 2;
            for (int i = 0; i < cRetryFirmwareUpdate; i++) {
                RobotLog.vv(TAG, "trying firmware update: count=%d", i);
                AppAliveNotifier.getInstance().notifyAppAlive(); // be sure not to trip the CH OS watchdog
                if (updateFirmwareOnce(firmwareImage, progressConsumer)) {
                    result.success = true;
                    break;
                }
            }
        } catch (RuntimeException e) {
            RobotLog.ee(TAG, e, "RuntimeException in updateLynxFirmware()");
        } finally {
            RobotLog.vv(TAG, "reengaging lynx usb device %s", device.getSerialNumber());
            device.engage();
            firmwareUpdateInProgress.set(false);
        }
        return result;
    }

    private boolean updateFirmwareOnce(byte[] firmwareImage, Consumer<ProgressParameters> progressConsumer) {
        boolean success = true;
        if (enterFirmwareUpdateMode()) {
            FlashLoaderManager manager = new FlashLoaderManager(device.getRobotUsbDevice(), firmwareImage);
            try {
                manager.updateFirmware(progressConsumer);
            } catch (InterruptedException e) {
                success = false;
                Thread.currentThread().interrupt();
                RobotLog.ee(TAG, "interrupt while updating firmware: serial=%s", device.getSerialNumber());
            } catch (FlashLoaderProtocolException e) {
                success = false;
                RobotLog.ee(TAG, e, "exception while updating firmware: serial=%s", device.getSerialNumber());
            }
        } else {
            RobotLog.ee(TAG, "failed to enter firmware update mode");
            success = false;
        }
        return success;
    }

    private boolean enterFirmwareUpdateMode() {
        boolean result = false;
        if (isControlHub) {
            RobotLog.vv(TAG, "putting embedded lynx into firmware update mode");
            result = enterFirmwareUpdateModeControlHub();
        } else {
            RobotLog.vv(TAG, "putting lynx(serial=%s) into firmware update mode", device.getSerialNumber());
            result = enterFirmwareUpdateModeUSB();
        }

        // Sleep a bit to give the Lynx module time to enter bootloader. Actual time spent is a wild guess.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result;
    }

    private boolean enterFirmwareUpdateModeUSB() {
        RobotLog.vv(LynxModule.TAG, "enterFirmwareUpdateModeUSB() serial=%s", device.getSerialNumber());

        if (!LynxConstants.isEmbeddedSerialNumber(device.getSerialNumber())) {
            RobotUsbDeviceFtdi deviceFtdi = LynxUsbDeviceImpl.accessCBus(device.getRobotUsbDevice());
            if (deviceFtdi != null) {
                try {
                    int msDelay = LynxUsbDeviceImpl.msCbusWiggle;

                    // Initialize with both lines deasserted
                    deviceFtdi.cbus_setup(LynxUsbDeviceImpl.cbusMask, LynxUsbDeviceImpl.cbusNeitherAsserted);
                    Thread.sleep(msDelay);

                    // Assert nProg
                    deviceFtdi.cbus_write(LynxUsbDeviceImpl.cbusProgAsserted);
                    Thread.sleep(msDelay);

                    // Assert nProg and nReset
                    deviceFtdi.cbus_write(LynxUsbDeviceImpl.cbusBothAsserted);
                    Thread.sleep(msDelay);

                    // Deassert nReset
                    deviceFtdi.cbus_write(LynxUsbDeviceImpl.cbusProgAsserted);
                    Thread.sleep(msDelay);

                    // Deassert nProg
                    deviceFtdi.cbus_write(LynxUsbDeviceImpl.cbusNeitherAsserted);
                    Thread.sleep(LynxUsbDeviceImpl.msResetRecovery);

                    return true;
                } catch (InterruptedException|RobotUsbException e) {
                    LynxUsbDeviceImpl.exceptionHandler.handleException(e);
                }
            } else {
                RobotLog.ee(TAG, "enterFirmwareUpdateModeUSB() can't access FTDI device");
            }
        } else {
            RobotLog.ee(TAG, "enterFirmwareUpdateModeUSB() issued on Control Hub's embedded Expansion Hub");
        }
        return false;
    }

    public boolean enterFirmwareUpdateModeControlHub() {
        RobotLog.vv(LynxModule.TAG, "enterFirmwareUpdateModeControlHub()");

        if (LynxConstants.isRevControlHub()) {
            try {
                int msDelay = LynxUsbDeviceImpl.msCbusWiggle;

                boolean prevState = AndroidBoard.getInstance().getAndroidBoardIsPresentPin().getState();
                RobotLog.vv(LynxModule.TAG, "fw update embedded usb device: isPresent: was=%s", prevState);

                // Assert Dragonboard presence to ensure we can manipulate the programming and reset lines
                if (!prevState) {
                    AndroidBoard.getInstance().getAndroidBoardIsPresentPin().setState(true);
                    Thread.sleep(msDelay);
                }

                // Assert programming
                AndroidBoard.getInstance().getProgrammingPin().setState(true);
                Thread.sleep(msDelay);

                // Assert reset
                AndroidBoard.getInstance().getLynxModuleResetPin().setState(true);
                Thread.sleep(msDelay);

                // Deassert reset
                AndroidBoard.getInstance().getLynxModuleResetPin().setState(false);
                Thread.sleep(msDelay);

                // Deassert programming
                AndroidBoard.getInstance().getProgrammingPin().setState(false);
                Thread.sleep(msDelay);

                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            RobotLog.ee(TAG, "enterFirmwareUpdateModeControlHub() issued on non-Control Hub");
        }
        return false;
    }
}
