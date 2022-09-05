/*
Copyright (c) 2017 Robert Atkinson

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
package org.firstinspires.ftc.robotcore.internal.camera.libuvc.nativeobject;

import android.content.Context;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.robotcore.BuildConfig;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;

import org.firstinspires.ftc.robotcore.internal.camera.CameraManagerImpl;
import org.firstinspires.ftc.robotcore.internal.camera.CameraManagerInternal;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.ClassFactoryImpl;
import org.firstinspires.ftc.robotcore.internal.system.Misc;
import org.firstinspires.ftc.robotcore.internal.system.NativeObject;
import org.firstinspires.ftc.robotcore.internal.system.SystemProperties;
import org.firstinspires.ftc.robotcore.internal.usb.UsbConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link UvcContext} is the Java manifestation of a native uvc_context_t, together with
 * some additional Java-level state that should be maintained throughout a UVC session.
 */
@SuppressWarnings("WeakerAccess")
public class UvcContext extends NativeObject
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public static final String TAG = UvcContext.class.getSimpleName();
    public String getTag() { return TAG; }
    public static boolean DEBUG_RS = false; // test what we'll ship, even in DEBUG app builds
    protected static final AtomicInteger instanceCounter = new AtomicInteger(0);
    protected static final UsbManager usbManager = (UsbManager) AppUtil.getDefContext().getSystemService(Context.USB_SERVICE);

    protected final int instanceNumber = instanceCounter.getAndIncrement();

    /**
     * We hold on to a {@link CameraManagerImpl}, but we DON'T ref count it. All we're trying to do is keep
     * same from being GC'd so long as we are alive; this will keep the instance management in
     * {@link ClassFactoryImpl#getCameraManager()} working better. To wit: whenever the UvcDeviceManager
     * ultimately gets GC'd and finalized, we'll be release()d and all will be well.
     */
    protected CameraManagerImpl cameraManagerImpl = null;

    protected @Nullable final String usbFileSystemRoot;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    public UvcContext(CameraManagerImpl cameraManagerImpl, @Nullable String usbFileSystemRoot)
        {
        super(nativeInitContext(usbFileSystemRoot, Build.VERSION.SDK_INT, AppUtil.FIRST_FOLDER.getAbsolutePath(), CameraManagerInternal.forceJavaUsbEnumerationKitKat), TraceLevel.None /*we'll trace ctor*/);
        traceLevel = defaultTraceLevel;
        if (usbFileSystemRoot == null)
            {
            RobotLog.ww(TAG, "creating UvcContext with null usbFileSystemRoot");
            }
        this.usbFileSystemRoot = usbFileSystemRoot;
        this.cameraManagerImpl = cameraManagerImpl;
        if (traceCtor()) RobotLog.vv(getTag(), "construct(%s)", getTraceIdentifier());
        }

    @Override protected void destructor()
        {
        if (pointer != 0)
            {
            nativeExitContext(pointer);
            clearPointer();
            }
        cameraManagerImpl = null;
        super.destructor();
        }

    @Override public String getTraceIdentifier()
        {
        return super.getTraceIdentifier() + Misc.formatInvariant("|inst#=%d", instanceNumber);
        }

    //----------------------------------------------------------------------------------------------
    // Accessing
    //----------------------------------------------------------------------------------------------

    public long getPointer()
        {
        return pointer;
        }

    public @Nullable String getUsbFileSystemRoot()
        {
        return usbFileSystemRoot;
        }

    public CameraManagerImpl getCameraManagerImpl()
        {
        return cameraManagerImpl;
        }

    /** Note that we make up a serial number if the device doesn't actually have one */
    public @Nullable SerialNumber getRealOrVendorProductSerialNumber(UsbDevice usbDevice)
        {
        SerialNumber serialNumber = SerialNumber.fromUsbOrNull(usbDevice.getSerialNumber());

        if (serialNumber==null) // Device lacks real serial number: go the long route so we make up a VendorProductSerialNumber
            {
            LibUsbDevice libUsbDevice = getLibUsbDeviceFromUsbDevice(usbDevice, false);
            if (libUsbDevice != null)
                {
                serialNumber = libUsbDevice.getRealOrVendorProductSerialNumber();
                libUsbDevice.releaseRef();
                }
            }
        return serialNumber;
        }

    public @Nullable LibUsbDevice getLibUsbDeviceFromUsbDevice(UsbDevice usbDevice, boolean traceEnabled)
        {
        synchronized (lock)
            {
            long libUsbDevicePointer = nativeGetLibUsbDeviceFromUsbDeviceName(pointer, usbDevice.getDeviceName());
            if (libUsbDevicePointer != 0)
                {
                return new LibUsbDevice(libUsbDevicePointer, usbDevice, traceEnabled);
                }
            return null;
            }
        }

    //----------------------------------------------------------------------------------------------
    // LibUsb device enumeration
    //----------------------------------------------------------------------------------------------

    protected interface LongConsumer
        {
        void accept(long value);
        }

    //----------------------------------------------------------------------------------------------
    // UVC device enumeration
    //----------------------------------------------------------------------------------------------

    /**
     * See uvc_is_usb_device_compatible()
     */
    protected boolean isUvcCompatible(UsbDevice usbDevice) // throws what?
        {
        UsbConfiguration usbConfiguration = usbDevice.getConfiguration(0);
        int interfaceCount = usbConfiguration.getInterfaceCount();
        for (int i = 0; i < interfaceCount; i++)
            {
            // In the Java model, the USB interface collection is flattened. UsbInterface's
            // are to be distinguished from each other using getId() and getAlternateSetting(),
            // which taken as a pair together uniquely identify the interface.
            UsbInterface usbInterface = usbConfiguration.getInterface(i);
            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_VIDEO && usbInterface.getInterfaceSubclass() == UsbConstants.USB_VIDEO_INTERFACE_SUBCLASS_STREAMING)
                {
                return true;
                }
            }
        return false;
        }

    protected UvcDevice uvcDeviceFrom(UsbDevice usbDevice)
        {
        UvcDevice result = null;
        long pointerUvcDevice = nativeCreateUvcDevice(this.pointer, usbDevice.getDeviceName());
        if (pointerUvcDevice != 0)
            {
            try {
                result = new UvcDevice(pointerUvcDevice, this, usbDevice);
                }
            catch (IOException e)
                {
                RobotLog.ee(TAG, e, "exception processing %s; ignoring", usbDevice.getDeviceName());
                }
            }
        else
            RobotLog.ee(TAG, "nativeCreateUvcDevice() failed");
        return result;
        }

    /**
     * Returns a list of currently attached USB Video class (UVC) cameras. To avoid security
     * issues on Android post KitKat, we do the USB device enumeration here in Java
     *
     * @return a list of currently attached USB Video class (UVC) cameras.
     */
    protected List<UvcDevice> getUvcDeviceListUsingJava() // throws NOTHING
        {
        synchronized (lock)
            {
            final ArrayList<UvcDevice> result = new ArrayList<>();

            for (final UsbDevice usbDevice : usbManager.getDeviceList().values())
                {
                try {
                    // Check for UVC compatibility using java
                    if (isUvcCompatible(usbDevice))
                        {
                        RobotLog.dd(TAG, "found webcam: usbPath=%s vid=0x%X pid=0x%X serial=%s product=%s",
                                usbDevice.getDeviceName(),
                                usbDevice.getVendorId(),
                                usbDevice.getProductId(),
                                usbDevice.getSerialNumber(),
                                usbDevice.getProductName());
                        UvcDevice uvcDevice = uvcDeviceFrom(usbDevice);
                        if (uvcDevice != null)
                            {
                            result.add(uvcDevice); // transfer ownership of the reference
                            }
                        }
                    else
                        RobotLog.dd(TAG, "usb device is *not* UVC compatible, %s", usbDevice.getDeviceName());
                    }
                catch (RuntimeException e)
                    {
                    RobotLog.ee(TAG, e, "exception processing %s; ignoring", usbDevice.getDeviceName());
                    }
                }
            return result;
            }
        }

    public List<UvcDevice> getDeviceList() // throws NOTHING
        {
        return getUvcDeviceListUsingJava();
        }

    //----------------------------------------------------------------------------------------------
    // Native Methods
    //----------------------------------------------------------------------------------------------

    protected native static long nativeInitContext(@Nullable String usbfs, int buildVersionSDKInt, @NonNull String tempFolder, boolean forceJavaUsbEnumerationKitKat);
    protected native static void nativeExitContext(long pointer);

    /** Creates a UvcDevice from whole cloth. */
    protected native static long nativeCreateUvcDevice(long pointer, String usbPath);

    /** Utility for finding serial number from usb path; necessary on KitKat */
    protected native static long nativeGetLibUsbDeviceFromUsbDeviceName(long pointer, String usbDeviceName);
    }
