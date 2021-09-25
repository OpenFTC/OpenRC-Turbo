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
// UvcContext methods
//
#include <jni.h>
#include <unistd.h>
#include <libuvc.h>
#include <libuvc/libuvc_internal.h>
#include "ftc.h"

#undef TAG
static LPCSTR TAG = "UvcContext";

JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcContext_nativeInitContext(JNIEnv *env, jclass type,
        /*nullable*/ jstring usbfs,
        jint buildVersionSDKInt,
        jstring tempFolder,
        jboolean forceJavaUsbEnumerationKitKat)
    {
    uvc_error rc = UVC_SUCCESS;
    FTC_TRACE_VERBOSE("buildVersionSDKInt=%d", buildVersionSDKInt);
    NATIVE_API_ONE_CALLER_VERBOSE();
    uvc_context* pContext = NULL;
    LPCSTR szUsbfsChars = nullptr;
    LPCSTR szTempFolderChars = nullptr;
    jboolean isCopy;
    if (usbfs)
        {
        szUsbfsChars = env->GetStringUTFChars(usbfs, &isCopy);
        if (!szUsbfsChars)
            {
            rc = outOfMemory();
            }
        }

    if (!rc && tempFolder)
        {
        szTempFolderChars = env->GetStringUTFChars(tempFolder, &isCopy);
        if (!szTempFolderChars)
            {
            rc = outOfMemory();
            }
        }

    if (!rc)
        {
        rc = uvc_init(&pContext, szUsbfsChars, buildVersionSDKInt, szTempFolderChars, !!forceJavaUsbEnumerationKitKat);
        if (rc != UVC_SUCCESS)
            {
            LOGE("failed: uvc_init(): %d:%s", rc, uvcErrorName(rc));
            }
        }

    if (usbfs && szUsbfsChars)
        {
        env->ReleaseStringUTFChars(usbfs, szUsbfsChars);
        }
    if (tempFolder && szTempFolderChars)
        {
        env->ReleaseStringUTFChars(tempFolder, szTempFolderChars);
        }

    return (JNI_NATIVE_POINTER)pContext;
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcContext_nativeExitContext(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE_VERBOSE();
    uvc_context* pContext = (uvc_context*)pointer;
    if (pContext)
        {
        NATIVE_API_ONE_CALLER_VERBOSE();
        uvc_exit(pContext);
        }
    else
        invalidArgs();
    }

/**
 * Creates a uvc_device from whole cloth. Caller is responsible for verifying that the device in question
 * is in fact UVC compatible.
 */
JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcContext_nativeCreateUvcDevice(JNIEnv *env, jclass type,
        JNI_NATIVE_POINTER pointer, jstring strUsbPath)
    {
    FTC_TRACE();
    uvc_context* pContext = reinterpret_cast<uvc_context*>(pointer);
    uvc_device* pResult = nullptr;

	if (pContext && strUsbPath)
        {
        NATIVE_API_ONE_CALLER();

        jboolean isCopy;
        LPCSTR szUsbPath = env->GetStringUTFChars(strUsbPath, &isCopy);
        if (szUsbPath)
            {
            libusb_device* pLibUsbDevice = libusb_create(pContext->pLibUsbContext, szUsbPath); // kadflkjadsf
            if (pLibUsbDevice)
                {
                uvc_device* pUvcDevice = nullptr;
                uvc_error rc = uvc_create_uvc_device(pContext, pLibUsbDevice, &pUvcDevice);
                if (pUvcDevice)
                    {
                    pResult = pUvcDevice;
                    }
                else
                    LOGE("uvc_device_from_libusb_device failed: rc=%d", rc);

                libusb_unref_device2(pLibUsbDevice, "nativeCreateUvcDevice"); // kadflkjadsf pUvcDevice grabbed its own ref if it needed to
                }
            else
                LOGE("libusb_create(%s) failed", szUsbPath);

            env->ReleaseStringUTFChars(strUsbPath, szUsbPath);
            }
        else
            outOfMemory();
        }
    else
        invalidArgs();

    return (JNI_NATIVE_POINTER)pResult;
    }

jstring jstringSerialNumberFromLibUsbDevice(JNIEnv *env, libusb_device *pUsbDevice);

JNIEXPORT jstring JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcContext_nativeGetSerialNumberFromUsbPath(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jstring strUsbDeviceName)
    {
    FTC_TRACE();
    uvc_context* pContext = reinterpret_cast<uvc_context*>(pointer);
    jstring result = nullptr;

	if (pContext && strUsbDeviceName)
        {
        NATIVE_API_ONE_CALLER();

        jboolean isCopy;
        LPCSTR szUsbDeviceName = env->GetStringUTFChars(strUsbDeviceName, &isCopy);
        if (szUsbDeviceName)
            {
            libusb_device* pLibUsbDevice = libusb_create(pContext->pLibUsbContext, szUsbDeviceName); // laklkj3490z
            if (pLibUsbDevice)
                {
                result = jstringSerialNumberFromLibUsbDevice(env, pLibUsbDevice);
                libusb_unref_device2(pLibUsbDevice, "nativeGetSerialNumberFromUsbPath"); // laklkj3490z
                }
            else
                LOGE("libusb_create(%s) failed", szUsbDeviceName);

            env->ReleaseStringUTFChars(strUsbDeviceName, szUsbDeviceName);
            }
        else
            outOfMemory();
        }
    else
        invalidArgs();

    return result;
    }

JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcContext_nativeGetLibUsbDeviceFromUsbDeviceName(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jstring strUsbDeviceName)
    {
    FTC_TRACE_VERBOSE();
    uvc_context* pContext = reinterpret_cast<uvc_context*>(pointer);
    libusb_device* pResultDevice = nullptr;

	if (pContext && strUsbDeviceName)
        {
        NATIVE_API_ONE_CALLER_VERBOSE();

        jboolean isCopy;
        LPCSTR szUsbDeviceName = env->GetStringUTFChars(strUsbDeviceName, &isCopy);
        if (szUsbDeviceName)
            {
            libusb_device* pLibUsbDevice = libusb_create(pContext->pLibUsbContext, szUsbDeviceName); // laklkj3490z
            if (pLibUsbDevice)
                {
                pResultDevice = pLibUsbDevice;
                }
            else
                LOGE("libusb_create(%s) failed", szUsbDeviceName);

            env->ReleaseStringUTFChars(strUsbDeviceName, szUsbDeviceName);
            }
        else
            outOfMemory();
        }
    else
        invalidArgs();

    return reinterpret_cast<JNI_NATIVE_POINTER>(pResultDevice);
    }

/** enumerates all currently attached libusb devices, whether they are UVC devices or not */
JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcContext_nativeEnumerateAttachedLibUsbDevicesKitKat(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jobject consumerFunction)
    {
    FTC_TRACE();
    uvc_context* pContext = reinterpret_cast<uvc_context*>(pointer);
	if (pContext && consumerFunction)
        {
        LPCSTR methodName = "accept";
        jmethodID methodId = findMethod(env, consumerFunction, methodName, "(" JNI_POINTER ")" JNI_VOID);
        libusb_device** rgpUsbDevice = nullptr;
        int cUsbDevice = 0;
        if (methodId != nullptr)
            {
            NATIVE_API_ONE_CALLER(); // paranoia
            cUsbDevice = libusb_get_device_list_kitkat(pContext->pLibUsbContext, &rgpUsbDevice); // warning: libusb_get_device_list_kitkat doesn't work on Lollipop or greater: see op_init()
            if (cUsbDevice >= 0)
                {
                }
            else
                LOGE("libusb_get_device_list() failed: rc=%d", cUsbDevice);
            }
        else
            LOGE("unable to find \"%s\" method", methodName);

        if (rgpUsbDevice != nullptr)
            {
            for (int iUsbDevice = 0; iUsbDevice < cUsbDevice; iUsbDevice++)
                {
                libusb_device* pUsbDevice = rgpUsbDevice[iUsbDevice];
                libusb_ref_device2(pUsbDevice, "nativeEnumerateAttachedLibUsbDevicesKitKat"); // callback needs a ref to own
                env->CallVoidMethod(consumerFunction, methodId, (JNI_NATIVE_POINTER)pUsbDevice);
                }
            libusb_free_device_list(rgpUsbDevice, true); // free all the (original) refs that came f
            }
        }
    else
        invalidArgs();
    }


/** returns the list of currently attached UVC devices */
JNIEXPORT jlongArray JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcContext_nativeGetUvcDeviceListKitKat(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE();
	uvc_context* pContext = (uvc_context*)pointer;
	jlongArray result = NULL;
	if (pContext != NULL)
        {
        NATIVE_API_ONE_CALLER();
        uvc_device** rgDevice = NULL;
        uvc_error ret = uvc_get_device_listKitKat(pContext, &rgDevice);
        if (!ret)
            {
            int cDevice = 0;
            while (rgDevice[cDevice] != NULL)
                {
                cDevice++;
                }

            bool unrefDevices = true;
            result = env->NewLongArray(cDevice);
            if (result)
                {
                jboolean isCopy;
                jlong* rgDeviceL = env->GetLongArrayElements(result, &isCopy);
                if (rgDeviceL)
                    {
                    unrefDevices = false;
                    for (int iDevice = 0; iDevice < cDevice; iDevice++)
                        {
                        rgDeviceL[iDevice] = (jlong)rgDevice[iDevice];
                        }
                    env->ReleaseLongArrayElements(result, rgDeviceL, 0);
                    }
                }
            else
                LOGE("failed to allocate dev list: %d devices ", cDevice);

            uvc_free_device_list(rgDevice, unrefDevices);
            }
        else
            {
            LOGE("uvc_get_device_list()=%d(%s)", ret, uvcErrorName(ret));
            }
        }

    if (result==NULL)
        {
        result = env->NewLongArray(0);
        }

    return result;
    }

