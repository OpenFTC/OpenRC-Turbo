// Copyright 2016 Google Inc.

package org.firstinspires.ftc.robotserver.internal.webserver;

import com.google.gson.annotations.SerializedName;
import com.qualcomm.robotcore.util.Device;

import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class PingResponse
    {
    public static final long EXPIRATION_DURATION_SECONDS = 3;
    private static final Comparator<ConnectedHttpDevice> CONNECTED_HTTP_DEVICE_COMPARATOR = new Comparator<ConnectedHttpDevice>()
        {
        @Override
        public int compare(ConnectedHttpDevice connectedHttpDevice1, ConnectedHttpDevice connectedHttpDevice2)
            {
            int result = connectedHttpDevice1.machineName.compareToIgnoreCase(connectedHttpDevice2.machineName);
            if (result == 0)
                {
                result = connectedHttpDevice1.currentPage.compareToIgnoreCase(connectedHttpDevice2.currentPage);
                }
            return result;
            }
        };

    private final transient Object pingLock = new Object();
    private final transient List<Long> pingTimes = new LinkedList<>();
    private final transient List<ConnectedHttpDevice> connectedDevices = new LinkedList<>();

    // Fields exposed via Gson (non-transient)
    private final String serial = Device.getSerialNumberOrUnknown();
    @SerializedName(value="connectedDevices") private List<ConnectedHttpDevice> sortedConnectedDevices = new ArrayList<>();

    public void noteDevicePing(ConnectedHttpDevice device)
        {
        synchronized (pingLock)
            {
            long now = System.nanoTime();
            int index = connectedDevices.indexOf(device);
            if (index != -1)
                {
                pingTimes.remove(index);
                connectedDevices.remove(index);
                }
            pingTimes.add(now);
            connectedDevices.add(device);
            }
        }

    public void removeOldPings()
        {
        synchronized (pingLock)
            {
            long minimum = System.nanoTime() - TimeUnit.SECONDS.toNanos(EXPIRATION_DURATION_SECONDS);
            while (!pingTimes.isEmpty() && pingTimes.get(0) < minimum)
                {
                pingTimes.remove(0);
                connectedDevices.remove(0);
                }
            }
        }

    private void updateSortedConnectedDevices()
        {
        synchronized (pingLock)
            {
            sortedConnectedDevices = new ArrayList<>(connectedDevices);
            Collections.sort(sortedConnectedDevices, CONNECTED_HTTP_DEVICE_COMPARATOR);
            }
        }

    public String toJson()
        {
        updateSortedConnectedDevices();
        return SimpleGson.getInstance().toJson(this);
        }
    }
