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
package com.qualcomm.ftccommon;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.FrameLayout;

import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.NetworkType;

import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.ftc.robotcore.internal.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.internal.ui.UILocation;

/**
 * {@link FtcAdvancedRCSettingsActivity} manages the editing of advanced RC settings
 */
@SuppressWarnings("WeakerAccess")
public class FtcAdvancedRCSettingsActivity extends ThemedActivity
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public static final String TAG = "FtcAdvancedRCSettingsActivity";
    @Override public String getTag() { return TAG; }
    @Override protected FrameLayout getBackBar() { return findViewById(org.firstinspires.inspection.R.id.backbar); }

    //----------------------------------------------------------------------------------------------
    // Life Cycle
    //----------------------------------------------------------------------------------------------

    protected static final String CLIENT_CONNECTED = "CLIENT_CONNECTED";

    public static class SettingsFragment extends PreferenceFragment
        {
        protected boolean clientConnected = false;
        protected boolean remoteConfigure = AppUtil.getInstance().isDriverStation();
        protected boolean controlHubConnectionMode = NetworkConnectionHandler.getDefaultNetworkType(AppUtil.getDefContext()) == NetworkType.WIRELESSAP;
        protected PreferencesHelper preferencesHelper = new PreferencesHelper(TAG);

        @Override
        public void onCreate(Bundle savedInstanceState)
            {
            super.onCreate(savedInstanceState);

            clientConnected = getArguments().getBoolean(CLIENT_CONNECTED);

            addPreferencesFromResource(R.xml.advanced_rc_settings);

            Preference prefEditClearRememberedGroups = findPreference(getString(R.string.pref_launch_wifi_remembered_groups_edit));
            Preference prefChangeChannel = findPreference(getString(R.string.pref_launch_wifi_channel_edit));
            Preference prefLynxFirmwareUpdateMode = findPreference(getString(R.string.pref_launch_lynx_firmware_update));

            // If we are a Control Hub, or are set up to connect to a Control Hub, adjust text accordingly
            if (LynxConstants.isRevControlHub() || controlHubConnectionMode)
                {
                prefLynxFirmwareUpdateMode.setSummary(R.string.summaryLynxFirmwareUpdateCH);
                }

            // TODO(CHv1): Update this channel changer to work for both Wifi Direct and the CH

            // If we're not connected to RC, then disable ALL controls on this screen
            if (!clientConnected)
                {
                for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++)
                    {
                    getPreferenceScreen().getPreference(i).setEnabled(false);
                    }
                }

            // TODO(Noah): At the next Robocol bump, replace this with a special connected_to_control_hub shared pref
            // Disable WiFi Direct settings when managing a Control Hub
            boolean changingControlHubSettings = (remoteConfigure && LynxConstants.isRevControlHub()) ||
                    (clientConnected && !preferencesHelper.readBoolean(getString(R.string.pref_wifip2p_remote_channel_change_works), false));
            prefChangeChannel.setEnabled(!changingControlHubSettings);
            prefEditClearRememberedGroups.setEnabled(!changingControlHubSettings);

            RobotLog.vv(TAG, "clientConnected=%s", clientConnected);
            }
        }

    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_settings);

        // Always make sure we have a real device name before we launch
        DeviceNameManagerFactory.getInstance().initializeDeviceNameIfNecessary();

        // Display the fragment as the main content.
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(CLIENT_CONNECTED, new PreferencesHelper(TAG, this).readBoolean(getString(R.string.pref_rc_connected), false));
        settingsFragment.setArguments(arguments);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, settingsFragment)
                .commit();
        }
    }
