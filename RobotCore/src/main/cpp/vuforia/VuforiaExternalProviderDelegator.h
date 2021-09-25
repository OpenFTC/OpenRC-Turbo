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
// VuforiaExternalProviderDelegator.h
//

#ifndef __VUFORIA_EXTERNAL_PROVIDER_DELEGATOR_H__
#define __VUFORIA_EXTERNAL_PROVIDER_DELEGATOR_H__

#include <ftc.h>
#include <ExternalProvider.h>
#include "NativeVuforiaWebcam.h"

/**
 * We use a delegator to actually pass to Vuforia so that the lifetime management on that
 * is independent of the lifetime management on our actual NativeVuforiaWebcam. They have an
 * explicit destroy call, whereas we interally use reference counting; the delegate helps
 * merge the two.
 */
struct VuforiaExternalProviderDelegator : ZeroOnNew, RefCounted, Vuforia::ExternalProvider::ExternalCamera
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------
private:
    NativeVuforiaWebcam* _pNativeVuforiaWebcam;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------
public:
    VuforiaExternalProviderDelegator(NativeVuforiaWebcam* pVuforiaWebcam)
        {
        FTC_TRACE();
        _pNativeVuforiaWebcam = pVuforiaWebcam;
        _pNativeVuforiaWebcam->addRef();  // KDFLKU#KK
        }

private:
    ~VuforiaExternalProviderDelegator() override
        {
        FTC_TRACE();
        int cref = ::releaseRef(_pNativeVuforiaWebcam); // KDFLKU#KK
        LOGD("NativeVuforiaWebcam cref remaining=%d", cref);
        }

    //----------------------------------------------------------------------------------------------
    // ExternalCamera
    //----------------------------------------------------------------------------------------------
public:
    bool open() override
        {
        return _pNativeVuforiaWebcam->open();
        }

    bool close() override
        {
        return _pNativeVuforiaWebcam->close();
        }

    bool
    start(Vuforia::ExternalProvider::CameraMode cameraMode, Vuforia::ExternalProvider::CameraCallback *pcb) override
        {
        return _pNativeVuforiaWebcam->start(cameraMode, pcb);
        }

    bool stop() override
        {
        return _pNativeVuforiaWebcam->stop();
        }

    uint32_t getNumSupportedCameraModes() override
        {
        return _pNativeVuforiaWebcam->getNumSupportedCameraModes();
        }

    bool getSupportedCameraMode(uint32_t index, Vuforia::ExternalProvider::CameraMode *out) override
        {
        return _pNativeVuforiaWebcam->getSupportedCameraMode(index, out);
        }

    bool supportsExposureMode(Vuforia::ExternalProvider::ExposureMode parameter) override
        {
        return _pNativeVuforiaWebcam->supportsExposureMode(parameter);
        }

    Vuforia::ExternalProvider::ExposureMode getExposureMode() override
        {
        return _pNativeVuforiaWebcam->getExposureMode();
        }

    bool setExposureMode(Vuforia::ExternalProvider::ExposureMode mode) override
        {
        return _pNativeVuforiaWebcam->setExposureMode(mode);
        }

    bool supportsExposureValue() override
        {
        return _pNativeVuforiaWebcam->supportsExposureValue();
        }

    uint64_t getExposureValueMin() override
        {
        return _pNativeVuforiaWebcam->getExposureValueMin();
        }

    uint64_t getExposureValueMax() override
        {
        return _pNativeVuforiaWebcam->getExposureValueMax();
        }

    uint64_t getExposureValue() override
        {
        return _pNativeVuforiaWebcam->getExposureValue();
        }

    bool setExposureValue(uint64_t exposureTime) override
        {
        return _pNativeVuforiaWebcam->setExposureValue(exposureTime);
        }

    bool supportsFocusMode(Vuforia::ExternalProvider::FocusMode parameter) override
        {
        return _pNativeVuforiaWebcam->supportsFocusMode(parameter);
        }

    Vuforia::ExternalProvider::FocusMode getFocusMode() override
        {
        return _pNativeVuforiaWebcam->getFocusMode();
        }

    bool setFocusMode(Vuforia::ExternalProvider::FocusMode mode) override
        {
        return _pNativeVuforiaWebcam->setFocusMode(mode);
        }

    bool supportsFocusValue() override
        {
        return _pNativeVuforiaWebcam->supportsFocusValue();
        }

    float getFocusValueMin() override
        {
        return _pNativeVuforiaWebcam->getFocusValueMin();
        }

    float getFocusValueMax() override
        {
        return _pNativeVuforiaWebcam->getFocusValueMax();
        }

    float getFocusValue() override
        {
        return _pNativeVuforiaWebcam->getFocusValue();
        }

    bool setFocusValue(float value) override
        {
        return _pNativeVuforiaWebcam->setFocusValue(value);
        }

    };

#endif // __VUFORIA_EXTERNAL_PROVIDER_DELEGATOR_H__
