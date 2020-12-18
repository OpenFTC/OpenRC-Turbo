/*
 * Copyright (c) 2020 Michael Hoogasian
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
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.internal.system.Tracer;

/**
 * A {@link GainControl} that caches state from another gain control
 */
@SuppressWarnings("WeakerAccess")
public class CachingGainControl implements GainControl, DelegatingCameraControl
{
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public static final String TAG = "CachingGainControl";
    public String getTag() { return TAG; }
    public static boolean TRACE = true;
    protected Tracer tracer = Tracer.create(getTag(), TRACE);

    public static boolean isUnknownGain(int gain) { return gain < 0; }

    protected final Object lock = new Object();
    protected Camera camera = null;
    protected @NonNull GainControl delegatedGainControl;
    protected final GainControl fakeGainControl;

    protected int unknownGain = -1;

    protected int minGain = unknownGain;
    protected int maxGain = unknownGain;
    protected int gain = unknownGain;

    protected boolean limitsInitialized = false;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    public CachingGainControl()
    {
        fakeGainControl = new GainControl()
        {
            @Override
            public int getMinGain()
            {
                return minGain;
            }

            @Override
            public int getMaxGain()
            {
                return maxGain;
            }

            @Override
            public int getGain()
            {
                return gain;
            }

            @Override
            public boolean setGain(int gain)
            {
                return false;
            }
        };
        delegatedGainControl = fakeGainControl;
    }

    @Override
    public void onCameraChanged(@Nullable Camera newCamera)
    {
        synchronized (lock)
        {
            if (camera != newCamera)
            {
                camera = newCamera;
                if (camera != null)
                {
                    //noinspection ConstantConditions
                    delegatedGainControl = camera.getControl(GainControl.class);
                    if (delegatedGainControl == null)
                    {
                        delegatedGainControl = fakeGainControl;
                    }
                    if (!limitsInitialized)
                    {
                        initializeLimits();
                        if (delegatedGainControl != fakeGainControl)
                        {
                            limitsInitialized = true;
                        }
                    }
                    write();
                    read();
                }
                else
                {
                    delegatedGainControl = fakeGainControl;
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // GainControl
    //----------------------------------------------------------------------------------------------

    protected void write()
    {
        if (!isUnknownGain(gain))
        {
            delegatedGainControl.setGain(gain);
        }
    }

    protected void read()
    {
        gain = delegatedGainControl.getGain();
        if (!limitsInitialized)
        {
            minGain = delegatedGainControl.getMinGain();
            maxGain = delegatedGainControl.getMaxGain();
        }
    }

    //----------------------------------------------------------------------------------------------

    void initializeLimits()
    {
        minGain = delegatedGainControl.getMinGain();
        maxGain = delegatedGainControl.getMaxGain();
    }

    @Override
    public int getMinGain()
    {
        return minGain;
    }

    @Override
    public int getMaxGain()
    {
        return maxGain;
    }

    @Override
    public int getGain()
    {
        synchronized (lock)
        {
            gain = delegatedGainControl.getGain();
            return gain;
        }
    }

    @Override
    public boolean setGain(int newGain)
    {
        if (newGain >= 0)
        {
            synchronized (lock)
            {
                if (delegatedGainControl.setGain(newGain))
                {
                    gain = newGain;
                    return true;
                }
            }
        }

        return false;
    }
}