/*
Copyright (c) 2018 Craig MacFarlane

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Craig MacFarlane nor the names of his contributors may be used to
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
package com.qualcomm.robotcore.util;

public final class Intents {

    /**
     * FTC related application specific intents.
     */
    public static final String INTENT_PREFIX = "org.firstinspires.ftc.intent.";
    public static final String ACTION_FTC_AP_NAME_CHANGE = INTENT_PREFIX + "action.FTC_AP_NAME_CHANGE";
    public static final String ACTION_FTC_AP_PASSWORD_CHANGE = INTENT_PREFIX + "action.FTC_AP_PASSWORD_CHANGE";
    public static final String ACTION_FTC_AP_CHANNEL_CHANGE = INTENT_PREFIX + "action.FTC_AP_CHANNEL_CHANGE";
    public static final String ACTION_FTC_AP_SETTINGS_CHANGE = "org.firstinspires.ftc.intent.action.FTC_AP_SETTINGS_CHANGE";
    public static final String ACTION_FTC_WIFI_FACTORY_RESET = INTENT_PREFIX + "action.FTC_FACTORY_RESET";
    public static final String ACTION_FTC_AP_GET_CURRENT_CHANNEL_INFO = INTENT_PREFIX + "action.FTC_AP_GET_CURRENT_CHANNEL_INFO";
    public static final String ACTION_FTC_AP_NOTIFY_BAND_CHANGE = "org.firstinspires.ftc.intent.action.FTC_AP_NOTIFY_BAND_CHANGE";

    public static final String EXTRA_AP_PREF = INTENT_PREFIX + "extra.EXTRA_AP_PREF";

    /**
     * Extras that can be sent with the FTC_AP_SETTINGS_CHANGE action
     */
    public static final String EXTRA_AP_NAME = "org.firstinspires.ftc.intent.extra.EXTRA_AP_NAME";
    public static final String EXTRA_AP_PASSWORD = "org.firstinspires.ftc.intent.extra.EXTRA_AP_PASSWORD";
    public static final String EXTRA_AP_CHANNEL = "org.firstinspires.ftc.intent.extra.EXTRA_AP_CHANNEL";
    // Can also be sent by the AP service with the NOTIFY_BAND_CHANGE broadcast
    public static final String EXTRA_AP_BAND = "org.firstinspires.ftc.intent.extra.EXTRA_AP_BAND";

    /**
     * Extra to provide when we need to receive data back in response to a broadcast via a ResultReceiver
     */
    public static final String EXTRA_RESULT_RECEIVER = "org.firstinspires.ftc.intent.extra.RESULT_RECEIVER";

    /**
     * Key strings for Bundle data sent in response to {@link Intents#ACTION_FTC_AP_GET_CURRENT_CHANNEL_INFO}
     */
    public static final String BUNDLE_KEY_CURRENT_BAND = "current_band";
    public static final String BUNDLE_KEY_CURRENT_CHANNEL = "current_channel";

    /**
     * Android system intents.
     */
    public static final String ANDROID_ACTION_WIFI_AP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";
}
