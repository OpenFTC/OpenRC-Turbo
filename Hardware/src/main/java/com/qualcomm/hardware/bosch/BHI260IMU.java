/*
Copyright (c) 2022 REV Robotics

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of REV Robotics nor the names of its contributors may be used to
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
package com.qualcomm.hardware.bosch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.hardware.R;
import com.qualcomm.hardware.lynx.LynxI2cDeviceSynch;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cWriteMultipleBytesCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cWriteReadMultipleBytesCommand;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDeviceWithParameters;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.I2cWarningManager;
import com.qualcomm.robotcore.hardware.TimestampedData;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.ReadWriteFile;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.TypeConversion;

import org.firstinspires.ftc.robotcore.external.navigation.Quaternion;
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard;
import org.firstinspires.ftc.robotcore.internal.system.AppAliveNotifier;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.SystemProperties;
import org.firstinspires.ftc.robotcore.internal.ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.internal.ui.UILocation;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.Locale;

@I2cDeviceType
@DeviceProperties(name = "@string/lynx_embedded_bhi260ap_imu_name", xmlTag = LynxConstants.EMBEDDED_BHI260AP_IMU_XML_TAG, description = "@string/lynx_embedded_imu_description", builtIn = true)
public class BHI260IMU extends I2cDeviceSynchDeviceWithParameters<I2cDeviceSynchSimple, BHI260IMU.InterimParameterClassDoNotUse> {
    // General constants
    private static final String TAG = "BHI260IMU";
    private static final boolean USE_FREEZE_PIN = true; // This should ALWAYS be true unless testing the freeze pin
    private static final I2cAddr I2C_ADDR = I2cAddr.create7bit(0x28);
    private static final double QUATERNION_SCALE_FACTOR = Math.pow(2, -14);
    private static final int PRODUCT_ID = 0x89;
    // It takes less than a second to boot from flash with no descriptor, but let's allow 2
    private static final int BOOT_FROM_FLASH_TIMEOUT_MS = 2000;
    // See W25Q64JW flash datasheet. In the worst-case scenario, the IMU will erase the flash chip
    // 4KiB at a time. Each 4KiB erase can take up to 400ms. We are erasing around 110-120 KiB of
    // the chip (to be conservative, let's use a value of 140 KiB), so erasing could theoretically
    // take as long as  take 14,000ms (not including any overhead). Of course, the typical 4KiB
    // erase time is just 45ms, so it's extremely unlikely to ever take that long.
    private static final int ERASE_FLASH_TIMEOUT_MS = 14_000;

    // Generic command constants
    private static final int COMMAND_HEADER_LENGTH = 4;
    private static final int MAX_SEND_I2C_BYTES_NO_REGISTER = LynxI2cWriteMultipleBytesCommand.cbPayloadLast;
    private static final int MAX_SEND_I2C_BYTES_WITH_REGISTER = MAX_SEND_I2C_BYTES_NO_REGISTER - 1;
    private static final int MAX_READ_I2C_BYTES = LynxI2cWriteReadMultipleBytesCommand.cbPayloadLast;
    private static final int COMMAND_ERROR_STATUS = 0x000F;

    // General firmware flashing constants
    private static final boolean DEBUG_FW_FLASHING = false;
    private static final int FW_START_ADDRESS = 0x1F84; // From the datasheet
    // ALWAYS update these two together
    private static final int FW_RESOURCE = R.raw.rev_bhi260_ap_fw_20;
    private static final int BUNDLED_FW_VERSION = 20;

    // Write Flash constants

    // For the Write Flash command, it's simpler if we think of the starting address as part of the
    // header instead of the payload, so that the "payload" only contains FW and padding bytes
    private static final int WRITE_FLASH_COMMAND_HEADER_LENGTH = 8;

    private static final int MAX_WRITE_FLASH_FIRMWARE_AND_PADDING_BYTES = MAX_SEND_I2C_BYTES_WITH_REGISTER - WRITE_FLASH_COMMAND_HEADER_LENGTH;
    private static final int MAX_WRITE_FLASH_FIRMWARE_BYTES = // Factors in the need for padding bytes
            MAX_WRITE_FLASH_FIRMWARE_AND_PADDING_BYTES - (MAX_WRITE_FLASH_FIRMWARE_AND_PADDING_BYTES % 4);
    // We'll likely need to increase this timeout if we gain the ability to write more I2C bytes at once
    private static final int WRITE_FLASH_RESPONSE_TIMEOUT_MS = 1_000;

    private int fwVersion = 0;

    public static boolean imuIsPresent(I2cDeviceSynchSimple deviceClient) {
        deviceClient.setI2cAddress(I2C_ADDR);

        RobotLog.vv(TAG, "Suppressing I2C warnings while we check for a BHI260AP IMU");
        I2cWarningManager.suppressNewProblemDeviceWarnings(true);
        try {
            // Check product identifier
            int productId = read8(deviceClient, Register.PRODUCT_IDENTIFIER);
            if (productId == PRODUCT_ID) {
                RobotLog.vv(TAG, "Found BHI260AP IMU");
                return true;
            } else {
                RobotLog.vv(TAG, "No BHI260AP IMU found");
                return false;
            }
        } finally {
            I2cWarningManager.suppressNewProblemDeviceWarnings(false);
        }
    }

    public static void flashFirmwareIfNecessary(I2cDeviceSynchSimple deviceClient) {
        deviceClient.setI2cAddress(I2C_ADDR);

        try {
            waitForHostInterface(deviceClient);
            checkForFlashPresence(deviceClient);
            boolean alreadyFlashed = waitForFlashVerification(deviceClient);

            int firmwareVersion;
            if (alreadyFlashed) {
                firmwareVersion = read16(deviceClient, Register.USER_VERSION);
            } else {
                firmwareVersion = 0;
            }

            RobotLog.vv(TAG, "flashFirmwareIfNecessary() alreadyFlashed=%b firmwareVersion=%d", alreadyFlashed, firmwareVersion);

            if (firmwareVersion != BUNDLED_FW_VERSION) {
                try {
                    // When we are flashing the firmware at the factory, we know that there is no
                    // wire connected to port 0, so it's safe to flash at 400 KHz.
                    if (SystemProperties.getBoolean("persist.bhi260.flash400khz", false)) {
                        RobotLog.vv(TAG, "Setting I2C bus speed to 400KHz for firmware flashing");
                        ((LynxI2cDeviceSynch) deviceClient).setBusSpeed(LynxI2cDeviceSynch.BusSpeed.FAST_400K);
                    } else {
                        RobotLog.vv(TAG, "Setting I2C bus speed to 100KHz for firmware flashing");
                        ((LynxI2cDeviceSynch) deviceClient).setBusSpeed(LynxI2cDeviceSynch.BusSpeed.STANDARD_100K);
                    }

                    // See flashing instructions in section 18.2 of the datasheet
                    RobotLog.ii(TAG, "Flashing IMU firmware version %d", BUNDLED_FW_VERSION);
                    ElapsedTime fwFlashTimer = new ElapsedTime();

                    ByteBuffer fwBuffer;
                    try {
                        fwBuffer = ByteBuffer.wrap(ReadWriteFile.readRawResourceBytesOrThrow(FW_RESOURCE));
                    } catch (IOException e) {
                        RobotLog.ee(TAG, e, "Failed to read IMU firmware file");
                        throw new InitException();
                    }

                    int fwLength = fwBuffer.remaining();

                    // Reset the IMU (which puts it in Host Boot mode), and wait for the command to
                    // be written (which will result in us waiting more than the 4 uS required by
                    // the datasheet)
                    RobotLog.vv(TAG, "Resetting IMU");
                    write8(deviceClient, Register.RESET_REQUEST, 0x1, I2cWaitControl.WRITTEN);

                    waitForHostInterface(deviceClient);
                    setStatusFifoToSynchronousMode(deviceClient);

                    // Register 0x32 is usually used for the custom REV quaternion output, but at
                    // this point in the boot process it contains the JDEC manufacturer ID for the
                    // IMU's external flash chip.
                    RobotLog.dd(TAG, "Flash device's JDEC manufacturer ID: 0x%X", deviceClient.read8(0x32));

                    if (DEBUG_FW_FLASHING) { RobotLog.dd(TAG, "FW length: %d bytes", fwLength); }

                    RobotLog.vv(TAG, "Wiping IMU flash memory");
                    // Show indeterminate progress bar while we wipe the flash memory
                    AppUtil.getInstance().showProgress(UILocation.BOTH, AppUtil.getDefContext().getString(R.string.flashingControlHubImu), new ProgressParameters(0, fwLength));
                    ByteBuffer eraseFlashPayload = ByteBuffer.allocate(8)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .putInt(FW_START_ADDRESS) // Start address
                            .putInt(FW_START_ADDRESS + fwLength); // End address
                    try {
                        sendCommandAndWaitForResponse(deviceClient, CommandType.ERASE_FLASH, eraseFlashPayload.array(), ERASE_FLASH_TIMEOUT_MS);
                    } catch (CommandFailureException e) {
                        RobotLog.ee(TAG, e, "IMU flash erase failed");
                        throw new InitException();
                    } finally {
                        // Erasing may have taken a long time; ensure the CH OS watchdog doesn't trip.
                        AppAliveNotifier.getInstance().notifyAppAlive(); // Make sure we don't trip the CH OS watchdog
                    }

                    // Send up to 64K in a single Write Flash command (will require multiple I2C writes)
                    int nextStartAddress = FW_START_ADDRESS;
                    int numBytesAlreadyWritten = 0;

                    RobotLog.ii(TAG, "Sending firmware data");
                    while (fwBuffer.hasRemaining()) {
                        int fwBytesToTransmitInCommand = Math.min(fwBuffer.remaining(), MAX_WRITE_FLASH_FIRMWARE_BYTES);

                        if (DEBUG_FW_FLASHING) { RobotLog.dd(TAG, "Transmitting Write Flash command with %d fw bytes", fwBytesToTransmitInCommand); }
                        try {
                            // This function will handle keeping the OS watchdog fed while it runs
                            sendWriteFlashCommandAndWaitForResponse(deviceClient, nextStartAddress, fwBytesToTransmitInCommand, fwBuffer);
                        } catch (CommandFailureException e) {
                            RobotLog.ee(TAG, e, "Write Flash command failed");
                            throw new InitException();
                        }
                        nextStartAddress += fwBytesToTransmitInCommand;
                        numBytesAlreadyWritten += fwBytesToTransmitInCommand;
                        AppUtil.getInstance().showProgress(UILocation.BOTH, AppUtil.getDefContext().getString(R.string.flashingControlHubImu), new ProgressParameters(numBytesAlreadyWritten, fwLength));
                    }

                    // We've finished writing the firmware to flash, now we tell the chip to boot from it
                    RobotLog.vv(TAG, "Booting into newly-flashed firmware");
                    sendCommand(deviceClient, CommandType.BOOT_FLASH, null);

                    waitForHostInterface(deviceClient);
                    checkForFlashPresence(deviceClient);
                    boolean flashSucceeded = waitForFlashVerification(deviceClient);
                    if (flashSucceeded) {
                        RobotLog.vv(TAG, "Successfully flashed Control Hub IMU firmware in %.2f seconds", fwFlashTimer.seconds());
                    } else {
                        RobotLog.ee(TAG, "IMU flash verification failed after flashing firmware");
                        throw new InitException();
                    }
                } finally {
                    AppUtil.getInstance().dismissProgress(UILocation.BOTH);
                }
            }
        } catch (InitException e) {
            RobotLog.addGlobalWarningMessage(AppUtil.getDefContext().getString(R.string.controlHubImuFwFlashFailed));
        }
    }

    public BHI260IMU(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        super(i2cDeviceSynchSimple, true, new InterimParameterClassDoNotUse());
    }

    @Override
    protected boolean internalInitialize(@NonNull InterimParameterClassDoNotUse parameters) {
        // TODO(Noah): Copy parameters (once we're actually making use of them)

        // TODO(Noah): Remove
        if (true) { return true; }

        deviceClient.setI2cAddress(I2C_ADDR);

        if (!imuIsPresent(deviceClient)) {
            RobotLog.ee(TAG, "Could not find Control Hub IMU (BHI260AP)");
            return false;
        }

        try {
            waitForHostInterface(deviceClient);
            checkForFlashPresence(deviceClient);
            boolean flashContentsVerified = waitForFlashVerification(deviceClient);

            if (!flashContentsVerified) {
                RobotLog.ee(TAG, "IMU flash contents were not verified");
                throw new InitException();
            }

            if (readBootStatusFlags(deviceClient).contains(BootStatusFlag.FIRMWARE_HALTED)) {
                RobotLog.ee(TAG, "IMU reports that its firmware is not running");
                return false;
            }

            // TODO(Noah): Disable full and delta timestamps with Control FIFO Format command

            // TODO(Noah): Wait for Initialized meta-event (also watch for Error and Sensor Error events)

            fwVersion = read16(deviceClient, Register.USER_VERSION);
            if (fwVersion == BUNDLED_FW_VERSION) {
                RobotLog.dd(TAG, "Firmware version: %d", fwVersion);
            } else {
                RobotLog.ee(TAG, "Firmware version is incorrect. expected=%d actual=%d", BUNDLED_FW_VERSION, fwVersion);
                throw new InitException();
            }

//            RobotLog.dd("Noah", "Host control: 0x%X", read8(deviceClient, Register.HOST_CONTROL));
//            RobotLog.dd("Noah", "Chip ID: 0x%X", read8(deviceClient, Register.CHIP_ID));
            // TODO(Noah): Remove log entries tagged "Noah"

            setStatusFifoToSynchronousMode(deviceClient);

            configureSensor(Sensor.GAME_ROTATION_VECTOR_VIA_REGISTER, 400, 0);
        } catch (InitException e) {
            return false;
        }

        return true;
    }

    /*public Quaternion getOrientation() {
        if (USE_FREEZE_PIN) { AndroidBoard.getInstance().getBhi260QuatRegFreezePin().setState(true); }
        TimestampedData timestampedData = deviceClient.readTimeStamped(Register.QUATERNION_OUTPUT.address, 8);
        if (USE_FREEZE_PIN) { AndroidBoard.getInstance().getBhi260QuatRegFreezePin().setState(false); }
        ByteBuffer data = ByteBuffer.wrap(timestampedData.data).order(ByteOrder.LITTLE_ENDIAN);
        int xInt = data.getShort();
        int yInt = data.getShort();
        int zInt = data.getShort();
        int wInt = data.getShort();
        float x = (float) (xInt * QUATERNION_SCALE_FACTOR);
        float y = (float) (yInt * QUATERNION_SCALE_FACTOR);
        float z = (float) (zInt * QUATERNION_SCALE_FACTOR);
        float w = (float) (wInt * QUATERNION_SCALE_FACTOR);
        return new Quaternion(w, x, y, z, timestampedData.nanoTime);
    }*/

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Lynx;
    }

    @Override
    public String getDeviceName() {
        return AppUtil.getDefContext().getString(R.string.lynx_embedded_bhi260ap_imu_name);
    }

    @Override
    public String getConnectionInfo() {
        return String.format("BHI260 IMU on %s", deviceClient.getConnectionInfo());
    }

    public int getFirmwareVersion() {
        return fwVersion;
    }

    private static int read8(I2cDeviceSynchSimple deviceClient, Register register) {
        return TypeConversion.unsignedByteToInt(deviceClient.read8(register.address));
    }

    private static int read16(I2cDeviceSynchSimple deviceClient, Register register) {
        return TypeConversion.byteArrayToShort(deviceClient.read(register.address, 2), ByteOrder.LITTLE_ENDIAN);
    }

    private static <T extends Enum<T>> EnumSet<T> read8Flags(I2cDeviceSynchSimple deviceClient, Register register, Class<T> enumClass) {
        return convertIntToEnumSet(read8(deviceClient, register), enumClass);
    }

    private static EnumSet<BootStatusFlag> readBootStatusFlags(I2cDeviceSynchSimple deviceClient) {
        return read8Flags(deviceClient, Register.BOOT_STATUS, BootStatusFlag.class);
    }

    private static void write8(I2cDeviceSynchSimple deviceClient, Register register, int data, I2cWaitControl waitControl) {
        deviceClient.write8(register.address, data, waitControl);
    }

    @SuppressWarnings("BusyWait")
    private static void waitForHostInterface(I2cDeviceSynchSimple deviceClient) throws InitException {
        ElapsedTime timeoutTimer = new ElapsedTime();
        EnumSet<BootStatusFlag> bootStatusFlags = readBootStatusFlags(deviceClient);

        try {
            while (!bootStatusFlags.contains(BootStatusFlag.HOST_INTERFACE_READY) && timeoutTimer.milliseconds() < BOOT_FROM_FLASH_TIMEOUT_MS) {
                Thread.sleep(10);
                bootStatusFlags = readBootStatusFlags(deviceClient);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InitException();
        }

        if (!bootStatusFlags.contains(BootStatusFlag.HOST_INTERFACE_READY)) {
            RobotLog.ee(TAG, "Timeout expired while waiting for IMU host interface to become ready");
            throw new InitException();
        }
    }

    private static void checkForFlashPresence(I2cDeviceSynchSimple deviceClient) throws InitException {
        EnumSet<BootStatusFlag> bootStatusFlags = readBootStatusFlags(deviceClient);
        if (!bootStatusFlags.contains(BootStatusFlag.FLASH_DETECTED) || bootStatusFlags.contains(BootStatusFlag.NO_FLASH)) {
            RobotLog.ee(TAG, "IMU did not detect flash chip");
            throw new InitException();
        }
    }

    /**
     * @return true if the flash chip contains valid firmware
     */
    @SuppressWarnings("BusyWait")
    private static boolean waitForFlashVerification(I2cDeviceSynchSimple deviceClient) throws InitException {
        ElapsedTime timeoutTimer = new ElapsedTime();
        EnumSet<BootStatusFlag> bootStatusFlags = readBootStatusFlags(deviceClient);
        AppAliveNotifier.getInstance().notifyAppAlive(); // Make sure we don't trip the CH OS watchdog

        try {
            // Wait for flash verification to either complete or error out
            while (!bootStatusFlags.contains(BootStatusFlag.FLASH_VERIFY_DONE) && timeoutTimer.milliseconds() < 1500) {
                if (bootStatusFlags.contains(BootStatusFlag.FLASH_VERIFY_ERROR)) {
                    RobotLog.ee(TAG, "Error verifying IMU firmware");
                    return false;
                }
                Thread.sleep(10);
                bootStatusFlags = readBootStatusFlags(deviceClient);
            }

            if (!bootStatusFlags.contains(BootStatusFlag.FLASH_VERIFY_DONE)) {
                RobotLog.ww(TAG, "Timeout expired while waiting for IMU to load its firmware from flash");
                return false;
            } else if (bootStatusFlags.contains(BootStatusFlag.FLASH_VERIFY_ERROR)) {
                RobotLog.ee(TAG, "Error verifying IMU firmware");
                return false;
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InitException();
        }
    }

    private static void sendCommand(I2cDeviceSynchSimple deviceClient, CommandType commandType, @Nullable byte[] payload) {
        if (payload == null) { payload = new byte[0]; }
        int totalLength = COMMAND_HEADER_LENGTH + payload.length;
        int numPaddingBytes = 0;

        // Make sure that we send a multiple of 4 bytes
        if (totalLength % 4 != 0) {
            numPaddingBytes = 4 - (payload.length % 4);
            totalLength += numPaddingBytes;
        }

        if (totalLength > MAX_SEND_I2C_BYTES_WITH_REGISTER) {
            throw new IllegalArgumentException("sendCommand() called with too large of a payload. Update sendCommand() to break into multiple I2C writes");
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalLength)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short) commandType.id)
                .putShort((short) (totalLength - COMMAND_HEADER_LENGTH))
                .put(payload);
        if (numPaddingBytes > 0) {
            buffer.put(new byte[numPaddingBytes]);
        }
        deviceClient.write(Register.COMMAND_INPUT.address, buffer.array());
    }

    private static StatusPacket sendCommandAndWaitForResponse(
            I2cDeviceSynchSimple deviceClient,
            CommandType commandType,
            @Nullable byte[] payload,
            int responseTimeoutMs) throws CommandFailureException {
        sendCommand(deviceClient, commandType, payload);
        return waitForCommandResponse(deviceClient, commandType, responseTimeoutMs);
    }

    /**
     * Also handles reporting flash progress and keeping the Control Hub OS watchdog fed
     */
    private static void sendWriteFlashCommandAndWaitForResponse(
            I2cDeviceSynchSimple deviceClient,
            int startAddress,
            int numFwBytesInCommand,
            ByteBuffer bytesSource) throws CommandFailureException {

        if (numFwBytesInCommand > MAX_WRITE_FLASH_FIRMWARE_BYTES) {
            throw new IllegalArgumentException("Tried to write too many bytes in a single Write Flash command");
        }

        AppAliveNotifier.getInstance().notifyAppAlive(); // Make sure we don't trip the CH OS watchdog

        // totalLengthOfCommand also needs to account for padding, if needed. We'll update it next.
        int totalLengthOfCommand = WRITE_FLASH_COMMAND_HEADER_LENGTH + numFwBytesInCommand;
        int numPaddingBytes = 0;

        // Make sure that we send a multiple of 4 bytes (required by the datasheet)
        if (totalLengthOfCommand % 4 != 0) {
            numPaddingBytes = 4 - (numFwBytesInCommand % 4);
            totalLengthOfCommand += numPaddingBytes;
        }
        int numFwPlusPaddingBytes = numFwBytesInCommand + numPaddingBytes;

        if (DEBUG_FW_FLASHING) { RobotLog.dd(TAG, "totalLengthOfCommand=%d numFwPlusPaddingBytes=%d numFwBytesInCommand=%d", totalLengthOfCommand, numFwPlusPaddingBytes, numFwBytesInCommand); }

        ByteBuffer buffer = ByteBuffer.allocate(totalLengthOfCommand)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short) CommandType.WRITE_FLASH.id);

        // While there is a WRITE_FLASH_COMMAND_HEADER_LENGTH constant, that is only used so that
        // we don't have to separately keep remembering to subtract out the starting address bytes,
        // in addition to the header. As far as the BHI260 is concerned, the Write Flash command has
        // the same size header as every other command.
        short writeFlashCommandLength = (short) (totalLengthOfCommand - COMMAND_HEADER_LENGTH);
        if (DEBUG_FW_FLASHING) { RobotLog.dd(TAG, "Write Flash command length: %d", TypeConversion.unsignedShortToInt(writeFlashCommandLength)); }
        buffer.putShort(writeFlashCommandLength);

        buffer.putInt(startAddress);
        int bufferPositionFirmwareBytes = buffer.position();

        if (DEBUG_FW_FLASHING) { RobotLog.dd(TAG, "Copying %d bytes from byte source (%d bytes remaining)", numFwBytesInCommand, bytesSource.remaining() - numFwBytesInCommand); }
        bytesSource.get(buffer.array(), bufferPositionFirmwareBytes, numFwBytesInCommand);

        buffer.position(WRITE_FLASH_COMMAND_HEADER_LENGTH + numFwBytesInCommand);

        if (numPaddingBytes > 0) {
            buffer.put(new byte[numPaddingBytes]);
        }

        if (DEBUG_FW_FLASHING) { printByteBuffer("Write Flash command", buffer); }
        deviceClient.write(Register.COMMAND_INPUT.address, buffer.array());
        waitForCommandResponse(deviceClient, CommandType.WRITE_FLASH, WRITE_FLASH_RESPONSE_TIMEOUT_MS);
    }

    /**
     * Because of the Expansion Hub firmware bug that causes an extra I2C byte to be read (without
     * telling us what that byte was), receiving the response can only work correctly when the
     * response payload has 100 bytes or less. We accomplish it working at all by ignoring the
     * second payload length byte when we read the header, so that we lose that instead of the first
     * payload byte.
     */
    private static StatusPacket waitForCommandResponse(I2cDeviceSynchSimple deviceClient, CommandType commandType, int timeoutMs) throws CommandFailureException {
        ElapsedTime timeoutTimer = new ElapsedTime();
        ElapsedTime notifyAppAliveTimer = new ElapsedTime();

        // Make sure we don't trip the CH OS watchdog
        AppAliveNotifier.getInstance().notifyAppAlive();

        // Loop until we the interrupt status indicates that there is Status data available
        while (true) {
            // After 10 seconds, the Control Hub OS watchdog will trip
            if (notifyAppAliveTimer.seconds() > 8) {
                AppAliveNotifier.getInstance().notifyAppAlive();
                notifyAppAliveTimer.reset();
            }

            if (timeoutTimer.milliseconds() >= timeoutMs) {
                throw new CommandFailureException(String.format(Locale.ENGLISH, "%dms timeout expired while waiting for response", timeoutMs));
            }

            // TODO(Noah): Use the IMU interrupt pin instead of the interrupt register
            EnumSet<InterruptStatusFlag> interruptStatusFlags = read8Flags(deviceClient, Register.INTERRUPT_STATUS, InterruptStatusFlag.class);

            // For some reason, the Reset or Fault status is always set for the Erase Flash response.
            if (interruptStatusFlags.contains(InterruptStatusFlag.RESET_OR_FAULT) && commandType != CommandType.ERASE_FLASH) {
                RobotLog.ww(TAG, "Reset or Fault interrupt status was set while waiting for %s response", commandType);
                RobotLog.ww(TAG, "Interrupt status: %s", interruptStatusFlags);
                RobotLog.ww(TAG, "Error value: 0x%X", read8(deviceClient, Register.ERROR_VALUE));
            }

            if (interruptStatusFlags.contains(InterruptStatusFlag.STATUS_STATUS)) {
                break;
            }
        }

        // Loop until we successfully receive a response. If the IMU responds with a status code of
        // 0, we try again.
        while (true) {
            // After 10 seconds, the Control Hub OS watchdog will trip
            if (notifyAppAliveTimer.seconds() > 8) {
                AppAliveNotifier.getInstance().notifyAppAlive();
                notifyAppAliveTimer.reset();
            }

            if (timeoutTimer.milliseconds() >= timeoutMs) {
                throw new CommandFailureException(String.format(Locale.ENGLISH, "%dms timeout expired while waiting for response", timeoutMs));
            }

            // As per section 14.2.1 of the datasheet, the standard FIFO format does not apply to
            // the Status and Debug FIFO in synchronous mode (which we are using). This means that
            // the first two bytes will be the status code, not the amount of data in the FIFO.

            // Read the first 3 bytes of the 4-byte header (reading the 4th would corrupt the
            // payload because of the Expansion Hub FW bug)

            // Bytes 0 and 1 tell us the command response's status code
            // Byte 3 tells us the length of the command response's payload
            byte[] responseHeaderBytes = deviceClient.read(Register.STATUS_AND_DEBUG_FIFO_OUTPUT.address, 3);
            ByteBuffer responseHeader = ByteBuffer.wrap(responseHeaderBytes).order(ByteOrder.LITTLE_ENDIAN);
            final int responseStatusCode = TypeConversion.unsignedShortToInt(responseHeader.getShort());
            final int responsePayloadLength = TypeConversion.unsignedByteToInt(responseHeader.get());

            if (responsePayloadLength > MAX_READ_I2C_BYTES) {
                throw new RuntimeException(String.format(Locale.ENGLISH, "IMU sent payload that was too long (%d bytes)", responsePayloadLength));
            }

            byte[] responsePayload = responsePayloadLength == 0 ? new byte[0] : deviceClient.read(responsePayloadLength);

            if (responseStatusCode != commandType.successStatusCode) {
                if (responseStatusCode == 0) {
                    RobotLog.ww(TAG, "Received status code 0, trying again");
                    continue; // Try again
                }

                String errorMessage = null;
                String erroredCommandDesc;

                if (responseStatusCode == COMMAND_ERROR_RESPONSE) {
                    CommandError commandError = null;
                    int commandErrorCode = -1;
                    int erroredCommandId = -1;

                    if (responsePayload.length >= 3) {
                        ByteBuffer errorPayload = ByteBuffer.wrap(responsePayload).order(ByteOrder.LITTLE_ENDIAN);
                        erroredCommandId = TypeConversion.unsignedShortToInt(errorPayload.getShort());
                        commandErrorCode = TypeConversion.unsignedByteToInt(errorPayload.get());
                        commandError = CommandError.fromInt(commandErrorCode);
                    }

                    if (erroredCommandId == commandType.id) {
                        erroredCommandDesc = commandType.toString() + " command";
                    } else {
                        // The command ID that this error is a response to does not match the command we just sent
                        CommandType erroredCommandType = CommandType.findById(erroredCommandId);

                        if (erroredCommandType == null) {
                            erroredCommandDesc = String.format(Locale.US, "unknown command 0x%4X (just sent %s command)", erroredCommandId, commandType);
                        } else {
                            erroredCommandDesc = String.format(Locale.US, "%s command (just sent %s command)", erroredCommandType, commandType);
                        }
                    }

                    if (commandError == null) {
                        errorMessage = String.format(Locale.US, "Received unknown Command Error code 0x%2X in response to %s", commandErrorCode, erroredCommandDesc);
                    } else {
                        errorMessage = String.format(Locale.US, "Received Command Error %s in response to %s", commandError, erroredCommandDesc);
                    }
                }

                // TODO(Noah): Add good support for receiving a non-error, non-matching response

                if (errorMessage == null) {
                    errorMessage = String.format(Locale.US, "Received unexpected response status 0x%X for %s command", responseStatusCode, commandType);
                }

                throw new CommandFailureException(errorMessage);
            }
            return new StatusPacket(responseStatusCode, responsePayload);
        }
    }

    private void configureSensor(Sensor sensor, float sampleRateHz, int latencyMs) {
        if (latencyMs > 16777215) {
            // The latency is larger than can fit into 3 bytes
            throw new IllegalArgumentException("Sensor latency must be less than 1,6777,215 milliseconds");
        }

        ByteBuffer buffer = ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) sensor.id)
                .putFloat(sampleRateHz); // putFloat() adds 4 bytes
        byte[] latency4Bytes = TypeConversion.intToByteArray(latencyMs, ByteOrder.LITTLE_ENDIAN);
        buffer.put(latency4Bytes, 0, 3); // Add the first 3 bytes of the latency integer
        sendCommand(deviceClient, CommandType.CONFIGURE_SENSOR, buffer.array());
        // TODO(Noah): Check that the sensor is correctly configured
        // TODO(Noah): Check the Error Value register
    }

    // Synchronous mode means that we can trust that the Status/Debug FIFO will only
    // contain responses to our commands
    private static void setStatusFifoToSynchronousMode(I2cDeviceSynchSimple deviceClient) {
        write8(deviceClient, Register.HOST_INTERFACE_CONTROL, 0, I2cWaitControl.ATOMIC); // 0x80 would be async mode
    }

    private static <T extends Enum<T>> EnumSet<T> convertIntToEnumSet(int value, Class<T> enumClass) {
        EnumSet<T> flags = EnumSet.noneOf(enumClass);
        for (T flag: enumClass.getEnumConstants()) {
            if (((1 << flag.ordinal()) & value) == (1 << flag.ordinal())) {
                flags.add(flag);
            }
        }
        return flags;
    }

    @Deprecated public static class InterimParameterClassDoNotUse { }

    private enum Register {
        COMMAND_INPUT(0x00),
        WAKE_UP_FIFO_OUTPUT(0x01),
        NON_WAKE_UP_FIFO_OUTPUT(0x02),
        STATUS_AND_DEBUG_FIFO_OUTPUT(0x03),
        CHIP_CONTROL(0x05),
        HOST_INTERFACE_CONTROL(0x06),
        HOST_INTERRUPT_CONTROL(0x07),
        RESET_REQUEST(0x14),
        TIMESTAMP_EVENT_REQUEST(0x15),
        HOST_CONTROL(0x16),
        HOST_STATUS(0x17),
        PRODUCT_IDENTIFIER(0x1C),
        REVISION_IDENTIFIER(0x1D),
        ROM_VERSION(0x1E),
        KERNEL_VERSION(0x20),
        USER_VERSION(0x22),
        FEATURE_STATUS(0x24),
        BOOT_STATUS(0x25),
        CHIP_ID(0x2B),
        INTERRUPT_STATUS(0x2D),
        ERROR_VALUE(0x2E),
        QUATERNION_OUTPUT(0x32);

        private final int address;

        Register(int address) {
            this.address = address;
        }
    }

    private enum CommandType {
        ERASE_FLASH(0x0004, 0x000A),
        WRITE_FLASH(0x0005, 0x000B),
        BOOT_FLASH(0x0006, 0),
        CONFIGURE_SENSOR(0x000D, 0);

        private final int id;
        private final int successStatusCode; // 0 indicates no response expected

        CommandType(int id, int successStatusCode) {
            this.id = id;
            this.successStatusCode = successStatusCode;
        }

        public static @Nullable CommandType findById(int id) {
            for (CommandType commandType: CommandType.values()) {
                if (commandType.id == id) { return commandType; }
            }
            return null;
        }
    }

    private static final int COMMAND_ERROR_RESPONSE = 0x0F;
    private enum CommandError {
        INCORRECT_LENGTH(0x01),
        TOO_LONG(0x02),
        PARAM_WRITE_ERROR(0x03),
        PARAM_READ_ERROR(0x04),
        INVALID_COMMAND(0x05),
        INVALID_PARAM(0x06),
        COMMAND_FAILED(0xFF);

        private final int value;
        CommandError(int value) {
            this.value = value;
        }

        public static @Nullable CommandError fromInt(int intValue) {
            for (CommandError value: values()) {
                if (intValue == value.value) { return value; }
            }
            return null;
        }
    }

    private enum Sensor {
        GAME_ROTATION_VECTOR_WAKE_UP(38),
        GAME_ROTATION_VECTOR_VIA_REGISTER(176);

        private final int id;

        Sensor(int id) {
            this.id = id;
        }
    }

    private enum BootStatusFlag {
        FLASH_DETECTED,
        FLASH_VERIFY_DONE,
        FLASH_VERIFY_ERROR,
        NO_FLASH,
        HOST_INTERFACE_READY,
        FIRMWARE_VERIFY_DONE,
        FIRMWARE_VERIFY_ERROR,
        FIRMWARE_HALTED
    }

    private enum InterruptStatusFlag {
        HOST_INTERRUPT_ASSERTED,
        WAKE_UP_FIFO_STATUS_1,
        WAKE_UP_FIFO_STATUS_2,
        NON_WAKE_UP_FIFO_STATUS_1,
        NON_WAKE_UP_FIFO_STATUS_2,
        STATUS_STATUS, // Indicates that a command result is ready
        DEBUG_STATUS,
        RESET_OR_FAULT
    }

    /**
     * Public-facing methods should not throw this exception.
     *
     * This exception indicates that initialization failed, and should be handled by returning
     * false from doInitialize()
     */
    private static class InitException extends Exception {}

    /**
     * Public-facing methods should not throw this exception.
     *
     * This exception indicates that a command failed.
     */
    private static class CommandFailureException extends Exception {
        public CommandFailureException(String message) {
            super(message);
        }
    }

    private static class StatusPacket {
        public final int statusCode;
        public final byte[] payload;

        private StatusPacket(int statusCode, byte[] payload) {
            this.statusCode = statusCode;
            this.payload = payload;
        }
    }

    public static void printByteBuffer(String tag, ByteBuffer buffer) {
        int initialPosition = buffer.position();
        buffer.position(0);
        int length = buffer.remaining();
        StringBuilder stringBuilder = new StringBuilder(String.format("%X", buffer.get()));
        while (buffer.hasRemaining()) {
            stringBuilder.append(String.format("-%X", buffer.get()));
        }
        RobotLog.dd(TAG, "%s (%d bytes): %s", tag, length, stringBuilder);
        buffer.position(initialPosition);
    }
}
