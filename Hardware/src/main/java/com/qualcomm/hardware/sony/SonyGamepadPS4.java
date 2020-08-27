/*
 * Copyright (c) 2020 FIRST
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

package com.qualcomm.hardware.sony;

import android.view.MotionEvent;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.internal.usb.UsbConstants;

public class SonyGamepadPS4 extends Gamepad {

    // When the PS4 controller is activated, each trigger reports that it is
    // mid-stroke until it is actually moved. From that point, it works as expected.
    // So we need to ignore the trigger until the first movement is detected.
    protected boolean TriggerFixLeft = false;
    protected boolean TriggerFixRight = false;

    public SonyGamepadPS4() {
        this(null);
    }

    public SonyGamepadPS4(GamepadCallback callback) {
        super(callback);

        // calibrate the device
        joystickDeadzone = 0.05f;
    }

    /**
     * Update the gamepad based on a KeyEvent
     * @param event key event
     */
    @Override
    public void update(android.view.KeyEvent event) {

        setGamepadId(event.getDeviceId());
        setTimestamp(event.getEventTime());

        int key = event.getKeyCode();

        if (key == 97) a = pressed(event);
        else if (key == 98) b = pressed(event);
        else if (key == 96) x = pressed(event);
        else if (key == 99) y = pressed(event);
        else if (key == 110) guide = pressed(event);
        else if (key == 105) start = pressed(event);
        else if (key == 104) back = pressed(event);
        else if (key == 106) touchpad = pressed(event);

        else if (key == 101) right_bumper = pressed(event);
        else if (key == 100) left_bumper = pressed(event);
        else if (key == 109) left_stick_button = pressed(event);
        else if (key == 108) right_stick_button = pressed(event);

        updateButtonAliases();
        callCallback();
    }

    /**
     * Update the gamepad based on a MotionEvent
     * @param event motion event
     */
    @Override
    public void update(android.view.MotionEvent event) {

        setGamepadId(event.getDeviceId());
        setTimestamp(event.getEventTime());

        left_stick_x = cleanMotionValues(event.getAxisValue(MotionEvent.AXIS_X));
        left_stick_y = cleanMotionValues(event.getAxisValue(MotionEvent.AXIS_Y));
        right_stick_x = cleanMotionValues(event.getAxisValue(MotionEvent.AXIS_Z));
        right_stick_y = cleanMotionValues(event.getAxisValue(MotionEvent.AXIS_RZ));

        float lt = event.getAxisValue(12) * 0.5f + 0.5f;
        if(TriggerFixLeft) {
            left_trigger = lt;
        } else {
            left_trigger = 0.0f;
            TriggerFixLeft = (lt != 0.5f);
        }

        float rt = event.getAxisValue(13) * 0.5f + 0.5f;
        if(TriggerFixRight) {
            right_trigger = rt;
        } else {
            right_trigger = 0.0f;
            TriggerFixRight = (rt != 0.5f);
        }

        dpad_down = event.getAxisValue(MotionEvent.AXIS_HAT_Y) > dpadThreshold;
        dpad_up = event.getAxisValue(MotionEvent.AXIS_HAT_Y) < -dpadThreshold;
        dpad_right = event.getAxisValue(MotionEvent.AXIS_HAT_X) > dpadThreshold;
        dpad_left = event.getAxisValue(MotionEvent.AXIS_HAT_X) < -dpadThreshold;

        updateButtonAliases();
        callCallback();
    }

    @Override
    public Type type() {
        return Type.SONY_PS4;
    }

    /**
     * Display a summary of this gamepad, including the state of all buttons, analog sticks, and triggers
     * @return a summary
     */
    @Override
    public String toString() {
        return ps4ToString();
    }

    public static boolean matchesVidPid(int vid, int pid) {
        boolean match = vid == UsbConstants.VENDOR_ID_SONY && pid == UsbConstants.PRODUCT_ID_SONY_GAMEPAD_PS4;
        // White-label wired PS4 controller clone
        match |= vid == 0x7545 && pid == 0x104;

        return match;
    }
}