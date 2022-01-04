package com.qualcomm.robotcore.hardware.usb.acm;/*
 *  Copyright (c) 2021 OpenFTC Team
 *
 *  This file is part of AndroidDirectGamepadAccess
 *
 *  AndroidDirectGamepadAccess is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  AndroidDirectGamepadAccess is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Lesser Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with AndroidDirectGamepadAccess. If not, see
 *  <https://www.gnu.org/licenses/>.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class PicoSerialManager
{
    // -----------------------------------------------------------------------------------------------------------------------------------
    //                      -------------- PUBLIC API ------------------
    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Get the singleton instance of DirectAccessGamepadManager
     * @param context an Android context
     * @return the singleton instance of DirectAccessGamepadManager
     */
    public static synchronized PicoSerialManager getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new PicoSerialManager(context);
            instance.initialize();
        }

        return instance;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    //                      -------------- INTERNAL ------------------
    // -----------------------------------------------------------------------------------------------------------------------------------

    private UsbManager usbManager;
    private Context context;
    private BroadcastReceiver usbHotplugReceiver;
    private static final String TAG = "PicoSerialManager";
    private ArrayList<PicoSerialPort> openedPicos = new ArrayList<>();
    private static PicoSerialManager instance = null;

    private final Object closeOpenLock = new Object();

    private PicoSerialManager(Context context)
    {
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.context = context;
    }

    private void initialize()
    {
        registerUsbHotplugReceiver();
    }

    public PicoSerialPort connectDevice(UsbDevice device)
    {
        synchronized (closeOpenLock)
        {
            for(PicoSerialPort port : openedPicos)
            {
                if(port.getId() == device.getDeviceId())
                {
                    return port;
                }
            }

            PicoSerialPort port = new PicoSerialPort(device, usbManager);
            PicoSerialPort.OpenResultCode resultCode = port.openAssumingPermission();

            if(resultCode == PicoSerialPort.OpenResultCode.SUCCESS)
            {
                Log.d(TAG, String.format("Successfully opened USB device id=%d", device.getDeviceId()));

                openedPicos.add(port);

                return port;
            }
            else
            {
                Log.d(TAG, String.format("Failed to open USB device id=%d, resultCode=%s", device.getDeviceId(), resultCode.toString()));
                return null;
            }
        }
    }

    private Collection<UsbDevice> enumerateUsbBus()
    {
        HashMap<String, UsbDevice> map = usbManager.getDeviceList();
        return map.values();
    }

    public ArrayList<UsbDevice> scanForPicos()
    {
        ArrayList<UsbDevice> ret = new ArrayList<>();

        for(UsbDevice device : enumerateUsbBus())
        {
            if(PicoSerialPort.isPico(device))
            {
                ret.add(device);
            }
        }

        return ret;
    }

    private void registerUsbHotplugReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        usbHotplugReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
                {
                    synchronized (closeOpenLock)
                    {
                        UsbDevice dev = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        Log.d(TAG, String.format("ACTION_USB_DEVICE_DETACHED device id=%d vid=0x%X pid=0x%X %s", dev.getDeviceId(), dev.getVendorId(), dev.getProductId(), dev.getDeviceName()));

                        PicoSerialPort portToRemove = null;

                        for(PicoSerialPort port : openedPicos)
                        {
                            if(port.getId() == dev.getDeviceId())
                            {
                                portToRemove = port;
                                break;
                            }
                        }

                        if(portToRemove != null)
                        {
                            Log.d(TAG, String.format("USB device id=%d was hooked by userspace driver; unhooking", dev.getDeviceId()));
                            portToRemove.close();
                            openedPicos.remove(portToRemove);
                        }
                        else
                        {
                            Log.d(TAG, String.format("Removal of USB device id=%d does not require any action", dev.getDeviceId()));
                        }
                    }
                }
            }
        };

        context.getApplicationContext().registerReceiver(usbHotplugReceiver, filter);
    }
}
