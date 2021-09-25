/*===============================================================================
Copyright (c) 2018 PTC Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.

@file
    ExternalProvider.h

@brief
    Header file for the ExternalProvider class.
===============================================================================*/

#ifndef VUFORIA_EXTERNAL_PROVIDER_H_
#define VUFORIA_EXTERNAL_PROVIDER_H_

#include <stdint.h>

#if defined(_MSC_VER)
#define PACKED_STRUCT(...) __pragma(pack(push, 1)) struct __VA_ARGS__ __pragma(pack(pop))
#elif defined(__GNUC__) || defined(__clang__)
#define PACKED_STRUCT(...) struct __attribute__((__packed__)) __VA_ARGS__
#else
#error "Unsupported compiler."
#endif

namespace Vuforia {
namespace ExternalProvider {

/// External provider API-version number.
const uint32_t EXTERNAL_PROVIDER_API_VERSION = 1;

/// FrameFormat enum.
/**
 *  A list of the supported pixel formats for camera frames.
 */
enum class FrameFormat : int32_t
{
    UNKNOWN,    ///< Unknown format.
    YUYV        ///< YUYV (YUY2) interleaved format.
};

/// FocusMode enum.
/**
 *  Camera focus modes.
 */
enum class FocusMode : int32_t
{
    UNKNOWN,            ///< Unknown focus mode.
    AUTO,               ///< Single trigger auto focus.
    CONTINUOUS_AUTO,    ///< Continuous auto focus.
    MACRO,              ///< Macro mode.
    INFINITY_FOCUS,     ///< Focus to infinity.
    FIXED               ///< Fixed focus that can't be adjusted.
};

/// ExposureMode enum.
/**
 *  Camera exposure modes.
 */
enum class ExposureMode : int32_t
{
    UNKNOWN,            ///< Unknown exposure mode.
    AUTO,               ///< Single trigger auto exposure.
    CONTINUOUS_AUTO,    ///< Continuous auto exposure.
    MANUAL,             ///< Manual exposure mode.
    SHUTTER_PRIORITY    ///< Shutter priority mode.
};

/// CameraMode struct.
/**
 *  A struct used to describe the size, frame rate
 *  and format of a camera frame.
 */
PACKED_STRUCT(CameraMode
{
    uint32_t width{ 0 };                        ///< Frame width.
    uint32_t height{ 0 };                       ///< Frame height.
    uint32_t fps{ 0 };                          ///< Frame rate. Frames per second.
    FrameFormat format{ FrameFormat::YUYV };    ///< Frame format.
});

/// CameraIntrinsics struct.
/**
 *  Describes the properties required to support
 *  the intrinsics for a camera.
 *  These values should be obtained from camera calibration.
 */
PACKED_STRUCT(CameraIntrinsics
{
    ///< Focal length x-component. 0.f if not available.
    float focalLengthX{ 0.f };

    ///< Focal length y-component. 0.f if not available.
    float focalLengthY{ 0.f };

    ///< Principal point x-component. 0.f if not available.
    float principalPointX{ 0.f };

    ///< Principal point y-component. 0.f if not available.
    float principalPointY{ 0.f };

    ///< An 8 element array of distortion coefficients.
    ///< Array should be filled in the following order (r: radial, t:tangential):
    ///< [r0, r1, t0, t1, r2, r3, r4, r5]
    ///< Values that are not available should be set to 0.f.
    float distortionCoefficients[8]{ 0.f };
});

/// CameraFrame struct.
/**
 *  Describes a camera frame.
 */
PACKED_STRUCT(CameraFrame
{
    ///< Frame timestamp at end of exposure in nanoseconds.
    ///< The time base varies between the platforms:
    ///< Android: CLOCK_MONOTONIC, current timestamp can be obtained with clock_gettime()
    uint64_t timestamp{ 0 };

    ///< Exposure duration in nanoseconds.
    uint64_t exposureTime{ 0 };

    ///< Pointer to first byte of the pixel buffer.
    uint8_t* buffer{ nullptr };

    ///< Size of the pixel buffer in bytes.
    uint32_t bufferSize{ 0 };

    ///< Frame index, ascending number.
    uint32_t index{ 0 };

    ///< Frame width.
    uint32_t width{ 0 };

    ///< Frame height.
    uint32_t height{ 0 };

    ///< Indicates how many bytes are used per row.
    ///< If the frame is tightly packed this should equal to width * bytes per pixel.
    uint32_t stride{ 0 };
    FrameFormat format{ FrameFormat::YUYV };    ///< Frame format.
    CameraIntrinsics intrinsics;                ///< Camera intrinsics used to capture the frame.
});

/// CameraCallback interface.
/**
 *  Interface that will be used to deliver camera frames to Vuforia.
 */
class CameraCallback
{
public:
    virtual ~CameraCallback() {};
    virtual void onNewCameraFrame(CameraFrame* frame) = 0;
};

/// ExternalCamera interface.
/**
 *  Interface used by Vuforia to interact with the external camera implementation.
 *
 *  The sequence of events between Vuforia and the external camera implementation is as follows:
 *  1. Vuforia calls vuforiaext_createExternalCamera() of the external camera implementation.
 *  2. The implementation creates an ExternalCamera instance and returns it to Vuforia.
 *  3. Vuforia calls ExternalCamera::open() on the returned instance.
 *  4. Vuforia discovers supported camera modes by iterating them by getting the number of modes
 *     with ExternalCamera::getNumSupportedCameraModes() and then iterates over the list
 *     with ExternalCamera::getSupportedCameraMode().
 *  5. Vuforia calls ExternalCamera::start(), which starts the flow of frames into
 *     the provided CameraCallback.
 *  6. On shutdown Vuforia calls
 *     -> ExternalCamera::stop()
 *     -> ExternalCamera::close()
 *     -> and finally vuforiaext_destroyExternalCamera().
 */
class ExternalCamera
{
public:
    virtual ~ExternalCamera() {};

    /// Opens the camera.
    /**
     * After opening the camera, the supported video modes should be available to be queried
     * with getNumSupportedCameraModes() and getSupportedCameraMode().
     *
     * \return A boolean indicating if the camera was opened.
     */
    virtual bool open() = 0;

    /// Closes the camera.
    /**
     * \return A boolean indicating if the camera was closed.
     */
    virtual bool close() = 0;

    /// Starts the camera.
    /**
     * \param cameraMode The requested mode that the camera should deliver the frames in.
     * \param cb Callback that the camera frames should be delivered to.
     *
     * \return A boolean indicating if the camera was started.
     */
    virtual bool start(CameraMode cameraMode, CameraCallback* cb) = 0;

    /// Stops the camera.
    /**
     * \return A boolean indicating if the camera was stopped.
     */
    virtual bool stop() = 0;

    /// Returns the number of supported camera modes.
    /**
     *  Should return the total number of supported camera modes.
     *  Vuforia uses this number then to query the camera modes with
     *  getSupportedCameraMode(), which iterates from 0 to totalNumber - 1.
     *
     * \return Number of camera modes that this camera supports.
     */
    virtual uint32_t getNumSupportedCameraModes() = 0;

    /// Returns a camera mode from a certain index.
    /**
     * \param index Vuforia iterates through indices from 0 to (return-value-of-getNumSupportedCameraModes() - 1).
     * \param out Returns the camera mode from the requested index.
     *
     * \return A boolean indicating if there was a camera mode in the requested index and out-parameter has been populated.
     */
    virtual bool getSupportedCameraMode(uint32_t index, CameraMode* out) = 0;

    /// Determines whether a particular exposure mode is supported.
    /**
     * \return A boolean indicating that the requested mode is supported.
     */
    virtual bool supportsExposureMode(ExposureMode parameter) = 0;

    /// Returns current exposure mode.
    /**
     * \return The current exposure mode.
     */
    virtual ExposureMode getExposureMode() = 0;

    /// Sets the current exposure mode.
    /**
     * \param mode New exposure mode.
     *
     * \return A boolean indicating if setting the exposure mode succeeded.
     */
    virtual bool setExposureMode(ExposureMode mode) = 0;

    /// Determines if setting the exposure manually is supported.
    /**
     * \return A boolean indicating support.
     */
    virtual bool supportsExposureValue() = 0;

    /// Returns the minimum supported value for manual exposure.
    /**
     * \return The minimum value.
     */
    virtual uint64_t getExposureValueMin() = 0;

    /// Returns the maximum supported value for manual exposure.
    /**
     * \return The maximum value.
     */
    virtual uint64_t getExposureValueMax() = 0;

    /// Gets the current manual exposure value.
    /**
     * \return The current value.
     */
    virtual uint64_t getExposureValue() = 0;

    /// Sets the current manual exposure value.
    /**
     * \param exposureTime New value for manual exposure.
     *
     * \return A boolean indicating success.
     */
    virtual bool setExposureValue(uint64_t exposureTime) = 0;

    /// Determines whether a particular focus mode is supported.
    /**
     * \return A boolean indicating that the requested mode is supported.
     */
    virtual bool supportsFocusMode(FocusMode parameter) = 0;

    /// Returns the current focus mode.
    /**
     * \return The current focus mode.
     */
    virtual FocusMode getFocusMode() = 0;

    /// Sets the current focus mode.
    /**
     * \param mode The new focus mode.
     *
     * \return A boolean indicating if setting succeeded.
     */
    virtual bool setFocusMode(FocusMode mode) = 0;

    /// Indicates if setting manual focus distance is supported.
    /**
     * \return A boolean indicating support for manual focus distance.
     */
    virtual bool supportsFocusValue() = 0;

    /// Returns the minimum supported value for manual focus distance in millimeters.
    /**
     * \return The minimum value.
     */
    virtual float getFocusValueMin() = 0;

    /// Returns the maximum supported value for manual focus distance in millimeters.
    /**
     * \return The maximum value.
     */
    virtual float getFocusValueMax() = 0;

    /// Returns the current manual focus distance in millimeters.
    /**
     * \return Current manual focus value.
     */
    virtual float getFocusValue() = 0;

    /// Sets the current manual focus value.
    /**
     * \param value The new value for manual focus distance in millimeters.
     *
     * \return A boolean indicating success.
     */
    virtual bool setFocusValue(float value) = 0;
};

} // namespace ExternalProvider
} // namespace Vuforia

extern "C"
{
    /// Returns the API version number.
    /**
     * This function returns the version of the ExternalProvider-API that this plugin conforms to.
     *
     * \return Should return EXTERNAL_PROVIDER_API_VERSION defined in this file.
     */
    uint32_t                                    vuforiaext_getAPIVersion();

    /// Returns the library version number.
    /**
     * This function should write a versionString-parameter with a user defined library version string
     * with maximum length of maxLen.
     *
     * \note Empty strings are not supported
     * \return The number of bytes written into versionString.
     */
    uint32_t                                    vuforiaext_getLibraryVersion(char* rgchOut, const uint32_t cchMax);

    /// Constructs a new instance of a ExternalCamera.
    /**
     * Vuforia will use this instance to interact with the library. The object is
     * expected to be valid until vuforiaext_destroyExternalCamera() is called.
     * The memory for the object is owned by the library.
     *
     * \return New camera instance.
     */
    Vuforia::ExternalProvider::ExternalCamera*  vuforiaext_createExternalCamera(void* pvUser);

    /// Destructs an pExternalCamera of an ExternalCamera object.
    /**
     * Vuforia will call this to destroy the pExternalCamera that was created with vuforiaext_createExternalCamera().
     */
    void                                        vuforiaext_destroyExternalCamera(Vuforia::ExternalProvider::ExternalCamera* pExternalCamera);
}

#endif // VUFORIA_EXTERNAL_PROVIDER_H_
