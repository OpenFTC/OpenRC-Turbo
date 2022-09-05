/*
 * Copyright (c) 2014, 2015, 2016 Qualcomm Technologies Inc
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
 /*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
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
package com.qualcomm.hardware.modernrobotics.comm;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DeviceManager.UsbDeviceType;
import com.qualcomm.robotcore.hardware.configuration.ModernRoboticsConstants;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbManager;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;

import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;

@SuppressWarnings("WeakerAccess")
public class ModernRoboticsUsbUtil
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

        //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    public ModernRoboticsUsbUtil()
        {
        }

    //----------------------------------------------------------------------------------------------
    // Device management
    //----------------------------------------------------------------------------------------------

    public static RobotUsbDevice openRobotUsbDevice(boolean doScan, RobotUsbManager robotUsbManager, SerialNumber serialNumber) throws RobotCoreException
        {
        if (doScan)
            {
            robotUsbManager.scanForDevices();
            }

        RobotUsbDevice robotUsbDevice = null;
        try
            {
            robotUsbDevice = robotUsbManager.openBySerialNumber(serialNumber);
            }
        catch (Exception e)
            {
            throw RobotCoreException.createChained(e, "Unable to open USB device " + serialNumber + ": " + e.getMessage());
            }

        try
            {
            robotUsbDevice.setBaudRate(ModernRoboticsConstants.USB_BAUD_RATE);
            robotUsbDevice.setDataCharacteristics((byte) 8, (byte) 0, (byte) 0);
            robotUsbDevice.setLatencyTimer(ModernRoboticsConstants.LATENCY_TIMER);
            }
        catch (Exception e)
            {
            robotUsbDevice.close();
            throw RobotCoreException.createChained(e, "Unable to parameterize USB device " + serialNumber + " - " + robotUsbDevice.getProductName() + ": " + e.getMessage());
            }

        try
            {
            /**
                * TODO: This timeout of 400ms can almost assuredly be reduced with some further analysis.
                *
                * "From: Berling, Jonathan
                * Sent: Tuesday, January 3, 2017
                *
                * [...] When we were developing that code, if we tried to open too many USB devices too
                * quickly they would sometimes fail. We never looked into the cause. It could have been
                * anything from the USB devices themselves, to Android, to the type of hub we were using,
                * etc. (This was long before MR was even considering making a USB hub.)"
                */
            Thread.sleep(400L);
            }
        catch (InterruptedException e)
            {
            Thread.currentThread().interrupt();
            }

        return robotUsbDevice;
        }

    public static byte[] getUsbDeviceHeader(RobotUsbDevice robotUsbDevice) throws RobotUsbException
        {
        byte[] deviceHeaderData = new byte[3];
        //      0 == firmware revision
        //      1 == manufacturer code
        //      2 == device id code

        try {
            ModernRoboticsReaderWriter readerWriter = new ModernRoboticsReaderWriter(robotUsbDevice);
            readerWriter.read(true, 0, deviceHeaderData, null);
            }
        catch (InterruptedException e)
            {
            Thread.currentThread().interrupt();
            }

        return deviceHeaderData;
        }

    }
