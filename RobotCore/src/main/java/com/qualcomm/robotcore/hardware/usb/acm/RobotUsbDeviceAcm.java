package com.qualcomm.robotcore.hardware.usb.acm;/*
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDeviceImplBase;
import com.qualcomm.robotcore.hardware.usb.acm.PicoSerialPort;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;

import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbDeviceClosedException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbUnspecifiedException;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * {@link RobotUsbDeviceTty} is an implementation of {@link RobotUsbDevice} that
 * sits on top of a serial file handle (such as /dev/tty) and presents the persona of
 * a (fake) USB device.
 */
@SuppressWarnings("WeakerAccess")
public class RobotUsbDeviceAcm extends RobotUsbDeviceImplBase implements RobotUsbDevice
{
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public static final String TAG = "RobotUsbDeviceAcm";
    public static boolean DEBUG = false;
    @Override public String getTag() { return TAG; }

    protected PicoSerialPort            serialPort;
    protected int                       baudRate;
    protected int                       msDefaultTimeout = 100;
    protected USBIdentifiers            usbIdentifiers   = new USBIdentifiers();
    protected final Object              startStopLock    = new Object();
    protected final Object              readLock         = new Object();
    protected final Object              writeLock        = new Object();
    protected Queue<Byte>               readAhead        = new ArrayDeque<Byte>();
    protected boolean                   debugRetainBuffers = false;
    protected @NonNull String           productName      = "";

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    public RobotUsbDeviceAcm(PicoSerialPort serialPort, SerialNumber serialNumber)
    {
        super(serialNumber);
        this.serialPort = serialPort;
        this.baudRate = 115200;

        usbIdentifiers.vendorId = serialPort.getVid();
        usbIdentifiers.productId = serialPort.getPid();
    }

    //----------------------------------------------------------------------------------------------
    // RobotUsbDevice - construction
    //----------------------------------------------------------------------------------------------

    @Override public void close()
    {
        synchronized (this.startStopLock)
        {
            if (this.serialPort != null)
            {
                removeFromExtantDevices();
            }
        }
    }

    @Override public boolean isOpen()
    {
        synchronized (this.startStopLock)
        {
            return true;
        }
    }

    @Override public boolean isAttached()
    {
        return new File(serialPort.getUsbDevice().getDeviceName()).exists();
    }

    //----------------------------------------------------------------------------------------------
    // RobotUsbDevice - core read & write
    //----------------------------------------------------------------------------------------------

    @Override public void resetAndFlushBuffers()
    {
        // TODO: Nothing we know how to do here, at the moment, but perhaps we can revisit this in future
    }

    @Override public void write(byte[] data) throws RobotUsbException
    {
        // Only one writer at a time: we're not *certain* the output stream is thread-safe
        synchronized (this.writeLock)
        {
            this.serialPort.write(data);
            if (DEBUG)
            {
                dumpBytesSent(data);
            }
        }
    }

    @Override
    public int read(byte[] data, final int ibFirst, final int cbToRead, final long msTimeout, @Nullable TimeWindow timeWindow) throws RobotUsbException, InterruptedException
    {
        // Only one reader at a time, thank you very much
        synchronized (this.readLock)
        {
            int cbRead = serialPort.read(data, ibFirst, cbToRead, (int)msTimeout, timeWindow);

            if(cbRead == cbToRead)
            {
                return cbRead;
            }

            if(cbRead == PicoSerialPort.RC_DEVICE_CLOSED)
            {
                throw new RobotUsbDeviceClosedException("error: closed: FT_Device.read()==RC_DEVICE_CLOSED");
            }

            throw new RuntimeException("chicken");
        }
    }

    @Override public boolean mightBeAtUsbPacketStart()
    {
        return true;
    }

    @Override public void skipToLikelyUsbPacketStart()
    {
        // Nothing we can do here of use
    }

    @Override public void requestReadInterrupt(boolean interruptRequested)
    {
        System.out.println("acm: request interrupt");
        serialPort.requestReadInterrupt(interruptRequested);
    }

    //----------------------------------------------------------------------------------------------
    // RobotUsbDevice - io configuration
    //----------------------------------------------------------------------------------------------

    @Override public void setDebugRetainBuffers(boolean retain)
    {
        this.debugRetainBuffers = retain;
    }
    @Override public boolean getDebugRetainBuffers()
    {
        return this.debugRetainBuffers;
    }
    @Override public void logRetainedBuffers(long nsOrigin, long nsTimerExpire, String tag, String format, Object...args)
    {
        RobotLog.ee(tag, format, args);
    }
    @Override public void setBaudRate(int baudRate) throws RobotUsbException
    {
        // Ignored: we are passed in our open device, with baud rate already configured.
        // If we wanted to update it on the fly, we'd need to do more work in SerialPort.cpp.
        // We could do that in theory, but it's not worth it right at the moment.
    }

    @Override
    public void setDataCharacteristics(byte dataBits, byte stopBits, byte parity) throws RobotUsbException
    {
        // ignored
    }

    @Override public void setLatencyTimer(int latencyTimer) throws RobotUsbException
    {
        // ignored
    }

    @Override public void setBreak(boolean enable) throws RobotUsbException
    {
        // ignored // TODO fix later
    }

    public void setMsDefaultTimeout(int msDefaultTimeout)
    {
        this.msDefaultTimeout = msDefaultTimeout;
    }

    public int getMsDefaultTimeout()
    {
        return msDefaultTimeout;
    }

    //----------------------------------------------------------------------------------------------
    // RobotUsbDevice - meta data
    //----------------------------------------------------------------------------------------------

    @Override public USBIdentifiers getUsbIdentifiers()
    {
        return this.usbIdentifiers;
    }

    public void setUsbIdentifiers(USBIdentifiers usbIdentifiers)
    {
        this.usbIdentifiers = usbIdentifiers;
    }

    public void setProductName(@NonNull String productName)
    {
        this.productName = productName;
    }

    @Override @NonNull public String getProductName()
    {
        return productName;
    }
}
