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
// UvcDevice methods
//
#include <jni.h>
#include <unistd.h>
#include <libuvc.h>
#include <libuvc/libuvc_internal.h>
#include <asm/errno.h>
#include "ftc.h"
#include "JniEnv.h"

#undef TAG
static LPCSTR TAG = "UvcDevice";

JNIEXPORT jboolean JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcDevice_nativeSetUsbDeviceInfo(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jint fd, jstring strUsbPath)
    {
    FTC_TRACE();
    jboolean result = JNI_FALSE;
    uvc_device_t* pDevice = (uvc_device_t*) pointer;
    if (pDevice && isValidFd(fd) && strUsbPath && env->GetStringLength(strUsbPath) > 0)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        jboolean isCopy;
        LPCSTR szUsbPath = env->GetStringUTFChars(strUsbPath, &isCopy);
        if (szUsbPath)
            {
            // Dup the handle and copy the path
            pDevice->fdJava = dup(fd);
            pDevice->szUsbPath = strdup(szUsbPath);
            if (pDevice->szUsbPath && isValidFd(pDevice->fdJava))
                {
                result = JNI_TRUE;
                }
            else
                {
                free(const_cast<LPSTR>(pDevice->szUsbPath));    // freeing null is ok
                if (isValidFd(pDevice->fdJava)) close(pDevice->fdJava);
                pDevice->szUsbPath = nullptr;
                pDevice->fdJava = FD_NONE;
                LOGE("nativeSetUsbDeviceInfo() failed");
                }

            env->ReleaseStringUTFChars(strUsbPath, szUsbPath);
            }
        else
            outOfMemory();
        }
    else
        invalidArgs();

    return result;
    }

JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcDevice_nativeGetContext(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE();
    uvc_device_t* pDevice = (uvc_device_t*) pointer;
    if (pDevice)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        return (JNI_NATIVE_POINTER)pDevice->ctx;
        }
    else
        {
        invalidArgs();
        return JNI_NATIVE_POINTER_NULL;
        }
    }

/** returns a new ref on the libusb_device pointer */
JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcDevice_nativeGetLibUsbDevice(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE();
    uvc_device_t* pDevice = (uvc_device_t*) pointer;
    if (pDevice)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        if (pDevice->usb_dev) libusb_ref_device2(pDevice->usb_dev, "nativeGetUsbDevice");
        return (JNI_NATIVE_POINTER)pDevice->usb_dev;
        }
    else
        {
        invalidArgs();
        return JNI_NATIVE_POINTER_NULL;
        }
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcDevice_nativeReleaseRefDevice(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE();
    uvc_device* pDevice = (uvc_device*) pointer;
    if (pDevice)
        {
        NATIVE_API_ONE_CALLER();
        uvc_unref_device(pDevice);
        }
    else
        invalidArgs();
    }

struct UsbInterfaceManagerImpl : ZeroOnNew, UsbInterfaceManager
    {
    jobject   _usbInterfaceManager;
    jmethodID _methodIDClaimInterface;
    jmethodID _methodIDReleaseInterface;
    jmethodID _methodIDSetInterfaceAltSetting;

    virtual ~UsbInterfaceManagerImpl() override
        {
        freeCallbacks();
        }

    uvc_error claimInterface(int idx) override
        {
        ScopedJniEnv env;
        return (uvc_error)env->CallIntMethod(_usbInterfaceManager, _methodIDClaimInterface, idx);
        }

    uvc_error releaseInterface(int idx) override
        {
        ScopedJniEnv env;
        return (uvc_error)env->CallIntMethod(_usbInterfaceManager, _methodIDReleaseInterface, idx);
        }

    uvc_error setInterfaceAltSetting(int bInterfaceNumber, int bAlternateSetting) override
        {
        ScopedJniEnv env;
        return (uvc_error)env->CallIntMethod(_usbInterfaceManager, _methodIDSetInterfaceAltSetting, bInterfaceNumber, bAlternateSetting);
        }

    bool findCallbacks(jobject usbInterfaceManager)
        {
        bool result = true;
        ScopedJniEnv env;
        _usbInterfaceManager = env->NewGlobalRef(usbInterfaceManager);
        if (_usbInterfaceManager != nullptr)
            {
            _methodIDClaimInterface = findMethod(env, _usbInterfaceManager, "claimInterface",   "(" JNI_INT ")" JNI_INT);
            _methodIDReleaseInterface = findMethod(env, _usbInterfaceManager, "releaseInterface", "(" JNI_INT ")" JNI_INT);
            _methodIDSetInterfaceAltSetting = findMethod(env, _usbInterfaceManager, "setInterfaceAltSetting", "(" JNI_INT JNI_INT ")" JNI_INT);
            }
        else
            {
            invalidArgs();
            result = false;
            }

        if (!result)
            {
            freeCallbacks();
            }

        return result;
        }

    void freeCallbacks()
        {
        ScopedJniEnv env;
        if (_usbInterfaceManager != nullptr)
            {
            env->DeleteGlobalRef(_usbInterfaceManager);
            _usbInterfaceManager = nullptr;
            }
        }

    jmethodID findMethod(ScopedJniEnv& env, jobject object, LPCSTR methodName, LPCSTR args)
        {
        return ::findMethod(env.getPointer(), object, methodName, args);
        }
    };

UsbInterfaceManager* createInterfaceManager(jobject usbInterfaceManager)
    {
    UsbInterfaceManagerImpl* result = new UsbInterfaceManagerImpl();
    if (result != nullptr)
        {
        if (result->findCallbacks(usbInterfaceManager))
            {
            // all is well
            }
        else
            {
            ::releaseRef(result);
            result = nullptr;
            }
        }
    else
        {
        outOfMemory();
        }
    return result;
    }

JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcDevice_nativeOpenDeviceHandle(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, /*optional*/jobject usbInterfaceManager)
    {
    FTC_TRACE();
    uvc_device* pDevice = (uvc_device*) pointer;
	JNI_NATIVE_POINTER result = JNI_NATIVE_POINTER_NULL;
	if (pDevice)
        {
        UsbInterfaceManager* pInterfaceManger = usbInterfaceManager ? createInterfaceManager(usbInterfaceManager) : nullptr;
        if (usbInterfaceManager==nullptr || pInterfaceManger != nullptr)
            {
            NATIVE_API_ONE_CALLER();
            uvc_device_handle* pDeviceHandle = NULL;
            uvc_error rc = uvc_open(pDevice, pInterfaceManger, &pDeviceHandle);
            if (rc == UVC_SUCCESS)
                {
                // all is well
                }
            else
                {
                LOGE("uvc_open(): rc=%d", rc);
                }
            result = (JNI_NATIVE_POINTER)pDeviceHandle;
            ::releaseRef(pInterfaceManger);
            }
        else
            invalidArgs();
        }
    else
        invalidArgs();
    return result;
    }


JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcDevice_nativeGetDeviceDescriptor(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE();
    JNI_NATIVE_POINTER result = JNI_NATIVE_POINTER_NULL;
    uvc_device* pDevice = (uvc_device*) pointer;
    if (pDevice)
        {
        NATIVE_API_ONE_CALLER();
        uvc_device_descriptor_t* pDeviceDescriptor;
        uvc_error_t rc = uvc_get_device_descriptor(pDevice, &pDeviceDescriptor);
        if (!rc)
            {
            // Result is malloc'd
            result = (JNI_NATIVE_POINTER)pDeviceDescriptor;
            }
        }
    else
        invalidArgs();
    return result;
    }

JNIEXPORT jlong JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcDevice_nativeGetDeviceInfo(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE();
    JNI_NATIVE_POINTER result = JNI_NATIVE_POINTER_NULL;
    uvc_device* pDevice = (uvc_device*) pointer;
    if (pDevice)
        {
        NATIVE_API_ONE_CALLER();
        uvc_device_info_t* pDeviceInfo = NULL;
        uvc_error_t rc = uvc_get_device_info(pDevice, &pDeviceInfo);
        if (!rc)
            {
            result = (JNI_NATIVE_POINTER)pDeviceInfo;
            }
        else
            {
            LOGE("uvc_get_device_info() failed rc=%d", rc);
            }
        }
    else
        invalidArgs();
    return result;
    }

JNIEXPORT jboolean JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcDevice_nativeIsUvcCompatible(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE();
    bool result = false;
    uvc_device* pDevice = (uvc_device*) pointer;
    if (pDevice)
        {
        NATIVE_API_ONE_CALLER();

        bool isCompatible = false;
        if (!uvc_is_usb_device_compatible(pDevice->getContext(), pDevice->usb_dev, &isCompatible))
            {
            result = isCompatible;
            }
        }
    else
        invalidArgs();

    return jboolean(result ? JNI_TRUE : JNI_FALSE);
    }

