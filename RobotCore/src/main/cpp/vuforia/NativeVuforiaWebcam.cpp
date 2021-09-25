/*
Copyright (c) 2018 Robert Atkinson

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
// NativeVuforiaWebcam.cpp
//
#include <string>
#include <ftc.h>
#include <Vuforia.h>
#include <ExternalProvider.h>
#include <libuvc.h>
#include "NativeVuforiaWebcam.h"
#include "VuforiaExternalProviderDelegator.h"

#undef TAG
static LPCSTR TAG = "UvcVuforiaWebcam";

//--------------------------------------------------------------------------------------------------
// Vuforia external provider API
//--------------------------------------------------------------------------------------------------
extern "C"
{

/**
 * Dyn-called by Vuforia
 */
uint32_t vuforiaext_getAPIVersion()
    {
    FTC_TRACE();
    return Vuforia::ExternalProvider::EXTERNAL_PROVIDER_API_VERSION;
    }

/**
 * Dyn-called by Vuforia
 */
uint32_t vuforiaext_getLibraryVersion(char* rgchOut, const uint32_t cchMax)
    {
    FTC_TRACE();
    std::string versionString = "FTC-Vuforia-USB-Camera-v1";
    uint32_t cchCopy = versionString.size() > cchMax ? cchMax : versionString.size();
    memcpy(rgchOut, versionString.c_str(), cchCopy);
    return cchCopy;
    }

/*
 * From the Vuforia External Camera Reference Guide:
 *
 * To start the flow of camera frames Vuforia will request a new camera instance that implements the
 * ExternalCamera interface by calling:
 *   1. vuforiaext_createExternalCamera(): The call must construct a new object that implements the
 *      ExternalCamera-interface. The memory and the lifetime of this object is owned by the plugin.
 *      Vuforia expects the object to be valid between the createExternalCamera and
 *      destroyExternalCamera calls.
 *   2. ExternalCamera::open(): Called on the returned instance. After calling open() the supported
 *      camera modes must be available.
 *   3. Vuforia discovers the supported camera modes
 *        o ExternalCamera::getNumSupportedCameraModes(): Returns the number of supported
 *          camera modes.
 *        o Vuforia then iterates over the list of supported camera modes with
 *          ExternalCamera::getSupportedCameraMode(). The iteration goes from 0 to
 *          (getNumSupportedCameraModes() - 1).
 *   4. ExternalCamera::start(cameraMode, callback): Starts the flow of frames into the provided
 *      callback.
 *
 * To deinitalize the plugin Vuforia closes the camera by calling:
 *   *  ExternalCamera::stop(): Stops the flow of camera frames.
 *   *  ExternalCamera::close(): Closes the camera.
 *   *  vuforiaext_destroyExternalCamera(): The plugin should then destroy the object. Vuforia will not
 *      use the object after this
 */

// Hack: vuforiaext_destroyExternalCamera sometimes gives us null instead of a/the previously
// created delegator that we gave it in vuforiaext_createExternalCamera. So we internally keep
// track of that, and try to correct for the bug.
VuforiaExternalProviderDelegator* g_lastDelegatorReturned = nullptr;

/**
 * Dyn-called by Vuforia
 */
Vuforia::ExternalProvider::ExternalCamera* vuforiaext_createExternalCamera(void* pvUser)
    {
    FTC_TRACE();
    NATIVE_API_ONE_CALLER();
    NativeVuforiaWebcam* pVuforiaWebcam = reinterpret_cast<NativeVuforiaWebcam*>(pvUser);
    if (pVuforiaWebcam != nullptr)
        {
        VuforiaExternalProviderDelegator* pDelegator = new VuforiaExternalProviderDelegator(pVuforiaWebcam); // xyzzya
        if (pDelegator)
            {
            if (g_lastDelegatorReturned == nullptr)
                {
                g_lastDelegatorReturned = pDelegator;
                }
            }
        else
            outOfMemory();

        return static_cast<Vuforia::ExternalProvider::ExternalCamera*>(pDelegator);
        }
    else
        {
        invalidArgs();
        return nullptr;
        }
    }

/**
 * Dyn-called by Vuforia
 */
void vuforiaext_destroyExternalCamera(Vuforia::ExternalProvider::ExternalCamera* pExternalCamera)
    {
    FTC_TRACE();
    NATIVE_API_ONE_CALLER();
    VuforiaExternalProviderDelegator* pDelegator = static_cast<VuforiaExternalProviderDelegator*>(pExternalCamera);
    if (pDelegator == nullptr)
        {
        LOGD("vuforiaext_destroyExternalCamera() provided null pointer: working around that bug");
        pDelegator = g_lastDelegatorReturned;
        }

    if (pDelegator != nullptr)
        {
        int remaining = ::releaseRef(pDelegator); // xyzzya
        LOGD("delegator cref remaining=%d", remaining);
        }

    g_lastDelegatorReturned = nullptr;
    }
}

//--------------------------------------------------------------------------------------------------
// Vuforia life cycle
//--------------------------------------------------------------------------------------------------

/**
 * Called before Vuforia is init()'d
 *
 * On success, return the pointer to the NativeVuforiaWebcam that we passed in to Vuforia::setExternalProviderLibrary.
 * On failure, return null.
 */
JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_vuforia_externalprovider_VuforiaWebcam_nativePreVuforiaInit(
        JNIEnv *env, jobject type,
        jstring jstrLibraryName    // null or empty if we're not to use an external library
        )
    {
    FTC_TRACE();
    bool success = true;
    NativeVuforiaWebcam* pVuforiaWebcam = new NativeVuforiaWebcam(); // DAKE#EKXK
    if (pVuforiaWebcam)
        {
        success = pVuforiaWebcam->construct(jstrLibraryName);
        }
    else
        {
        success = false;
        outOfMemory();
        }

    if (!success)
        {
        ::releaseRef(pVuforiaWebcam);
        }

    return reinterpret_cast<JNI_NATIVE_POINTER>(pVuforiaWebcam);
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_vuforia_externalprovider_VuforiaWebcam_nativeReleaseVuforiaWebcam(
        JNIEnv *env, jobject type,
        JNI_NATIVE_POINTER pointer
        )
    {
    FTC_TRACE();
    NativeVuforiaWebcam* pVuforiaWebcam = reinterpret_cast<NativeVuforiaWebcam*>(pointer);
    int remaining = ::releaseRef(pVuforiaWebcam); // DAKE#EKXK
    LOGD("NativeVuforiaWebcam cref remaining=%d", remaining);
    }

JNIEXPORT jboolean JNICALL
Java_org_firstinspires_ftc_robotcore_internal_vuforia_externalprovider_VuforiaWebcam_nativePostVuforiaInit(
        JNIEnv *env, jobject type,
        JNI_NATIVE_POINTER pointer,
        jobject vuforiaWebcam // a VuforiaWebcamNativeCallback
        )
    {
    FTC_TRACE();
    uvc_error rc = UVC_SUCCESS;
    NativeVuforiaWebcam* pVuforiaWebcam = reinterpret_cast<NativeVuforiaWebcam*>(pointer);
    if (pVuforiaWebcam != nullptr)
        {
        if (pVuforiaWebcam->postVuforiaInit(vuforiaWebcam))
            {
            // all is well
            }
        else
            rc = UVC_ERROR_OTHER;
        }
    else
        {
        rc = uvcInvalidArgs();
        }
    return tojboolean(rc);
    }

JNIEXPORT jboolean JNICALL
Java_org_firstinspires_ftc_robotcore_internal_vuforia_externalprovider_VuforiaWebcam_nativePreVuforiaDeinit(
        JNIEnv *env, jobject type,
        JNI_NATIVE_POINTER pointer
        )
    {
    FTC_TRACE();
    uvc_error rc = UVC_SUCCESS;
    NativeVuforiaWebcam* pVuforiaWebcam = reinterpret_cast<NativeVuforiaWebcam*>(pointer);
    if (pVuforiaWebcam != nullptr)
        {
        pVuforiaWebcam->preVuforiaDeinit();
        }
    else
        {
        rc = uvcInvalidArgs();
        }
    return tojboolean(rc);
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_vuforia_externalprovider_VuforiaWebcam_nativePostVuforiaDeinit(JNIEnv *env, jobject type)
    {
    FTC_TRACE();
    Vuforia::setExternalProviderLibrary("", nullptr);
    }

//--------------------------------------------------------------------------------------------------
// Other
//--------------------------------------------------------------------------------------------------

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_vuforia_externalprovider_VuforiaWebcam_nativeNoteAndroidVuforiaExternalFormatMapping(JNIEnv *env, jobject type,
         JNI_NATIVE_POINTER pointer,
         int uvcFrameFormat,
         int vuforiaExternalProviderFormat)
    {
    FTC_TRACE_VERBOSE();
    if (pointer)
        {
        NativeVuforiaWebcam* pVuforiaWebcam = reinterpret_cast<NativeVuforiaWebcam*>(pointer);
        pVuforiaWebcam->noteUvcVuforiaExternalFormatMapping(static_cast<uvc_frame_format>(uvcFrameFormat), static_cast<Vuforia::ExternalProvider::FrameFormat>(vuforiaExternalProviderFormat));
        }
    else
        invalidArgs();
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_vuforia_externalprovider_VuforiaWebcam_nativeDeliverFrameToVuforiaUvc(JNIEnv *env, jobject type,
        JNI_NATIVE_POINTER pointer,
        JNI_NATIVE_POINTER pointerCallback,
        JNI_NATIVE_POINTER pointerUvcFrame,
        jlong exposureTime,
        jfloatArray jfloatArrayCameraIntrinsics
        )
    {
    FTC_TRACE_VERBOSE();
    if (pointer && pointerCallback && pointerUvcFrame && jfloatArrayCameraIntrinsics)
        {
        int cFloat = env->GetArrayLength(jfloatArrayCameraIntrinsics);
        if (cFloat >= sizeof(Vuforia::ExternalProvider::CameraIntrinsics) / sizeof(float))
            {
            NativeVuforiaWebcam* pVuforiaWebcam = reinterpret_cast<NativeVuforiaWebcam*>(pointer);
            Vuforia::ExternalProvider::CameraCallback* pVuforiaCallback = reinterpret_cast<Vuforia::ExternalProvider::CameraCallback*>(pointerCallback);
            uvc_frame* pUvcFrame = reinterpret_cast<uvc_frame*>(pointerUvcFrame);

            Vuforia::ExternalProvider::CameraIntrinsics cameraIntrinsics;
            jboolean isCopy;
            float* rgFloat = env->GetFloatArrayElements(jfloatArrayCameraIntrinsics, &isCopy);
            if (rgFloat)
                {
                pVuforiaWebcam->setCameraIntrinsicsData(&cameraIntrinsics, rgFloat);
                env->ReleaseFloatArrayElements(jfloatArrayCameraIntrinsics, rgFloat, JNI_ABORT);

                pVuforiaWebcam->deliverFrameToVuforia(pVuforiaCallback, pUvcFrame, exposureTime, cameraIntrinsics);
                }
            else
                outOfMemory();
            }
        else
            invalidArgs();
        }
    else
        invalidArgs();
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_vuforia_externalprovider_VuforiaWebcam_nativeDeliverFrameToVuforiaVuforia(JNIEnv *env, jobject type,
        JNI_NATIVE_POINTER pointer,
        JNI_NATIVE_POINTER pointerCallback,
        JNI_NATIVE_POINTER pointerVuforiaFrame,
        jfloatArray jfloatArrayCameraIntrinsics
        )
    {
    FTC_TRACE_VERBOSE();
    if (pointer && pointerCallback && pointerVuforiaFrame && jfloatArrayCameraIntrinsics)
        {
        int cFloat = env->GetArrayLength(jfloatArrayCameraIntrinsics);
        if (cFloat >= sizeof(Vuforia::ExternalProvider::CameraIntrinsics) / sizeof(float))
            {
            NativeVuforiaWebcam* pVuforiaWebcam = reinterpret_cast<NativeVuforiaWebcam*>(pointer);
            Vuforia::ExternalProvider::CameraCallback* pVuforiaCallback = reinterpret_cast<Vuforia::ExternalProvider::CameraCallback*>(pointerCallback);
            Vuforia::ExternalProvider::CameraFrame* pVuforiaCameraFrame = reinterpret_cast<Vuforia::ExternalProvider::CameraFrame*>(pointerVuforiaFrame);

            Vuforia::ExternalProvider::CameraIntrinsics cameraIntrinsics;
            jboolean isCopy;
            float* rgFloat = env->GetFloatArrayElements(jfloatArrayCameraIntrinsics, &isCopy);
            if (rgFloat)
                {
                pVuforiaWebcam->setCameraIntrinsicsData(&cameraIntrinsics, rgFloat);
                env->ReleaseFloatArrayElements(jfloatArrayCameraIntrinsics, rgFloat, JNI_ABORT);

                pVuforiaWebcam->deliverFrameToVuforia(pVuforiaCallback, pVuforiaCameraFrame, cameraIntrinsics);
                }
            else
                outOfMemory();
            }
        else
            invalidArgs();
        }
    else
        invalidArgs();
    }