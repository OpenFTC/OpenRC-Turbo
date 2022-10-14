/*
Copyright (c) 2016 Robert Atkinson, Noah Andrews

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
package org.firstinspires.ftc.robotcore.internal.hardware.android;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.util.LastKnown;
import com.qualcomm.robotcore.util.RobotLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * {@link GpioPin} controls an exported GPIO pin on an Android board.
 * <p>
 * If you're accessing a new pin, it should be defined as an abstract getter in {@link AndroidBoard}
 * and implemented in all {@link AndroidBoard} subclasses.
 */
@SuppressWarnings("WeakerAccess")
public class GpioPin implements DigitalChannel
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public enum Active { LOW, HIGH }

    protected final int             rawGpioNumber;
    protected final File            path;
    protected final Active          active;
    protected LastKnown<DigitalChannel.Mode> lastKnownMode;
    protected DigitalChannel.Mode   defaultMode;
    protected boolean               defaultStateIfOutput;
    protected String                TAG;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    /**
     * See comments in the {@link AndroidBoard} subclasses for documentation on calculating the raw GPIO number
     */
    public static GpioPin createInput(int rawGpioNumber, Active active, String name)
        {
        return new GpioPin(rawGpioNumber, Mode.INPUT, false, active, name);
        }

    /**
     * See comments in the {@link AndroidBoard} subclasses for documentation on calculating the raw GPIO number
     */
    public static GpioPin createOutput(int rawGpioNumber, boolean initialState, Active active, String name)
        {
        return new GpioPin(rawGpioNumber, Mode.OUTPUT, initialState, active, name);
        }

    private GpioPin(int rawGpioNumber, DigitalChannel.Mode defaultMode, boolean defaultStateIfOutput, Active active, String name)
        {
        this.rawGpioNumber = rawGpioNumber;
        this.path = new File(String.format(Locale.US, "/sys/class/gpio/gpio%d", rawGpioNumber));
        this.active = active;
        //
        this.lastKnownMode = new LastKnown<>();
        this.defaultMode = defaultMode;
        this.defaultStateIfOutput = defaultStateIfOutput;
        this.TAG = name;
        setDefaultState();
        }

    //----------------------------------------------------------------------------------------------
    // Operations
    //----------------------------------------------------------------------------------------------

    // TODO: If it's an input pin, then read the value directly from the device. Otherwise, return
    //       what we last wrote
    @Override public boolean getState()
        {
        return adjustActive(getRawState());
        }

    protected synchronized boolean getRawState()
        {
        // We can read 'value' even for output pins
        try {
            String zeroOrOne = readAspect("value");
            return Integer.parseInt(zeroOrOne) != 0;
            }
        catch (IOException e)
            {
            RobotLog.logExceptionHeader(TAG, e, "exception in getRawState(); ignored");
            return false;
            }
        }

    @Override public synchronized void setState(boolean state)
        {
        if (getMode() == Mode.OUTPUT)
            {
            try {
                String zeroOrOne = adjustActive(state) ? "1" : "0";
                writeAspect("value", zeroOrOne);
                }
            catch (IOException e)
                {
                RobotLog.logExceptionHeader(TAG, e, "exception in setState(); ignored");
                }
            }
        }

    @Override public synchronized Mode getMode()
        {
        Mode result = lastKnownMode.getValue();
        if (result == null)
            {
            result = getRawMode();
            lastKnownMode.setValue(result);
            }
        return result;
        }

    protected synchronized Mode getRawMode()
        {
        try {
            String direction = readAspect("direction");
            switch (direction)
                {
                case "out": return Mode.OUTPUT;
                case "in": default: return Mode.INPUT;
                }
            }
        catch (IOException e)
            {
            RobotLog.logExceptionHeader(TAG, e, "exception in getRawMode(); ignored");
            return Mode.INPUT;  // arbitrary
            }
        }

    @Override public synchronized void setMode(Mode mode)
        {
        try {
            // To avoid permissions errors for pins that we're not allowed to set the direction of,
            // we first check if the mode is already as-desired.
            String desiredContents = mode==Mode.INPUT ? "in" : "out";
            String actualContents = readAspect("direction");
            if (!desiredContents.equals(actualContents))
                {
                writeAspect("direction", desiredContents);
                }
            lastKnownMode.setValue(mode);
            }
        catch (IOException e)
            {
            RobotLog.logExceptionHeader(TAG, e, "exception in setMode(); ignored");
            }
        }

    @Override @Deprecated public void setMode(DigitalChannelController.Mode mode)
        {
        setMode(mode.migrate());
        }

    //----------------------------------------------------------------------------------------------
    // HardwareDevice
    //----------------------------------------------------------------------------------------------

    @Override public Manufacturer getManufacturer()
        {
        return Manufacturer.Other;
        }

    @Override public String getDeviceName()
        {
        return "GPIO Pin " + TAG;
        }

    @Override public String getConnectionInfo()
        {
        return String.format(Locale.ENGLISH, "GPIO #%d", rawGpioNumber);
        }

    @Override public int getVersion()
        {
        return 1;
        }

    @Override public synchronized void resetDeviceConfigurationForOpMode()
        {
        // Nothing to do
        }

    @Override public void close()
        {
        // Nothing to do
        }

    //----------------------------------------------------------------------------------------------
    // Utility
    //----------------------------------------------------------------------------------------------

    private void setDefaultState()
        {
        setMode(defaultMode);
        if (defaultMode == Mode.OUTPUT)
            {
            setState(defaultStateIfOutput);
            }
        }

    protected boolean adjustActive(boolean state)
        {
        //noinspection SimplifiableConditionalExpression
        return (active == Active.HIGH) ? state : !state;
        }

    protected String readAspect(String aspect) throws IOException
        {
        File aspectFile = new File(getPath(), aspect);
        try (BufferedReader reader = new BufferedReader(new FileReader(aspectFile)))
            {
            return reader.readLine();
            }
        }

    protected void writeAspect(String aspect, String value) throws IOException
        {
        File aspectFile = new File(getPath(), aspect);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(aspectFile)))
            {
            writer.write(value);
            }
        }

    protected File getPath()
        {
        return path;
        }
    }
