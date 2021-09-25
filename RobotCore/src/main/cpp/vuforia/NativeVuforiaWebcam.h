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

The present work is a conceptual derivative of a different work from PTC which was
copyrighted thusly:
===============================================================================
Copyright (c) 2018 PTC Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
===============================================================================*/
//
// NativeVuforiaWebcam.h
//

#ifndef __VUFORIA_WEBCAM_H__
#define __VUFORIA_WEBCAM_H__

#include <ftc.h>
#include <vector>
#include <ExternalProvider.h>
#include "JniEnv.h"

#undef TAG
#define TAG "UvcVuforiaWebcam"

enum UvcProcessingUnitBitShift
    {
    BIT_SHIFT_PU_BRIGHTNESS_CONTROL,
    BIT_SHIFT_PU_CONTRAST_CONTROL,
    BIT_SHIFT_PU_HUE_CONTROL,
    BIT_SHIFT_PU_SATURATION_CONTROL,
    BIT_SHIFT_PU_SHARPNESS_CONTROL,
    BIT_SHIFT_PU_GAMMA_CONTROL,
    BIT_SHIFT_PU_WHITE_BALANCE_TEMPERATURE_CONTROL,
    BIT_SHIFT_PU_WHITE_BALANCE_COMPONENT_CONTROL,
    BIT_SHIFT_PU_BACKLIGHT_COMPENSATION_CONTROL,
    BIT_SHIFT_PU_GAIN_CONTROL,
    BIT_SHIFT_PU_POWER_LINE_FREQUENCY_CONTROL,
    BIT_SHIFT_PU_HUE_AUTO_CONTROL,
    BIT_SHIFT_PU_WHITE_BALANCE_TEMPERATURE_AUTO_CONTROL,
    BIT_SHIFT_PU_WHITE_BALANCE_COMPONENT_AUTO_CONTROL,
    BIT_SHIFT_PU_DIGITAL_MULTIPLIER_CONTROL,
    BIT_SHIFT_PU_DIGITAL_MULTIPLIER_LIMIT_CONTROL,
    BIT_SHIFT_PU_ANALOG_VIDEO_STANDARD_CONTROL,
    BIT_SHIFT_PU_ANALOG_LOCK_STATUS_CONTROL,
    BIT_SHIFT_PU_CONTRAST_AUTO_CONTROL
    };

struct NativeVuforiaWebcam : ZeroOnNew, RefCounted, Vuforia::ExternalProvider::ExternalCamera
    {
    //----------------------------------------------------------------------------------------------
    // Types
    //----------------------------------------------------------------------------------------------
private:

    struct FormatMapping
        {
        uvc_frame_format uvcFrameFormat;
        Vuforia::ExternalProvider::FrameFormat vuforiaWebcamFormat;
        };

    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------
private:

    bool               _useExternalCamera = false;
    std::vector<FormatMapping> _formatMaps;

    jobject            _vuforiaWebcamNativeCallbacks = nullptr;

    jmethodID          _methodIdNativeCallbackOpen;
    jmethodID          _methodIdNativeCallbackClose;
    jmethodID          _methodIdNativeCallbackGetNumSupportedCameraModes;
    jmethodID          _methodIdNativeCallbackGetSupportedCameraMode;
    jmethodID          _methodIdNativeCallbackStart;
    jmethodID          _methodIdNativeCallbackStop;

    jmethodID          _methodIdNativeCallbackIsFocusModeSupported;
    jmethodID          _methodIdNativeCallbackGetFocusMode;
    jmethodID          _methodIdNativeCallbackSetFocusMode;
    jmethodID          _methodIdNativeCallbackGetMinFocusLength;
    jmethodID          _methodIdNativeCallbackGetMaxFocusLength;
    jmethodID          _methodIdNativeCallbackGetFocusLength;
    jmethodID          _methodIdNativeCallbackSetFocusLength;
    jmethodID          _methodIdNativeCallbackIsFocusLengthSupported;

    jmethodID          _methodIdNativeCallbackIsExposureModeSupported;
    jmethodID          _methodIdNativeCallbackGetExposureMode;
    jmethodID          _methodIdNativeCallbackSetExposureMode;
    jmethodID          _methodIdNativeCallbackGetMinExposure;
    jmethodID          _methodIdNativeCallbackGetMaxExposure;
    jmethodID          _methodIdNativeCallbackGetExposure;
    jmethodID          _methodIdNativeCallbackSetExposure;
    jmethodID          _methodIdNativeCallbackIsExposureSupported;


    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------
public:

    NativeVuforiaWebcam()
        {
        FTC_TRACE();
        }

    bool construct(/*nullable*/ jstring jstrLibraryName)
        {
        ScopedJniEnv env;

        bool success = true;
        jboolean isCopy;
        LPCSTR jstrChars = nullptr;
        LPCSTR libraryName = nullptr;

        // Get access to the library name, and canonicalize. null means no external library
        if (jstrLibraryName == nullptr)
            {
            libraryName = "";
            }
        else
            {
            jstrChars = env->GetStringUTFChars(jstrLibraryName, &isCopy);
            if (jstrChars != nullptr)
                {
                libraryName = jstrChars;
                }
            else
                {
                success = false;
                outOfMemory();
                }
            }

        if (success)
            {
            _useExternalCamera = strlen(libraryName) > 0;
            void* pvUser = _useExternalCamera ? reinterpret_cast<void*>(this) : nullptr;
            LOGD("setting external vuforia library: libraryName='%s' pvUser=0x%08zx", libraryName, (size_t)pvUser);
            if (Vuforia::setExternalProviderLibrary(libraryName, pvUser))
                {
                // all is well
                }
            else
                {
                success = false;
                LOGE("Vuforia::setExternalProviderLibrary failed");
                }
            }

        if (jstrChars != nullptr)
            {
            env->ReleaseStringUTFChars(jstrLibraryName, jstrChars);
            }

        return success;
        }

    bool postVuforiaInit(jobject vuforiaWebcamNativeCallbacks)
        {
        bool result = true;
        ScopedJniEnv env;

        if (vuforiaWebcamNativeCallbacks != nullptr)
            {
            _vuforiaWebcamNativeCallbacks = env->NewGlobalRef(vuforiaWebcamNativeCallbacks);
            if (_vuforiaWebcamNativeCallbacks != nullptr)
                {
                _methodIdNativeCallbackOpen                       = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackOpen",                        "("                          ")" JNI_BOOLEAN);
                _methodIdNativeCallbackClose                      = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackClose",                       "("                          ")" JNI_BOOLEAN);
                _methodIdNativeCallbackGetNumSupportedCameraModes = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackGetNumSupportedCameraModes",  "("                          ")" JNI_INT);
                _methodIdNativeCallbackGetSupportedCameraMode     = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackGetSupportedCameraMode",      "(" JNI_INT                  ")" JNI_INTARRAY);
                _methodIdNativeCallbackStart                      = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackStart",                       "(" JNI_INTARRAY JNI_POINTER ")" JNI_BOOLEAN);
                _methodIdNativeCallbackStop                       = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackStop",                        "("                          ")" JNI_BOOLEAN);

                _methodIdNativeCallbackIsFocusModeSupported       = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackIsFocusModeSupported",        "(" JNI_INT          ")" JNI_BOOLEAN);
                _methodIdNativeCallbackGetFocusMode               = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackGetFocusMode",                "("                  ")" JNI_INT);
                _methodIdNativeCallbackSetFocusMode               = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackSetFocusMode",                "(" JNI_INT          ")" JNI_BOOLEAN);
                _methodIdNativeCallbackGetMinFocusLength          = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackGetMinFocusLength",           "("                  ")" JNI_DOUBLE);
                _methodIdNativeCallbackGetMaxFocusLength          = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackGetMaxFocusLength",           "("                  ")" JNI_DOUBLE);
                _methodIdNativeCallbackGetFocusLength             = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackGetFocusLength",              "("                  ")" JNI_DOUBLE);
                _methodIdNativeCallbackSetFocusLength             = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackSetFocusLength",              "(" JNI_DOUBLE       ")" JNI_BOOLEAN);
                _methodIdNativeCallbackIsFocusLengthSupported     = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackIsFocusLengthSupported",      "("                  ")" JNI_BOOLEAN);

                _methodIdNativeCallbackIsExposureModeSupported    = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackIsExposureModeSupported",     "(" JNI_INT          ")" JNI_BOOLEAN);
                _methodIdNativeCallbackGetExposureMode            = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackGetExposureMode",             "("                  ")" JNI_INT);
                _methodIdNativeCallbackSetExposureMode            = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackSetExposureMode",             "(" JNI_INT          ")" JNI_BOOLEAN);
                _methodIdNativeCallbackGetMinExposure             = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackGetMinExposure",              "("                  ")" JNI_LONG);
                _methodIdNativeCallbackGetMaxExposure             = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackGetMaxExposure",              "("                  ")" JNI_LONG);
                _methodIdNativeCallbackGetExposure                = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackGetExposure",                 "("                  ")" JNI_LONG);
                _methodIdNativeCallbackSetExposure                = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackSetExposure",                 "(" JNI_LONG         ")" JNI_BOOLEAN);
                _methodIdNativeCallbackIsExposureSupported        = findMethod(env, _vuforiaWebcamNativeCallbacks, "nativeCallbackIsExposureSupported",         "("                  ")" JNI_BOOLEAN);
                }
            else
                {
                invalidArgs();
                result = false;
                }
            }
        else if (_useExternalCamera)
            {
            invalidArgs();
            result = false;
            }

        if (!result)
            {
            freeVuforiaWebcamNativeCallbacks();
            }
        return result;
        }

    void preVuforiaDeinit()
        {
        FTC_TRACE();
        // Nothing, really, to do
        }

protected:
    /** searches the superclass chain */
    jmethodID findMethod(ScopedJniEnv& env, jobject object, LPCSTR methodName, LPCSTR args)
        {
        return ::findMethod(env.getPointer(), object, methodName, args);
        }

    virtual ~NativeVuforiaWebcam() override
        {
        FTC_TRACE();
        free();
        }

    void free()
        {
        FTC_TRACE();
        stop();
        close();
        freeVuforiaWebcamNativeCallbacks();
        }

    void freeVuforiaWebcamNativeCallbacks()
        {
        FTC_TRACE();
        ScopedJniEnv env;
        if (_vuforiaWebcamNativeCallbacks != nullptr)
            {
            env->DeleteGlobalRef(_vuforiaWebcamNativeCallbacks);
            _vuforiaWebcamNativeCallbacks = nullptr;
            }
        }

    //----------------------------------------------------------------------------------------------
    // ExternalCamera: open & close
    //----------------------------------------------------------------------------------------------
public:

    bool open() override
        {
        FTC_TRACE();

        ScopedJniEnv env;
        jboolean success = env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackOpen);
        if (success)
            {
            }
        else
            {
            LOGE("open(): webcam failed to open");
            }
        return success;
        }

    bool close() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        jboolean jResult = JNI_TRUE;
        if (_methodIdNativeCallbackClose)
            {
            jResult = env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackClose);
            }
        return jResult != JNI_FALSE;
        }

    //----------------------------------------------------------------------------------------------
    // ExternalCamera: miscellaneous
    //----------------------------------------------------------------------------------------------
public:

    bool start(Vuforia::ExternalProvider::CameraMode cameraMode, Vuforia::ExternalProvider::CameraCallback* pvuforiaEngineFrameCallback) override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        jboolean jResult = JNI_FALSE;
        jintArray jintArrayCameraMode = env->NewIntArray(4);
        if (jintArrayCameraMode != nullptr)
            {
            jboolean isCopy;
            jint* rgiCameraMode = env->GetIntArrayElements(jintArrayCameraMode, &isCopy);
            if (rgiCameraMode)
                {
                getCameraModeData(rgiCameraMode, cameraMode);
                env->ReleaseIntArrayElements(jintArrayCameraMode, rgiCameraMode, 0);
                //
                jResult = env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackStart,
                    jintArrayCameraMode,
                    reinterpret_cast<JNI_NATIVE_POINTER>(pvuforiaEngineFrameCallback));
                }
            }
        else
            outOfMemory();
        return jResult != JNI_FALSE;
        }

    bool stop() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        jboolean jResult = JNI_TRUE;
        if (_methodIdNativeCallbackStop)
            {
            jResult = env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackStop);
            }
        return jResult != JNI_FALSE;
        }

    void deliverFrameToVuforia(Vuforia::ExternalProvider::CameraCallback* pVuforiaCallback, uvc_frame* pUvcFrame, const uint64_t& exposureTime, const Vuforia::ExternalProvider::CameraIntrinsics& cameraIntrinsics)
        {
        FTC_TRACE_VERBOSE();
        Vuforia::ExternalProvider::CameraFrame vuforiaCameraFrame;
        zero(&vuforiaCameraFrame);
        vuforiaCameraFrame.index        = static_cast<uint32_t>(pUvcFrame->frameNumber);
        vuforiaCameraFrame.width        = pUvcFrame->width;
        vuforiaCameraFrame.height       = pUvcFrame->height;
        vuforiaCameraFrame.format       = getVuforiaWebcamFromUvcFormat(pUvcFrame->frameFormat);
        vuforiaCameraFrame.stride       = pUvcFrame->cbLineStride;
        vuforiaCameraFrame.buffer       = reinterpret_cast<uint8_t*>(pUvcFrame->pbData);
        vuforiaCameraFrame.bufferSize   = pUvcFrame->cbData;
        vuforiaCameraFrame.timestamp    = static_cast<uint64_t>(pUvcFrame->captureTime);
        vuforiaCameraFrame.exposureTime = exposureTime;
        vuforiaCameraFrame.intrinsics   = cameraIntrinsics;

        pVuforiaCallback->onNewCameraFrame(&vuforiaCameraFrame);
        }

    void deliverFrameToVuforia(Vuforia::ExternalProvider::CameraCallback* pVuforiaCallback, Vuforia::ExternalProvider::CameraFrame* pVuforiaCameraFrame, const Vuforia::ExternalProvider::CameraIntrinsics& cameraIntrinsics)
        {
        FTC_TRACE_VERBOSE();
        pVuforiaCameraFrame->intrinsics = cameraIntrinsics;

        pVuforiaCallback->onNewCameraFrame(pVuforiaCameraFrame);
        }

    //----------------------------------------------------------------------------------------------
    // ExternalCamera: Metadata
    //----------------------------------------------------------------------------------------------
public:

    void noteUvcVuforiaExternalFormatMapping(uvc_frame_format uvcFrameFormat, Vuforia::ExternalProvider::FrameFormat vuforiaWebcamFormat)
        {
        for(auto it = _formatMaps.begin(); it != _formatMaps.end(); it++)
            {
            if (it->uvcFrameFormat == uvcFrameFormat && it->vuforiaWebcamFormat == vuforiaWebcamFormat)
                {
                return; // found a duplicate
                }
            }

        if (vuforiaWebcamFormat != Vuforia::ExternalProvider::FrameFormat::UNKNOWN)
            {
            FormatMapping mapping;
            mapping.uvcFrameFormat = uvcFrameFormat;
            mapping.vuforiaWebcamFormat = vuforiaWebcamFormat;
            LOGD("format map: uvc=%d vuforiaWebcam=%d", uvcFrameFormat, vuforiaWebcamFormat);
            _formatMaps.push_back(mapping);
            }
        }

    Vuforia::ExternalProvider::FrameFormat getVuforiaWebcamFromUvcFormat(uvc_frame_format uvcFrameFormat)
        {
        for(auto it = _formatMaps.begin(); it != _formatMaps.end(); it++)
            {
            if (it->uvcFrameFormat == uvcFrameFormat)
                {
                return it->vuforiaWebcamFormat;
                }
            }
        return Vuforia::ExternalProvider::FrameFormat::UNKNOWN;
        }

    static void getCameraModeData(jint* rgiDest, const Vuforia::ExternalProvider::CameraMode& cameraModeSrc)
        {
        rgiDest[0] = static_cast<jint>(cameraModeSrc.width);
        rgiDest[1] = static_cast<jint>(cameraModeSrc.height);
        rgiDest[2] = static_cast<jint>(cameraModeSrc.fps);
        rgiDest[3] = static_cast<jint>(cameraModeSrc.format);
        }

    static void setCameraModeData(Vuforia::ExternalProvider::CameraMode* pCameraModeDest, jint* rgiSrc)
        {
        pCameraModeDest->width  = static_cast<uint32_t>(rgiSrc[0]);
        pCameraModeDest->height = static_cast<uint32_t>(rgiSrc[1]);
        pCameraModeDest->fps    = static_cast<uint32_t>(rgiSrc[2]);
        pCameraModeDest->format = static_cast<Vuforia::ExternalProvider::FrameFormat>(rgiSrc[3]);
        }

    static void setCameraIntrinsicsData(Vuforia::ExternalProvider::CameraIntrinsics* pIntrinsicsDest, float* pFloatSrc)
        {
        pIntrinsicsDest->focalLengthX = *pFloatSrc++;
        pIntrinsicsDest->focalLengthY = *pFloatSrc++;
        pIntrinsicsDest->principalPointX = *pFloatSrc++;
        pIntrinsicsDest->principalPointY = *pFloatSrc++;
        memcpy(&pIntrinsicsDest->distortionCoefficients[0], pFloatSrc, sizeof(pIntrinsicsDest->distortionCoefficients));
        }

    uint32_t getNumSupportedCameraModes() override
        {
        ScopedJniEnv env;
        jint iResult = env->CallIntMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackGetNumSupportedCameraModes);
        return static_cast<uint32_t>(iResult);
        }

    bool getSupportedCameraMode(uint32_t index, Vuforia::ExternalProvider::CameraMode* pCameraMode) override
        {
        bool success = false;
        ScopedJniEnv env;
        jintArray fourIntArray = reinterpret_cast<jintArray>(env->CallObjectMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackGetSupportedCameraMode, static_cast<jint>(index)));
        if (fourIntArray != nullptr)
            {
            if (env->GetArrayLength(fourIntArray) == 4)
                {
                jboolean isCopy;
                jint* rgi = env->GetIntArrayElements(fourIntArray, &isCopy);
                if (rgi != nullptr)
                    {
                    setCameraModeData(pCameraMode, rgi);
                    success = true;
                    // LOGD("getSupportedCameraMode: index=%d format=%d %dx%d fps=%d", index, pCameraMode->format, pCameraMode->width, pCameraMode->height, pCameraMode->fps);
                    //
                    env->ReleaseIntArrayElements(fourIntArray, rgi, JNI_ABORT);
                    }
                else
                    LOGE("env->GetIntArrayElements(fourIntArray, &isCopy) returned null");
                }
            else
                LOGE("nativeCallbackGetSupportedCameraMode returned incorrect array size");
            }
        else
            LOGE("null returned by java in nativeCallbackGetSupportedCameraMode");
        //
        return success;
        }

    //----------------------------------------------------------------------------------------------
    // ExternalCamera: Exposure mode
    //----------------------------------------------------------------------------------------------

    bool supportsExposureMode(Vuforia::ExternalProvider::ExposureMode vuforiaExposureMode) override
        {
        FTC_TRACE_VERBOSE();
        ScopedJniEnv env;
        return env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackIsExposureModeSupported, vuforiaExposureMode);
        }

    Vuforia::ExternalProvider::ExposureMode getExposureMode() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return (Vuforia::ExternalProvider::ExposureMode)env->CallIntMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackGetExposureMode);
        }

    bool setExposureMode(Vuforia::ExternalProvider::ExposureMode vuforiaExposureMode) override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackSetExposureMode, vuforiaExposureMode);
        }

    //----------------------------------------------------------------------------------------------
    // ExternalCamera: Exposure value
    //----------------------------------------------------------------------------------------------

    bool supportsExposureValue() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackIsExposureSupported);
        }

    uint64_t getExposureValueMin() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return (uint64_t)env->CallLongMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackGetMinExposure);
        }

    uint64_t getExposureValueMax() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return (uint64_t)env->CallLongMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackGetMaxExposure);
        }

    uint64_t getExposureValue() override
        {
        FTC_TRACE_VERBOSE();
        ScopedJniEnv env;
        return (uint64_t)env->CallLongMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackGetExposure);
        }

    bool setExposureValue(uint64_t exposureTime) override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackSetExposure, exposureTime);
        }

    //----------------------------------------------------------------------------------------------
    // ExternalCamera: Focus mode
    //----------------------------------------------------------------------------------------------

    bool supportsFocusMode(Vuforia::ExternalProvider::FocusMode vuforiaFocusMode) override
        {
        FTC_TRACE_VERBOSE("mode=%d", vuforiaFocusMode);
        ScopedJniEnv env;
        return env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackIsFocusModeSupported, vuforiaFocusMode);
        }

    Vuforia::ExternalProvider::FocusMode getFocusMode() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return (Vuforia::ExternalProvider::FocusMode)env->CallIntMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackGetFocusMode);
        }

    bool setFocusMode(Vuforia::ExternalProvider::FocusMode vuforiaFocusMode) override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackSetFocusMode, vuforiaFocusMode);
        }

    //----------------------------------------------------------------------------------------------
    // ExternalCamera: Focus value
    //----------------------------------------------------------------------------------------------

    bool supportsFocusValue() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackIsFocusLengthSupported);
        }

    float getFocusValueMin() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return (float)env->CallDoubleMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackGetMinFocusLength);
        }

    float getFocusValueMax() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return (float)env->CallDoubleMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackGetMaxFocusLength);
        }

    float getFocusValue() override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return (float)env->CallDoubleMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackGetFocusLength);
        }

    bool setFocusValue(float value) override
        {
        FTC_TRACE();
        ScopedJniEnv env;
        return env->CallBooleanMethod(_vuforiaWebcamNativeCallbacks, _methodIdNativeCallbackSetFocusLength, (double)value);
        }
    };

#endif