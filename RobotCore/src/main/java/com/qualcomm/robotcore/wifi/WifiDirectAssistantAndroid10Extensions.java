/*
 * Copyright (c) 2022 Michael Hoogasian
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

package com.qualcomm.robotcore.wifi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

// This class is needed to prevent logspam on Android versions prior to 10
// due to the nonexistence of WifiP2pManager.DeviceInfoListener
public class WifiDirectAssistantAndroid10Extensions
{
    static abstract class DelegateDeviceInfoListener
    {
        abstract void onDeviceInfoAvailable(@Nullable WifiP2pDevice wifiP2pDevice);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void handleRegisterBroadcastReceiver(WifiP2pManager mgr, WifiP2pManager.Channel channel, final DelegateDeviceInfoListener listener)
    {
        // In Android versions prior to 10, WIFI_P2P_THIS_DEVICE_CHANGED_ACTION and
        // WIFI_P2P_CONNECTION_CHANGED_ACTION were sticky broadcasts, so re-registering the broadcast
        // receiver was enough to guarantee that we would be updated with the current name. In
        // Android 10+, we have to explicitly request the latest information the first time
        // (any changes after that will still be broadcast)

        if (ContextCompat.checkSelfPermission(AppUtil.getDefContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            throw new RuntimeException("We do NOT have permission to access fine location");
        }
        mgr.requestDeviceInfo(channel, new WifiP2pManager.DeviceInfoListener() {
            @Override
            public void onDeviceInfoAvailable(@Nullable WifiP2pDevice wifiP2pDevice) {
                listener.onDeviceInfoAvailable(wifiP2pDevice);
            }
        });
    }
}
