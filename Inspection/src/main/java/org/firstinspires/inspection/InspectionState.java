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
package org.firstinspires.inspection;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;

import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.wifi.NetworkType;

import org.firstinspires.ftc.robotcore.internal.hardware.CachedLynxFirmwareVersions;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManager;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.PasswordManagerFactory;
import org.firstinspires.ftc.robotcore.internal.network.WifiUtil;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.internal.network.StartResult;
import org.firstinspires.ftc.robotcore.internal.network.WifiDirectAgent;

import java.util.List;

/**
 * {@link InspectionState} contains the inspection state of either a RC or a DS
 */
@SuppressWarnings("WeakerAccess")
public class InspectionState
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public static final String robotControllerPackage = "com.qualcomm.ftcrobotcontroller";
    public static final String driverStationPackage = "com.qualcomm.ftcdriverstation";

    public static final String NO_VERSION = "";
    public static final int NO_VERSION_CODE = 0;

    public String manufacturer;
    public String model;
    public String osVersion; // Android version (e.g. 7.1.1)
    public String controlHubOsVersion; // Control Hub OS version (e.g. 1.1.1)
    public String firmwareVersion; // TODO(Noah): The next time we bump Robocol, send a list of firmware versions instead
    public int sdkInt;
    public int controlHubOsVersionNum;
    public boolean airplaneModeOn;
    public boolean bluetoothOn;
    public boolean wifiEnabled;
    public boolean wifiConnected;
    public boolean wifiDirectEnabled;
    public boolean wifiDirectConnected;
    public String deviceName;
    public double batteryFraction;
    public String robotControllerVersion;
    public int    robotControllerVersionCode;
    public String driverStationVersion;
    public int    driverStationVersionCode;
    public long    rxDataCount;
    public long    txDataCount;
    public long    bytesPerSecond;
    public boolean isDefaultPassword;

    // Legacy fields that can be removed once the Robocol version has been moved past 121
    public String zteChannelChangeVersion = NO_VERSION;
    public int    ztcChannelChangeVersionCode = NO_VERSION_CODE;
    public boolean channelChangerRequired = false;
    public boolean isAppInventorInstalled = false;

    //----------------------------------------------------------------------------------------------
    // Construction and initialization
    //----------------------------------------------------------------------------------------------

    public InspectionState()
        {
        // For deserialization, initialize CH OS version to NO_VERSION value.
        // Otherwise, it will be null when the RC is running 5.x
        this.controlHubOsVersion = NO_VERSION;
        }

    public void initializeLocal()
        {
        DeviceNameManager nameManager = DeviceNameManagerFactory.getInstance();
        StartResult startResult = new StartResult();
        nameManager.start(startResult);
        initializeLocal(nameManager);
        nameManager.stop(startResult);
        }

    public void initializeLocal(DeviceNameManager nameManager)
        {
        this.manufacturer = Build.MANUFACTURER;
        this.model = Build.MODEL;
        this.osVersion = Build.VERSION.RELEASE;
        this.firmwareVersion = getFirmwareInspectionVersions();
        this.sdkInt = Build.VERSION.SDK_INT;
        this.airplaneModeOn = WifiUtil.isAirplaneModeOn();
        this.bluetoothOn = WifiUtil.isBluetoothOn();
        this.wifiEnabled = WifiUtil.isWifiEnabled();
        this.batteryFraction = getLocalBatteryFraction();

        this.robotControllerVersion         = getPackageVersion(robotControllerPackage);
        this.robotControllerVersionCode     = getPackageVersionCode(robotControllerPackage);
        this.driverStationVersion           = getPackageVersion(driverStationPackage);
        this.driverStationVersionCode       = getPackageVersionCode(driverStationPackage);
        this.deviceName                     = nameManager.getDeviceName();

        NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
        NetworkType networkType = networkConnectionHandler.getNetworkType();
        if (networkType == NetworkType.WIRELESSAP || networkType == NetworkType.RCWIRELESSAP)
            {
            if (Device.isRevControlHub())
                {
                this.controlHubOsVersion = LynxConstants.getControlHubOsVersion();
                if (this.controlHubOsVersion == null)
                    {
                    this.controlHubOsVersion = "unknown";
                    }
                this.controlHubOsVersionNum = LynxConstants.getControlHubOsVersionNum();
                this.isDefaultPassword = PasswordManagerFactory.getInstance().isDefault();
                this.wifiEnabled = WifiUtil.isWifiApEnabled();
                if (this.wifiEnabled) this.wifiConnected = true;
                }
                else
                {
                this.controlHubOsVersion = NO_VERSION;
                this.controlHubOsVersionNum = NO_VERSION_CODE;
                this.deviceName = WifiUtil.getConnectedSsid();
                this.wifiDirectEnabled = WifiUtil.isWifiEnabled();
                this.wifiConnected = WifiUtil.isWifiConnected();
                }
            this.wifiDirectConnected = false;  // Not shown on inspection activity.  Why does it exist?
            }
            else
            {
            this.wifiConnected = WifiDirectAgent.getInstance().isWifiConnected();
            this.wifiDirectEnabled = WifiDirectAgent.getInstance().isWifiDirectEnabled();
            this.wifiDirectConnected = WifiDirectAgent.getInstance().isWifiDirectConnected();
            }
        this.rxDataCount = networkConnectionHandler.getRxDataCount();
        this.txDataCount = networkConnectionHandler.getTxDataCount();
        this.bytesPerSecond = networkConnectionHandler.getBytesPerSecond();
        }

    public static boolean isPackageInstalled(String packageVersion) { return !packageVersion.equals(NO_VERSION); }

    public boolean isRobotControllerInstalled()
        {
        return isPackageInstalled(robotControllerVersion);
        }
    public boolean isDriverStationInstalled()
        {
        return isPackageInstalled(driverStationVersion);
        }

    protected double getLocalBatteryFraction()
        {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = AppUtil.getInstance().getApplication().registerReceiver(null, intentFilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level / (double) scale;
        }

    protected int getPackageVersionCode(String packageName)
        {
        PackageManager pm = AppUtil.getDefContext().getPackageManager();
        try
            {
            return pm.getPackageInfo(packageName, PackageManager.GET_META_DATA).versionCode;
            }
        catch (PackageManager.NameNotFoundException e)
            {
            return NO_VERSION_CODE;
            }
        }

    protected String getPackageVersion(String packageName)
        {
        PackageManager pm = AppUtil.getDefContext().getPackageManager();
        try
            {
            return pm.getPackageInfo(packageName, PackageManager.GET_META_DATA).versionName;
            }
        catch (PackageManager.NameNotFoundException e)
            {
            return NO_VERSION;
            }
        }

    //----------------------------------------------------------------------------------------------
    // Serialization
    //----------------------------------------------------------------------------------------------

    public String serialize()
        {
        return SimpleGson.getInstance().toJson(this);
        }

    public static InspectionState deserialize(String serialized)
        {
        return SimpleGson.getInstance().fromJson(serialized, InspectionState.class);
        }

    //----------------------------------------------------------------------------------------------
    // Firmware determination
    //----------------------------------------------------------------------------------------------

    /*
     * getFirmwareDisplayVersion
     *
     * Returns displayable text that we can use for firmware versions on the Inspection activity.
     */
    private static String getFirmwareInspectionVersions()
        {
        List<CachedLynxFirmwareVersions.LynxModuleInfo> versions = CachedLynxFirmwareVersions.getFormattedVersions();

        if (versions ==  null || versions.isEmpty())
            {
            return "N/A";
            }

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < versions.size(); i++)
            {
            stringBuilder.append(String.format("[%s] %s", versions.get(i).name, versions.get(i).firmwareVersion));

            // Append carriage returns, but not for the last item
            if(i < versions.size()-1)
                {
                stringBuilder.append("\n");
                }
            }

        return stringBuilder.toString();
        }
    }
