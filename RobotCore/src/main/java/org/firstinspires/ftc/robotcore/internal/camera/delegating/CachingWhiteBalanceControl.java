/*
 * Copyright (c) 2021 Michael Hoogasian
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Michael Hoogasian nor the names of his contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.robotcore.internal.camera.delegating;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.firstinspires.ftc.robotcore.external.hardware.camera.Camera;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.PtzControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.WhiteBalanceControl;
import org.firstinspires.ftc.robotcore.internal.system.Tracer;

/**
 * A {@link WhiteBalanceControl} that caches state from another WhiteBalance control
 */
@SuppressWarnings("WeakerAccess")
public class CachingWhiteBalanceControl implements WhiteBalanceControl, DelegatingCameraControl
{
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public static final String TAG = "CachingWhiteBalanceControl";
    public String getTag() { return TAG; }
    public static boolean TRACE = true;
    protected Tracer tracer = Tracer.create(getTag(), TRACE);

    public static boolean isUnknownTemperature(int temperature) { return temperature == unknownTemperature; }

    protected final Object lock = new Object();
    protected Camera camera = null;
    protected @NonNull WhiteBalanceControl delegatedWhiteBalanceControl;
    protected final WhiteBalanceControl fakeWhiteBalanceControl;

    protected static final int unknownTemperature = -1;

    protected int minTemperature = unknownTemperature;
    protected int maxTemperature = unknownTemperature;
    protected int temperature = unknownTemperature;
    protected Mode mode = null;

    protected boolean limitsInitialized = false;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    public CachingWhiteBalanceControl()
    {
        fakeWhiteBalanceControl = new WhiteBalanceControl()
        {
            @Override
            public int getWhiteBalanceTemperature()
            {
                return temperature;
            }

            @Override
            public boolean setWhiteBalanceTemperature(int temperature)
            {
                return false;
            }

            @Override
            public Mode getMode()
            {
                return Mode.UNKNOWN;
            }

            @Override
            public boolean setMode(Mode mode)
            {
                return false;
            }

            @Override
            public int getMinWhiteBalanceTemperature()
            {
                return minTemperature;
            }

            @Override
            public int getMaxWhiteBalanceTemperature()
            {
                return maxTemperature;
            }
        };


        delegatedWhiteBalanceControl = fakeWhiteBalanceControl;
    }

    @Override public void onCameraChanged(@Nullable Camera newCamera)
    {
        synchronized (lock)
        {
            if (camera != newCamera)
            {
                camera = newCamera;
                if (camera != null)
                {
                    //noinspection ConstantConditions
                    delegatedWhiteBalanceControl = camera.getControl(WhiteBalanceControl.class);
                    if (delegatedWhiteBalanceControl == null)
                    {
                        delegatedWhiteBalanceControl = fakeWhiteBalanceControl;
                    }
                    if (!limitsInitialized)
                    {
                        initializeLimits();
                        if (delegatedWhiteBalanceControl != fakeWhiteBalanceControl)
                        {
                            limitsInitialized = true;
                        }
                    }
                    write();
                    read();
                }
                else
                {
                    delegatedWhiteBalanceControl = fakeWhiteBalanceControl;
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // GainControl
    //----------------------------------------------------------------------------------------------

    protected void write()
    {
        if (!isUnknownTemperature(temperature))
        {
            delegatedWhiteBalanceControl.setWhiteBalanceTemperature(temperature);
        }
    }

    protected void read()
    {
        temperature = delegatedWhiteBalanceControl.getWhiteBalanceTemperature();

        if (!limitsInitialized)
        {
            initializeLimits();
        }
    }

    //----------------------------------------------------------------------------------------------

    void initializeLimits()
    {
        minTemperature = delegatedWhiteBalanceControl.getMinWhiteBalanceTemperature();
        maxTemperature = delegatedWhiteBalanceControl.getMaxWhiteBalanceTemperature();
    }

    @Override
    public int getWhiteBalanceTemperature()
    {
        synchronized (lock)
        {
            temperature = delegatedWhiteBalanceControl.getWhiteBalanceTemperature();
            return temperature;
        }
    }

    @Override
    public boolean setWhiteBalanceTemperature(int whiteBalanceTemperature)
    {
        if (whiteBalanceTemperature >= minTemperature && whiteBalanceTemperature <= maxTemperature)
        {
            synchronized (lock)
            {
                /*if(mode != Mode.MANUAL)
                {
                    return false;
                }*/

                if (delegatedWhiteBalanceControl.setWhiteBalanceTemperature(whiteBalanceTemperature))
                {
                    temperature = whiteBalanceTemperature;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Mode getMode()
    {
        synchronized (lock)
        {
            mode = delegatedWhiteBalanceControl.getMode();
            return mode;
        }
    }

    @Override
    public boolean setMode(Mode newMode)
    {
        synchronized (lock)
        {
            if (delegatedWhiteBalanceControl.setMode(newMode))
            {
                mode = newMode;
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    @Override
    public int getMinWhiteBalanceTemperature()
    {
        return minTemperature;
    }

    @Override
    public int getMaxWhiteBalanceTemperature()
    {
        return maxTemperature;
    }
}