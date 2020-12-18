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
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.PtzControl;
import org.firstinspires.ftc.robotcore.internal.system.Tracer;

/**
 * A {@link PtzControl} that caches state from another gain control
 */
@SuppressWarnings("WeakerAccess")
public class CachingPtzControl implements PtzControl, DelegatingCameraControl
{
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public static final String TAG = "CachingPtzControl";
    public String getTag() { return TAG; }
    public static boolean TRACE = true;
    protected Tracer tracer = Tracer.create(getTag(), TRACE);

    public static boolean isUnknownZoom(int zoom) { return zoom < 0; }
    public static boolean isUnknownPanTilt(PanTiltHolder holder) { return holder == null; }

    protected final Object lock = new Object();
    protected Camera camera = null;
    protected @NonNull PtzControl delegatedPtzControl;
    protected final PtzControl fakePtzControl;

    protected int unknownZoom = -1;

    protected int minZoom = unknownZoom;
    protected int maxZoom = unknownZoom;
    protected int zoom = unknownZoom;

    protected PanTiltHolder maxPanTilt = null;
    protected PanTiltHolder minPanTilt = null;
    protected PanTiltHolder panTilt = null;
    protected PanTiltHolder fakePanTilt = new PanTiltHolder();

    protected boolean limitsInitialized = false;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    public CachingPtzControl()
    {
        fakePtzControl = new PtzControl()
        {
            @Override
            public PanTiltHolder getPanTilt()
            {
                if(!isUnknownPanTilt(panTilt))
                {
                    return panTilt;
                }
                else
                {
                    return fakePanTilt;
                }
            }

            @Override
            public boolean setPanTilt(PanTiltHolder panTiltHolder)
            {
                return false;
            }

            @Override
            public PanTiltHolder getMinPanTilt()
            {
                if(!isUnknownPanTilt(minPanTilt))
                {
                    return minPanTilt;
                }
                else
                {
                    return fakePanTilt;
                }
            }

            @Override
            public PanTiltHolder getMaxPanTilt()
            {
                if(!isUnknownPanTilt(maxPanTilt))
                {
                    return maxPanTilt;
                }
                else
                {
                    return fakePanTilt;
                }
            }

            @Override
            public int getZoom()
            {
                return zoom;
            }

            @Override
            public boolean setZoom(int zoom)
            {
                return false;
            }

            @Override
            public int getMinZoom()
            {
                return minZoom;
            }

            @Override
            public int getMaxZoom()
            {
                return maxZoom;
            }
        };


        delegatedPtzControl = fakePtzControl;
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
                    delegatedPtzControl = camera.getControl(PtzControl.class);
                    if (delegatedPtzControl == null)
                    {
                        delegatedPtzControl = fakePtzControl;
                    }
                    if (!limitsInitialized)
                    {
                        initializeLimits();
                        if (delegatedPtzControl != fakePtzControl)
                        {
                            limitsInitialized = true;
                        }
                    }
                    write();
                    read();
                }
                else
                {
                    delegatedPtzControl = fakePtzControl;
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // GainControl
    //----------------------------------------------------------------------------------------------

    protected void write()
    {
        if (!isUnknownZoom(zoom))
        {
            delegatedPtzControl.setZoom(zoom);
        }

        if(!isUnknownPanTilt(panTilt))
        {
            delegatedPtzControl.setPanTilt(panTilt);
        }
    }

    protected void read()
    {
        zoom = delegatedPtzControl.getZoom();
        panTilt = delegatedPtzControl.getPanTilt();

        if (!limitsInitialized)
        {
            initializeLimits();
        }
    }

    //----------------------------------------------------------------------------------------------

    void initializeLimits()
    {
        minZoom = delegatedPtzControl.getMinZoom();
        maxZoom = delegatedPtzControl.getMaxZoom();

        minPanTilt = delegatedPtzControl.getMinPanTilt();
        maxPanTilt = delegatedPtzControl.getMaxPanTilt();
    }

    @Override
    public PanTiltHolder getPanTilt()
    {
        synchronized (lock)
        {
            panTilt = delegatedPtzControl.getPanTilt();
            return panTilt;
        }
    }

    @Override
    public boolean setPanTilt(PanTiltHolder newPanTilt)
    {
        if (newPanTilt != null)
        {
            synchronized (lock)
            {
                if (delegatedPtzControl.setPanTilt(newPanTilt))
                {
                    panTilt = newPanTilt;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public PanTiltHolder getMinPanTilt()
    {
        return minPanTilt;
    }

    @Override
    public PanTiltHolder getMaxPanTilt()
    {
        return maxPanTilt;
    }

    @Override
    public int getZoom()
    {
        synchronized (lock)
        {
            zoom = delegatedPtzControl.getZoom();
            return zoom;
        }
    }

    @Override
    public boolean setZoom(int newZoom)
    {
        if (newZoom >= 0)
        {
            synchronized (lock)
            {
                if (delegatedPtzControl.setZoom(newZoom))
                {
                    zoom = newZoom;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int getMinZoom()
    {
        return minZoom;
    }

    @Override
    public int getMaxZoom()
    {
        return maxZoom;
    }
}