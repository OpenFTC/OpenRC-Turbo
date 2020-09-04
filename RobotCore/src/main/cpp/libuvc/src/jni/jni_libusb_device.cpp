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
//
// UvcLibUsbDevice methods
//
#include <jni.h>
#include <unistd.h>
#include <libusb.h>
#include <libuvc.h>
extern "C" {
#include <libusbi.h>
}
#include "ftc.h"

#undef TAG
static LPCSTR TAG = "UvcLibUsbDevice";

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_LibUsbDevice_nativeAddRefDevice(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE();
    libusb_device* pDevice = (libusb_device*) pointer;
    if (pDevice)
        {
        libusb_ref_device2(pDevice, "nativeAddRefDevice");
        }
    else
        invalidArgs();
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_LibUsbDevice_nativeReleaseRefDevice(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jboolean traceEnabled)
    {
    FTC_TRACE_ENABLED(traceEnabled);
    libusb_device* pDevice = (libusb_device*) pointer;
    if (pDevice)
        {
        libusb_unref_device2(pDevice, "nativeReleaseRefDevice");
        }
    else
        invalidArgs();
    }

JNIEXPORT jbyte JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_LibUsbDevice_nativeGetPortNumber(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE_VERBOSE();
    libusb_device* pDevice = (libusb_device*) pointer;
    if (pDevice)
        {
        return libusb_get_port_number(pDevice);
        }
    else
        {
        invalidArgs();
        return 0;
        }
    }

JNIEXPORT jbyte JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_LibUsbDevice_nativeGetBusNumber(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE_VERBOSE();
    libusb_device* pDevice = (libusb_device*) pointer;
    if (pDevice)
        {
        return libusb_get_bus_number(pDevice);
        }
    else
        {
        invalidArgs();
        return 0;
        }
    }

JNIEXPORT jint JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_LibUsbDevice_nativeGetVendorId(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE_VERBOSE();
    libusb_device* pDevice = (libusb_device*) pointer;
    if (pDevice)
        {
        libusb_device_descriptor descriptor;
        if (!libusb_get_device_descriptor(pDevice, &descriptor))
            {
            return descriptor.idVendor;
            }
        LOGE("libusb_get_device_descriptor() failed");
        }
    else
        invalidArgs();

    return 0;
    }

JNIEXPORT jint JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_LibUsbDevice_nativeGetProductId(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE_VERBOSE();
    libusb_device* pDevice = (libusb_device*) pointer;
    if (pDevice)
        {
        libusb_device_descriptor descriptor;
        if (!libusb_get_device_descriptor(pDevice, &descriptor))
            {
            return descriptor.idProduct;
            }
        LOGE("libusb_get_device_descriptor() failed");
        }
    else
        invalidArgs();

    return 0;
    }

JNIEXPORT jbyte JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_LibUsbDevice_nativeGetDeviceAddress(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE_VERBOSE();
    libusb_device* pDevice = (libusb_device*) pointer;
    if (pDevice)
        {
        return libusb_get_linux_dev_addr(pDevice);
        }
    else
        {
        invalidArgs();
        return 0;
        }
    }

jstring jstringSerialNumberFromLibUsbDevice(JNIEnv *env, libusb_device *pUsbDevice)
    {
    jstring result = nullptr;
    LPSTR szSerialNumber = NULL;
    int rc = libusb_get_serial_number(pUsbDevice, &szSerialNumber);
    if (!rc)
        {
        LOGV("libusb_get_serial_number: %s", szSerialNumber);
        result = env->NewStringUTF(szSerialNumber);
        free(szSerialNumber);
        }
    else
        LOGE("libusb_get_serial_number failed: %d", rc);

    return result;
    }

JNIEXPORT jstring JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_LibUsbDevice_nativeGetSerialNumber(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jboolean traceEnabled)
    {
    FTC_TRACE_ENABLED(traceEnabled);
    jstring result = nullptr;
    libusb_device* pDevice = (libusb_device*) pointer;
    if (pDevice)
        {
        NATIVE_API_ONE_CALLER_VERBOSE(); // paranoia
        result = jstringSerialNumberFromLibUsbDevice(env, pDevice);
        }
    else
        invalidArgs();

    return result;
    }

JNIEXPORT jstring JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_LibUsbDevice_nativeGetSysfs(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE_VERBOSE();
    jstring result = nullptr;
    libusb_device* pDevice = (libusb_device*) pointer;
    if (pDevice)
        {
        NATIVE_API_ONE_CALLER_VERBOSE(); // paranoia
        if (pDevice->szSysfsDir)
            {
            result = env->NewStringUTF(pDevice->szSysfsDir);
            }
        }
    else
        invalidArgs();

    return result;
    }
