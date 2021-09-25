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
// UvcStreamControl methods
//
#include <jni.h>
#include <unistd.h>
#include <libuvc.h>
#include <errno.h>
#include <libuvc/libuvc_internal.h>
#include "ftc.h"

#undef TAG
static LPCSTR TAG = "UvcStreamControl";

JNIEXPORT jintArray JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcStreamCtrl_nativeGetFieldOffsets(JNIEnv *env, jclass type, int cFieldExpected)
    {
    OFFSET_MAP_START(17, uvc_stream_ctrl_t)

    #define addField(field)  (cFieldAdded++, (*pT++) = offsetof(uvc_stream_ctrl_t, field))
    addField(bmHint);
    addField(bFormatIndex);
    addField(bFrameIndex);
    addField(dwFrameInterval);
    addField(wKeyFrameRate);
    addField(wPFrameRate);
    addField(wCompQuality);
    addField(wCompWindowSize);
    addField(wDelay);
    addField(dwMaxVideoFrameSize);
    addField(dwMaxPayloadTransferSize);
    addField(dwClockFrequency);
    addField(bmFramingInfo);
    addField(bPreferredVersion);
    addField(bMinVersion);
    addField(bMaxVersion);
    addField(bInterfaceNumber);
    #undef addField

    OFFSET_MAP_END()
    }

JNIEXPORT jstring JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcStreamCtrl_nativePrint(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, JNI_NATIVE_POINTER pointerUvcContext)
    {
    uvc_stream_ctrl_t* pStreamControl = (uvc_stream_ctrl_t*) pointer;
    uvc_context* pContext = (uvc_context*)pointerUvcContext;
    jstring result = NULL;
    if (pStreamControl && pContext)
        {
        TempFile tempFile = TempFile(pContext->szTempFolder);
        if (tempFile.create())
            {
            NATIVE_API_ONE_CALLER();
            uvc_print_stream_ctrl(pStreamControl, tempFile.pFile);
            result = tempFile.getJavaString(env);
            tempFile.close();
            }
        }
    else
        invalidArgs();

    if (NULL==result) LOGE("nativePrint failed");
    return result;
    }

JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcStreamCtrl_nativeOpen(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointerDeviceHandle, JNI_NATIVE_POINTER pointerStreamControl)
    {
    FTC_TRACE();
    uvc_device_handle* pDeviceHandle = (uvc_device_handle*) pointerDeviceHandle;
    uvc_stream_ctrl* pStreamControl = (uvc_stream_ctrl*) pointerStreamControl;
    JNI_NATIVE_POINTER result = JNI_NATIVE_POINTER_NULL;
    //
    if (pDeviceHandle && pStreamControl)
        {
        NATIVE_API_ONE_CALLER();
        // uvc_stream_open_ctrl ALLOCATES and returns a new handle
        uvc_stream_handle_t* pStreamHandle = NULL;
        int rc = uvc_stream_open_ctrl(pDeviceHandle, &pStreamHandle, pStreamControl);
        if (!rc && pStreamHandle != NULL)
            {
            result = (JNI_NATIVE_POINTER) pStreamHandle;
            }
        else
            LOGE("rc=%d", rc);
        }
    else
        invalidArgs();
    //
    if (JNI_NATIVE_POINTER_NULL==result) LOGE("nativeOpen failed");
    return result;
    }
