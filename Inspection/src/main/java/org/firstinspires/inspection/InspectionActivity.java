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

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.wifi.NetworkType;

import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.network.StartResult;
import org.firstinspires.ftc.robotcore.internal.network.WifiDirectAgent;
import org.firstinspires.ftc.robotcore.internal.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.internal.ui.UILocation;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public abstract class InspectionActivity extends ThemedActivity
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public static final String TAG = "InspectionActivity";
    @Override public String getTag() { return TAG; }
    @Override protected FrameLayout getBackBar() { return findViewById(R.id.backbar); }

    /*
     * To turn on traffic stats on the inspection activities, set this
     * and RecvLoopRunnable.DO_TRAFFIC_DATA to true.
     */
    private static final boolean SHOW_TRAFFIC_STATS = false;

    private static final AppUtil appUtil = AppUtil.getInstance();

    private static final String notApplicable = "N/A";
    private static final String notInstalled = "Not installed";
    private static final String fwUnavailable = "firmware version unavailable";
    private static final String fwMismatch = "mismatched";

    private final boolean remoteConfigure = appUtil.isDriverStation();

    ValidatedInspectionItem airplaneMode, bluetooth, location, rcPassword, wifiEnabled, wifiConnected,
            wifiName, androidVersion, isRCInstalled, rcMatchesDSVersion, isDSInstalled, osVersion,
            firmwareVersion1, firmwareVersion2;

    TextView batteryLevel;
    View hubFirmwareExtraLineLayout, hubFirmwarePrimaryLine_layout;
    TextView trafficCount, bytesPerSecond;
    TextView trafficCountLabel, bytesPerSecondLabel;
    TextView txtManufacturer, txtModel, txtAppVersion;
    TextView osVersionLabel;
    LinearLayout osVersionLayout;
    LinearLayout airplaneModeLayout;
    Pattern teamNoRegex;
    Future refreshFuture = null;
    int textOk = AppUtil.getColor(R.color.text_okay);
    int textWarning = AppUtil.getColor(R.color.text_warning);
    int textError = AppUtil.getColor(R.color.text_error);
    StartResult nameManagerStartResult = new StartResult();
    private boolean properWifiConnectedState;

    protected static final int RC_MIN_VERSIONCODE = 42; // Corresponds to RC 7.0
    protected static final int DS_MIN_VERSIONCODE = 42; // Corresponds to DS 7.0

    class ValidatedInspectionItem
        {
        TextView txt;
        ImageView img;

        ValidatedInspectionItem(int txtId, int imgId)
            {
            this.txt = findViewById(txtId);
            this.img = findViewById(imgId);
            }

        void setVisibility(int visibility)
           {
           txt.setVisibility(visibility);
           img.setVisibility(visibility);
           }
        }

    //----------------------------------------------------------------------------------------------
    // Life cycle
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        // Find our various bits on the screen
        isRCInstalled = new ValidatedInspectionItem(R.id.txtIsRCInstalled, R.id.txtIsRCInstalled_img);
        rcMatchesDSVersion = new ValidatedInspectionItem(R.id.matchesDSVersion_txt, R.id.matchesDSVersion_img);
        isDSInstalled = new ValidatedInspectionItem(R.id.txtIsDSInstalled, R.id.txtIsDSInstalled_img);

        wifiName = new ValidatedInspectionItem(R.id.wifiName, R.id.wifiName_img);
        trafficCount = findViewById(R.id.trafficCount);
        bytesPerSecond = findViewById(R.id.bytesPerSecond);
        trafficCountLabel = findViewById(R.id.trafficCountLabel);
        bytesPerSecondLabel = findViewById(R.id.bytesPerSecondLabel);
        wifiEnabled = new ValidatedInspectionItem(R.id.wifiEnabled, R.id.wifiEnabled_img);
        batteryLevel = findViewById(R.id.batteryLevel);
        androidVersion = new ValidatedInspectionItem(R.id.androidVersion, R.id.androidVersion_img);
        osVersion = new ValidatedInspectionItem(R.id.osVersion, R.id.osVersion_img);
        firmwareVersion1 = new ValidatedInspectionItem(R.id.hubFirmwarePrimaryLine, R.id.hubFirmware_img);
        firmwareVersion2 = new ValidatedInspectionItem(R.id.hubFirmwareExtraLine, R.id.hubFirmwareExtraLine_img);
        hubFirmwareExtraLineLayout = findViewById(R.id.hubFirmwareExtraLine_layout);
        hubFirmwarePrimaryLine_layout = findViewById(R.id.hubFirmwarePrimaryLine_layout);
        airplaneMode = new ValidatedInspectionItem(R.id.airplaneMode, R.id.airplaneMode_img);
        bluetooth = new ValidatedInspectionItem(R.id.bluetoothEnabled, R.id.bluetoothEnabled_img);
        location = new ValidatedInspectionItem(R.id.locationEnabled, R.id.locationEnabled_img);
        wifiConnected = new ValidatedInspectionItem(R.id.wifiConnected, R.id.wifiConnected_img);
        txtAppVersion = findViewById(R.id.textDeviceName);
        rcPassword = new ValidatedInspectionItem(R.id.isDefaultPassword, R.id.isDefaultPassword_img);
        osVersionLabel = findViewById(R.id.osVersionLabel);
        osVersionLayout = findViewById(R.id.osVersionLayout);
        airplaneModeLayout = findViewById(R.id.airplaneModeLayout);

        txtAppVersion.setText(inspectingRobotController()
            ? getString(R.string.titleInspectionReportRC)
            : getString(R.string.titleInspectionReportDS));

        if (!inspectingRobotController())
            {
            rcPassword.setVisibility(View.GONE);
            findViewById(R.id.textViewPassword).setVisibility(View.GONE);
            hubFirmwarePrimaryLine_layout.setVisibility(View.GONE);
            hubFirmwareExtraLineLayout.setVisibility(View.GONE);
            }

        if (!inspectingRemoteDevice())
            {
            // The only time we can know if the versions match is when we are inspecting a remote device
            rcMatchesDSVersion.setVisibility(View.GONE);
            findViewById(R.id.matchesDSVersionLabel).setVisibility(View.GONE);
            }

        txtManufacturer = findViewById(R.id.txtManufacturer);
        txtModel = findViewById(R.id.txtModel);

        teamNoRegex = Pattern.compile("^\\d{1,5}(-\\w)?-(RC|DS)\\z", Pattern.CASE_INSENSITIVE);

        ImageButton buttonMenu = findViewById(R.id.menu_buttons);
        if (useMenu())
            {
            buttonMenu.setOnClickListener(new View.OnClickListener()
                {
                @Override
                public void onClick(View v)
                    {
                    PopupMenu popupMenu = new PopupMenu(InspectionActivity.this, v);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            return onOptionsItemSelected(item); // Delegate to the handler for the hardware menu button
                        }
                    });
                    popupMenu.inflate(R.menu.main_menu);
                    popupMenu.show();
                    }
                });
            }
        else
            {
            buttonMenu.setEnabled(false);
            buttonMenu.setVisibility(View.INVISIBLE);
            }

        DeviceNameManagerFactory.getInstance().start(nameManagerStartResult);

        properWifiConnectedState = false;

        NetworkType networkType = NetworkConnectionHandler.getDefaultNetworkType(this);
        if (networkType == NetworkType.WIRELESSAP)
            {
            makeWirelessAPModeSane();
            }

        enableTrafficDataReporting(SHOW_TRAFFIC_STATS);

        // Off to the races
        refresh();
        }

    protected void enableTrafficDataReporting(boolean enable)
        {
        if (enable)
            {
            trafficCount.setVisibility(View.VISIBLE);
            bytesPerSecond.setVisibility(View.VISIBLE);
            trafficCountLabel.setVisibility(View.VISIBLE);
            bytesPerSecondLabel.setVisibility(View.VISIBLE);
            }
            else
            {
            trafficCount.setVisibility(View.GONE);
            bytesPerSecond.setVisibility(View.GONE);
            trafficCountLabel.setVisibility(View.GONE);
            bytesPerSecondLabel.setVisibility(View.GONE);
            }
        }

    protected void makeWirelessAPModeSane()
        {
        TextView labelWifiName = findViewById(R.id.labelWifiName);
        labelWifiName.setText(getString(R.string.wifiAccessPointLabel));

        properWifiConnectedState = true;
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
        {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
        {
        // Historical note: we used to have other items on the menu as well, but
        // the ability to clear remembered groups is now available on the Settings screen,
        // and the ability to clear all Wi-Fi networks is (apparently) not available from M
        // onwards, and so is now of marginal utility. Thus, both of these items have
        // been removed.

        int id = item.getItemId();
        if (id == R.id.disconnect_from_wifidirect)
            {
            if (!remoteConfigure)
                {
                if (WifiDirectAgent.getInstance().disconnectFromWifiDirect())
                    {
                    showToast(getString(R.string.toastDisconnectedFromWifiDirect));
                    }
                else
                    {
                    showToast(getString(R.string.toastErrorDisconnectingFromWifiDirect));
                    }
                }
            else
                {
                NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_DISCONNECT_FROM_WIFI_DIRECT));
                }
            return true;
            }

        return super.onOptionsItemSelected(item);
        }

    @Override
    protected void onResume()
        {
        super.onResume();
        startRefreshing();
        }

    @Override
    protected void onPause()
        {
        super.onPause();
        stopRefreshing();
        }

    @Override protected void onDestroy()
        {
        super.onDestroy();
        DeviceNameManagerFactory.getInstance().stop(nameManagerStartResult);
        }

    //----------------------------------------------------------------------------------------------
    // Refreshing
    //----------------------------------------------------------------------------------------------

    private void startRefreshing() {
        stopRefreshing();
        int msInterval = 5000;
        refreshFuture = ThreadPool.getDefaultScheduler().scheduleAtFixedRate(new Runnable() {
            @Override public void run() {
                appUtil.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        refresh();
                    }
                });
            }
        }, msInterval, msInterval, TimeUnit.MILLISECONDS);
    }

    private void stopRefreshing() {
        if (refreshFuture != null) {
            refreshFuture.cancel(false);
            refreshFuture = null;
        }
    }

    private void refresh(ValidatedInspectionItem item, boolean valid, String message)
        {
        item.txt.setText(message);
        item.img.setImageResource(valid ? R.drawable.ic_check_circle : R.drawable.ic_error);
        }
    private void refreshTrafficCount(TextView view, long rxData, long txData)
        {
        view.setText(String.format("%d/%d", rxData, txData));
        }
    private void refreshTrafficStats(InspectionState state)
        {
        if (SHOW_TRAFFIC_STATS)
            {
            refreshTrafficCount(trafficCount, state.rxDataCount, state.txDataCount);
            refresh(bytesPerSecond, state.bytesPerSecond);
            }
        }
    private void refresh(TextView view, long data)
        {
        view.setText(String.format("%d", data));
        }

    protected void refresh()
        {
        InspectionState state = new InspectionState();
        state.initializeLocal(DeviceNameManagerFactory.getInstance());
        refresh(state);
        }

    protected void refresh(InspectionState state)
        {
        // Set values

        refresh(wifiEnabled, state.wifiEnabled == true, state.wifiEnabled ? "Yes" : "No");
        refreshTrafficStats(state);
        refresh(bluetooth, state.bluetoothOn == false, state.bluetoothOn ? "Enabled" : "Disabled");
        refresh(wifiConnected, state.wifiConnected == properWifiConnectedState, state.wifiConnected ? "Yes" : "No");

        if (state.sdkInt >= Build.VERSION_CODES.O)
            {
            refresh(location, state.locationEnabled == true, state.locationEnabled ? "Enabled" : "Disabled");
            }
        else
            {
            location.setVisibility(View.GONE);
            findViewById(R.id.locationLabel).setVisibility(View.GONE);
            }

        txtManufacturer.setText(state.manufacturer);
        txtModel.setText(state.model);
        refresh(androidVersion, isValidAndroidVersion(state), state.osVersion);

        if(state.firmwareVersion == null || state.firmwareVersion.equals(notApplicable))
            {
            refresh(firmwareVersion1, false, notApplicable);
            hubFirmwareExtraLineLayout.setVisibility(View.GONE);
            }
        else if(state.firmwareVersion.contains(fwMismatch))
            {
            refresh(firmwareVersion1, false, fwMismatch);
            hubFirmwareExtraLineLayout.setVisibility(View.GONE);
            }
        else
            {
            String[] firmwareStrings = state.firmwareVersion
                    .replace("Expansion Hub", "EH")
                    .replace("Control Hub", "CH")
                    .split("\n");

            refresh(firmwareVersion1, isValidFirmwareVersion(firmwareStrings[0]), firmwareStrings[0].replace(fwUnavailable, notApplicable));

            if(firmwareStrings.length > 1)
                {
                refresh(firmwareVersion2, isValidFirmwareVersion(firmwareStrings[1]), firmwareStrings[1].replace(fwUnavailable, notApplicable));
                }
            else
                {
                hubFirmwareExtraLineLayout.setVisibility(View.GONE);
                }
            }

        refresh(wifiName, isValidDeviceName(state), state.deviceName);
        batteryLevel.setText(Math.round(state.batteryFraction * 100f) + "%");
        batteryLevel.setTextColor(state.batteryFraction > 0.6 ? textOk : textWarning);
        refresh(rcPassword, state.isDefaultPassword == false, state.isDefaultPassword ? "Default" : "Not default");

        // Only display Control Hub / Driver Hub OS version if there is one to display
        boolean inspectingControlHub = !InspectionState.NO_VERSION.equals(state.controlHubOsVersion);
        boolean inspectingDriverHub = !InspectionState.NO_VERSION.equals(state.driverHubOsVersion);
        if (!inspectingControlHub && !inspectingDriverHub)
            {
            osVersionLayout.setVisibility(View.GONE);
            }
        else
            {
            osVersionLayout.setVisibility(View.VISIBLE);
            if (inspectingControlHub)
                {
                osVersionLabel.setText(R.string.controlHubOsVersionLabel);
                refresh(osVersion, isValidControlHubOsVersion(state), state.controlHubOsVersion);
                }
            else
                {
                osVersionLabel.setText(R.string.driverHubOsVersionLabel);
                refresh(osVersion, isValidDriverHubOsVersion(state), state.driverHubOsVersion);
                }
            }

        // Only display airplane mode line on non-REV devices.
        // REV devices don't have cellular radios, and therefore don't need to be in airplane mode.
        if (state.manufacturer.equals(Device.MANUFACTURER_REV))
            {
            airplaneModeLayout.setVisibility(View.GONE);
            }
        else
            {
            airplaneModeLayout.setVisibility(View.VISIBLE);
            refresh(airplaneMode, state.airplaneModeOn == true, state.airplaneModeOn ? "Enabled" : "Disabled");
            }

        // check the installed apps.
        if(inspectingRobotController())
            {
            boolean rcIsObsolete = state.robotControllerBuildTime == null
                    || appUtil.appIsObsolete(appUtil.getYearMonthFromIso8601(state.robotControllerBuildTime));
            boolean validRcState = InspectionState.isPackageInstalled(state.robotControllerVersion) &&
                    state.robotControllerVersionCode >= RC_MIN_VERSIONCODE &&
                    !rcIsObsolete;
            refresh(isRCInstalled,
                    validRcState,
                    state.robotControllerVersion.isEmpty() ? notInstalled : state.robotControllerVersion);
            refresh(isDSInstalled,
                    InspectionState.isPackageInstalled(state.driverStationVersion) == false,
                    state.driverStationVersion.isEmpty() ? notInstalled : state.driverStationVersion);
            if (inspectingRemoteDevice())
                {
                try
                    {
                    int ownVersionCode = AppUtil.getDefContext().getPackageManager().getPackageInfo(AppUtil.getDefContext().getPackageName(), 0).versionCode;
                    boolean codesMatch = state.robotControllerVersionCode == ownVersionCode;
                    refresh(rcMatchesDSVersion, codesMatch, codesMatch ? "Yes" : "No");
                    }
                catch (PackageManager.NameNotFoundException e)
                    {
                    RobotLog.ee(TAG, "Unable to get our own version code (should never happen");
                    }
                }
            }
        else
            {
            boolean dsIsObsolete = state.driverStationBuildTime == null
                    || appUtil.appIsObsolete(appUtil.getYearMonthFromIso8601(state.driverStationBuildTime));
            boolean validDsState = InspectionState.isPackageInstalled(state.driverStationVersion) &&
                    state.driverStationVersionCode >= DS_MIN_VERSIONCODE &&
                    !dsIsObsolete;
            refresh(isDSInstalled,
                    validDsState,
                    state.driverStationVersion.isEmpty() ? notInstalled : state.driverStationVersion);
            refresh(isRCInstalled,
                    InspectionState.isPackageInstalled(state.robotControllerVersion) == false,
                    state.robotControllerVersion.isEmpty() ? notInstalled : state.robotControllerVersion);
            }
        }

    public boolean isValidAndroidVersion(InspectionState state)
        {
        // for 2020-2021 season we require Marshmallow or higher.
        return (state.sdkInt >= Build.VERSION_CODES.M);
        }

    public boolean isValidControlHubOsVersion(InspectionState state)
        {
        return state.controlHubOsVersionNum >= LynxConstants.MINIMUM_LEGAL_CH_OS_VERSION_CODE;
        }

    public boolean isValidDriverHubOsVersion(InspectionState state)
        {
        return state.driverHubOsVersionNum >= LynxConstants.MINIMUM_LEGAL_DH_OS_VERSION_CODE;
        }

    public boolean isValidFirmwareVersion(String string)
        {
        // For the 2021-2022 season, require firmware version 1.8.2
        // When you update this, make sure to update LynxModuleWarningManager at the same time.

        //noinspection RedundantIfStatement
        if (string != null && (
                string.contains("1.6.0") ||
                string.contains("1.7.0") ||
                string.contains("1.7.2") ||
                string.contains(fwUnavailable) ||
                string.contains(fwMismatch)))
            {
            return false;
            }
        return true;
        }

    public boolean isValidDeviceName(InspectionState state)
        {
        if (state.deviceName.contains("\n") || state.deviceName.contains("\r")) return false;
        return (teamNoRegex.matcher(state.deviceName)).find();
        }

    //----------------------------------------------------------------------------------------------
    // Subclass queries
    //----------------------------------------------------------------------------------------------
    protected abstract boolean inspectingRobotController();
    protected abstract boolean inspectingRemoteDevice();
    protected abstract boolean useMenu();

    //----------------------------------------------------------------------------------------------
    // Utility
    //----------------------------------------------------------------------------------------------
    
    private void showToast(String message)
        {
        appUtil.showToast(UILocation.BOTH, message);
        }
    }
