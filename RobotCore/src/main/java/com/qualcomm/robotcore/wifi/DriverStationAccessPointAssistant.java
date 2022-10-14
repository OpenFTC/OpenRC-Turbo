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
package com.qualcomm.robotcore.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.robotcore.R;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;

import org.firstinspires.ftc.robotcore.internal.network.ApChannel;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.WifiUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DriverStationAccessPointAssistant extends AccessPointAssistant {

    private static final String TAG = "DSAccessPointAssistant";
    private static final boolean DEBUG = false;
    private static final int DEFAULT_SECONDS_BETWEEN_WIFI_SCANS = 15;

    private static DriverStationAccessPointAssistant wirelessAPAssistant = null;

    private IntentFilter intentFilter;
    private BroadcastReceiver receiver;
    private volatile ConnectStatus connectStatus;
    private ConnectivityManager connectivityManager;

    private ConnectivityManager.NetworkCallback wifiNetworkCallback;

    private final Object enableDisableLock = new Object();

    private static final Object listenersLock = new Object();
    private ArrayList<ConnectedNetworkHealthListener> healthListeners = new ArrayList<>();
    private NetworkHealthPollerThread networkHealthPollerThread;

    protected volatile boolean doContinuousScans;
    private volatile int secondsBetweenWifiScans = DEFAULT_SECONDS_BETWEEN_WIFI_SCANS;
    private final List<ScanResult> scanResults = new ArrayList<ScanResult>();
    protected final WiFiScanRunnable wiFiScanRunnable = new WiFiScanRunnable();

    @Nullable protected ScheduledFuture<?> wifiScanFuture;
    protected final Object wifiScanFutureLock = new Object();

    @Nullable protected ScheduledFuture<?> resetTimeBetweenWiFiScansFuture;
    protected final Object resetTimeBetweenWiFiScansFutureLock = new Object();

    private DriverStationAccessPointAssistant(Context context)
    {
        super(context);
        connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectStatus = ConnectStatus.NOT_CONNECTED;
        doContinuousScans = false;

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    }

    /**
     * getDriverStationAccessPointAssistant
     */
    public synchronized static DriverStationAccessPointAssistant getDriverStationAccessPointAssistant(Context context)
    {
        if (wirelessAPAssistant == null) {
            wirelessAPAssistant = new DriverStationAccessPointAssistant(context);
        }

        return wirelessAPAssistant;
    }

    /**
     * enable
     *
     * Listen for Wi-Fi state changes.
     */
    @Override
    public void enable()
    {
        synchronized (enableDisableLock) {
            if (receiver == null) {
                receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                            handleScanResultsAvailable(intent);
                        }
                    }
                };
                context.registerReceiver(receiver, intentFilter);
            }

            if (wifiNetworkCallback == null) {
                try {
                    wifiNetworkCallback = new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            onWiFiNetworkConnected(network);
                        }

                        @Override
                        public void onLost(@NonNull Network network) {
                            onWiFiNetworkDisconnected(network);
                        }

                        @Override
                        public void onUnavailable() {
                            RobotLog.ee(TAG, "connectivityManager.requestNetwork() failed");
                        }
                    };
                    NetworkRequest wifiNetworkRequest = new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .build();
                  connectivityManager.requestNetwork(wifiNetworkRequest, wifiNetworkCallback);
                } catch (RuntimeException e) {
                    // We need this code to run again if this method gets called again
                    wifiNetworkCallback = null;
                    // Ultimately, exceptions here should be handled at a higher level, so we rethrow
                    throw e;
                }
            }
        }
    }

    /**
     * disable
     *
     * Stop listening for Wi-Fi state changes.
     */
    @Override
    public void disable()
    {
        synchronized (enableDisableLock) {
            if (receiver != null) {
                context.unregisterReceiver(receiver);
                receiver = null;
            }
            if (wifiNetworkCallback != null) {
                // Since wifiNetworkCallback won't get notified past this point if our currently
                // bound network goes away, it's important that we unbind it now
                connectivityManager.bindProcessToNetwork(null);
                try {
                    connectivityManager.unregisterNetworkCallback(wifiNetworkCallback);
                } catch (RuntimeException e) {
                    RobotLog.ww(TAG, "Unable to unregister network callback (it may have never been registered)");
                }
                wifiNetworkCallback = null;
            }
        }
    }

    /**
     * getConnectionOwnerName
     *
     * Returns the ssid of the access point we are currently connected to.
     */
    @Override public String getConnectionOwnerName()
    {
        return WifiUtil.getConnectedSsid();
    }

    @Override
    public void setNetworkSettings(@Nullable String deviceName, @Nullable String password, @Nullable ApChannel channel)
    {
        RobotLog.ee(TAG, "setNetworkProperties not supported on Driver Station");
    }

    /**
     *  discoverPotentialConnections
     *
     *  On disconnect from an already connected access point, start continuous scanning for access points.
     *  The scan results will be used to attempt to find the last known access point and automatically reconnect.
     *  We spend the cycles for continuous scanning as presumably we want to reconnect as quickly as possible.
     *  A typical scenario is pulling the power supply on a control hub that's broadcasting the ssid.
     *
     *  Proactive reconnects mitigate the captive portal problem wherein the underlying OS won't reconnect to
     *  an access point that it's determined has no upstream internet connectivity.
     *
     *  Note that this has a detrimental effect on battery life if left in this state for extended (hours and hours)
     *  periods of time.
     */
    @Override
    public void discoverPotentialConnections()
    {
        RobotLog.vv(TAG, "Starting scans for the most recently connected Control Hub Wi-Fi network");
        doContinuousScans = true;

        synchronized (wifiScanFutureLock) {
            if (wifiScanFuture == null || wifiScanFuture.isDone() || wifiScanFuture.isCancelled()) {
                /*
                 * If we perform a scan too quickly after detecting a disconnect, then it might
                 * still detect the now non-existent SSID. Wait a short period to let whatever
                 * latent state there is floating around clear before we start scanning.
                 */
                wifiScanFuture = ThreadPool.getDefaultScheduler().schedule(wiFiScanRunnable, 1000, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * cancelPotentialConnections
     *
     * Stop the continuous scanning.
     */
    @Override
    public void cancelPotentialConnections()
    {
        RobotLog.vv(TAG, "Stopping scans for the most recently connected Control Hub Wi-Fi network");
        doContinuousScans = false;
        resetSecondsBetweenWiFiScansToDefault();
    }

    /**
     * lookForKnownAccessPoint
     *
     * Looks through a list of ssid's looking for the last known ssid that we
     * had connected to.  If it finds it, reconnect.  This is part of the captive
     * portal mitigation strategy.
     */
    protected boolean lookForKnownAccessPoint(String ssid, String macAddr, List<ScanResult> scanResults)
    {
        if ((ssid == null) || (macAddr == null)) {
            return false;
        }

        if (DEBUG) RobotLog.vv(TAG, "Access point scanResults found: " + scanResults.size());
        if (DEBUG) RobotLog.vv(TAG, "Looking for match to " + ssid + ", " + macAddr);
        for (ScanResult scanResult : scanResults) {
            if (scanResult.SSID.equals(ssid) && scanResult.BSSID.equals(macAddr)) {
                if (DEBUG) RobotLog.ii(TAG, "Found known access point " + scanResult.SSID + ", " + scanResult.BSSID);
                if (connectToAccessPoint(scanResult.SSID) == true) {
                    return true;
                }
                break;
            }
        }

        return false;
    }

    /**
     * handleScanResultsAvailable
     */
    protected void handleScanResultsAvailable(Intent intent)
    {
        PreferencesHelper prefs = new PreferencesHelper(TAG, context);
        String ssid = (String)prefs.readPref(context.getString(R.string.pref_last_known_ssid));
        String macAddr = (String)prefs.readPref(context.getString(R.string.pref_last_known_macaddr));

        scanResults.clear();
        scanResults.addAll(wifiManager.getScanResults());

        if (doContinuousScans == true) {
            if (lookForKnownAccessPoint(ssid, macAddr, scanResults) == false) {
                // We didn't find what we were looking for. Schedule another scan, unless one is already scheduled
                synchronized (wifiScanFutureLock) {
                    if (wifiScanFuture == null || wifiScanFuture.isDone() || wifiScanFuture.isCancelled()) {
                        wifiScanFuture = ThreadPool.getDefaultScheduler().schedule(wiFiScanRunnable, secondsBetweenWifiScans, TimeUnit.SECONDS);
                    }
                }
            } else {
                // We found the network we were looking for, so we can stop looking
                cancelPotentialConnections();
            }
        }
    }

    public void registerNetworkHealthListener(ConnectedNetworkHealthListener listener)
    {
        synchronized (listenersLock) {
            healthListeners.add(listener);
        }
    }

    public void unregisterNetworkHealthListener(ConnectedNetworkHealthListener listener)
    {
        synchronized (listenersLock) {
            healthListeners.remove(listener);
        }
    }

    public void temporarilySetSecondsBetweenWifiScans(int newSecondsBetweenScans, int secondsBeforeReset)
    {
        RobotLog.dd(TAG, "Setting the number of seconds between Wi-Fi scans to %d. This will reset to %d seconds after %d seconds.", newSecondsBetweenScans, DEFAULT_SECONDS_BETWEEN_WIFI_SCANS, secondsBeforeReset);
        synchronized (resetTimeBetweenWiFiScansFutureLock) {
            if (resetTimeBetweenWiFiScansFuture != null) {
                resetTimeBetweenWiFiScansFuture.cancel(false);
            }

            resetTimeBetweenWiFiScansFuture = ThreadPool.getDefaultScheduler().schedule(new Runnable() {
                @Override public void run()
                {
                    resetSecondsBetweenWiFiScansToDefault();
                }
            }, secondsBeforeReset, TimeUnit.SECONDS);
        }
        secondsBetweenWifiScans = newSecondsBetweenScans;
    }

    public void resetSecondsBetweenWiFiScansToDefault()
    {
        if (secondsBetweenWifiScans != DEFAULT_SECONDS_BETWEEN_WIFI_SCANS) {
            RobotLog.dd(TAG, "Resetting time between Wi-Fi scans to the default value of %d seconds", DEFAULT_SECONDS_BETWEEN_WIFI_SCANS);
            secondsBetweenWifiScans = DEFAULT_SECONDS_BETWEEN_WIFI_SCANS;
        }

        synchronized (resetTimeBetweenWiFiScansFutureLock) {
            if (resetTimeBetweenWiFiScansFuture != null) {
                resetTimeBetweenWiFiScansFuture.cancel(false);
                resetTimeBetweenWiFiScansFuture = null;
            }
        }
    }

    public interface ConnectedNetworkHealthListener
    {
        void onNetworkHealthUpdate(int rssi, int linkSpeed);
    }

    public class NetworkHealthPollerThread extends Thread
    {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                int rssi = wifiInfo.getRssi();
                int linkSpeed = wifiInfo.getLinkSpeed();

                synchronized (listenersLock) {
                    for(ConnectedNetworkHealthListener listener : healthListeners) {
                        listener.onNetworkHealthUpdate(rssi, linkSpeed);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void startHealthPoller()
    {
        if (networkHealthPollerThread != null && !networkHealthPollerThread.isInterrupted()) {
            //error
        }
        else {
            networkHealthPollerThread = new NetworkHealthPollerThread();
            networkHealthPollerThread.start();
        }
    }

    private void killHealthPoller()
    {
        if(networkHealthPollerThread != null) {
            networkHealthPollerThread.interrupt();
            networkHealthPollerThread = null;
        }
    }

    protected void onWiFiNetworkConnected(Network network) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        RobotLog.ii(TAG, "onWiFiConnected() called. wifiInfo: " + wifiInfo);
        RobotLog.vv(TAG, "Binding app to new Wi-Fi network.\n" +
                "NOTE: Communication that can be transmitted over a different network (such as Ethernet)" +
                "should use sockets explicitly bound to a different android.net.Network instance.");
        connectivityManager.bindProcessToNetwork(network);

        if (connectStatus == ConnectStatus.NOT_CONNECTED) {
            startHealthPoller();
            connectStatus = ConnectStatus.CONNECTED;
            saveConnectionInfo(wifiInfo);
        }
        sendEvent(NetworkEvent.CONNECTION_INFO_AVAILABLE);
    }

    protected void onWiFiNetworkDisconnected(Network network) {
        RobotLog.ii(TAG, "onWiFiDisconnected() called. Unbinding app.");
        connectivityManager.bindProcessToNetwork(null);
        if (connectStatus == ConnectStatus.CONNECTED) {
            connectStatus = ConnectStatus.NOT_CONNECTED;
            handleWifiDisconnect();
            killHealthPoller();
        }
    }

    /**
     * connectToAccessPoint()
     *
     * Attempt to mitigate the damage done by captive portal detection wherein
     * a device will not automatically reconnect to an access point that it determines
     * has no broader internet access (can't ping a google server).
     */
    protected boolean connectToAccessPoint(String ssid)
    {
        boolean status;

        RobotLog.vv(TAG, "Attempting to auto-connect to " + ssid);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list == null) {
            RobotLog.ee(TAG, "Wi-Fi is likely off");
            return false;
        }

        for (WifiConfiguration i : list) {
            if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                // wifiManager.disconnect();
                status = wifiManager.enableNetwork(i.networkId, true);
                if (status == false) {
                    RobotLog.ww(TAG, "Could not enable " + ssid);
                    return false;
                }
                status = wifiManager.reconnect();
                if (status == false) {
                    RobotLog.ww(TAG, "Could not reconnect to " + ssid);
                    return false;
                }
                break;
            }
        }

        return true;
    }

    @Override
    protected String getIpAddress() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return getIpAddressAsString(wifiInfo.getIpAddress());
    }

    /**
     * handleWifiDisconnect()
     *
     * The driver station disconnected from the AP.  Everything needs to revert to factory reset
     * as pre-existing sockets will not work properly upon a reconnect, even to the same AP.
     */
    private void handleWifiDisconnect()
    {
        RobotLog.vv(TAG, "Handling Wi-Fi disconnect");

        connectStatus = ConnectStatus.NOT_CONNECTED;
        sendEvent(NetworkEvent.DISCONNECTED);

        NetworkConnectionHandler networkConnection = NetworkConnectionHandler.getInstance();
        networkConnection.shutdown();
    }

    /**
     * saveConnectionInfo
     *
     * Caching the last known access point info.
     */
    private void saveConnectionInfo(WifiInfo wifiInfo)
    {
        String ssid = wifiInfo.getSSID().replace("\"", "");
        String macAddr = wifiInfo.getBSSID();

        PreferencesHelper prefs = new PreferencesHelper(TAG, context);
        prefs.writePrefIfDifferent(context.getString(R.string.pref_last_known_ssid), ssid);
        prefs.writePrefIfDifferent(context.getString(R.string.pref_last_known_macaddr), macAddr);
    }


    /**
     * getIpAddressAsString
     */
    private static String getIpAddressAsString(int ipAddress)
    {
        String address =
                String.format("%d.%d.%d.%d",
                        (ipAddress & 0xff),
                        (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff),
                        (ipAddress >> 24 & 0xff));
        return address;
    }

    private class WiFiScanRunnable implements Runnable {
        @Override
        public void run()
        {
            if (doContinuousScans) {
                wifiManager.startScan();
            }
        }
    }
}
