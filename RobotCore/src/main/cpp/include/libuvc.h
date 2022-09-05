#ifndef LIBUVC_H
#define LIBUVC_H

#include <stdio.h> // FILE
#include <stdint.h>
#include <sys/time.h>
#include <errno.h>
extern "C"
    {
    #include <libusb.h>
    }
#include <libuvc_config.h>
#include <ftc.h>
#include <utlist.h>

struct libusb_context;
struct libusb_device_handle;

/** UVC error types, largely based on libusb errors
 * @ingroup diag
 */
typedef enum uvc_error
    {
    // Many uvc_errors transfer over from libusb errors: you can cast from the latter to the former
    /** Success (no error) */
    UVC_SUCCESS = LIBUSB_SUCCESS,
    /** A simple way to return 'false' distinguished from other constants. Note is >=0, so not an *error**/
    UVC_SUCCESS_FALSE = 1,

    /** Input/output error */
    UVC_ERROR_IO = LIBUSB_ERROR_IO,
    /** Invalid parameter */
    UVC_ERROR_INVALID_PARAM = LIBUSB_ERROR_INVALID_PARAM,
    /** Access denied */
    UVC_ERROR_ACCESS = LIBUSB_ERROR_ACCESS,
    /** No such device */
    UVC_ERROR_NO_DEVICE = LIBUSB_ERROR_NO_DEVICE,
    /** Entity not found */
    UVC_ERROR_NOT_FOUND = LIBUSB_ERROR_NOT_FOUND,
    /** Resource busy */
    UVC_ERROR_BUSY = LIBUSB_ERROR_BUSY,
    /** Operation timed out */
    UVC_ERROR_TIMEOUT = LIBUSB_ERROR_TIMEOUT,
    /** Overflow */
    UVC_ERROR_OVERFLOW = LIBUSB_ERROR_OVERFLOW,
    /** Pipe error */
    UVC_ERROR_PIPE = LIBUSB_ERROR_PIPE,
    /** System call interrupted */
    UVC_ERROR_INTERRUPTED = LIBUSB_ERROR_INTERRUPTED,
    /** Insufficient memory */
    UVC_ERROR_NO_MEM = LIBUSB_ERROR_NO_MEM,
    /** Operation not supported */
    UVC_ERROR_NOT_SUPPORTED = LIBUSB_ERROR_NOT_SUPPORTED,

    /** Device is not UVC-compliant */
    UVC_ERROR_INVALID_DEVICE = -50,
    /** Mode not supported */
    UVC_ERROR_INVALID_MODE = -51,
    /** Resource has a callback (can't use polling and async) */
    UVC_ERROR_CALLBACK_EXISTS = -52,
    /** A system-level error code of some unspecified sort */
    UVC_ERROR_ERRNO = -53,
    /** Invalid arguments */
    UVC_ERROR_INVALID_ARGS = -54,
    /** Undefined error */
    UVC_ERROR_OTHER = LIBUSB_ERROR_OTHER,

    // libusb_transfer_status are positive, we want them negative here so they're errors
    UVC_ERROR_LIBUSB_TRANSFER_BASE = -1000,
	UVC_ERROR_TRANSFER_COMPLETED    = UVC_ERROR_LIBUSB_TRANSFER_BASE - LIBUSB_TRANSFER_COMPLETED,
	UVC_ERROR_TRANSFER_ERROR        = UVC_ERROR_LIBUSB_TRANSFER_BASE - LIBUSB_TRANSFER_ERROR,
	UVC_ERROR_TRANSFER_TIMED_OUT    = UVC_ERROR_LIBUSB_TRANSFER_BASE - LIBUSB_TRANSFER_TIMED_OUT,
	UVC_ERROR_TRANSFER_CANCELLED    = UVC_ERROR_LIBUSB_TRANSFER_BASE - LIBUSB_TRANSFER_CANCELLED,
	UVC_ERROR_TRANSFER_STALL        = UVC_ERROR_LIBUSB_TRANSFER_BASE - LIBUSB_TRANSFER_STALL,
	UVC_ERROR_TRANSFER_NO_DEVICE    = UVC_ERROR_LIBUSB_TRANSFER_BASE - LIBUSB_TRANSFER_NO_DEVICE,
	UVC_ERROR_TRANSFER_OVERFLOW     = UVC_ERROR_LIBUSB_TRANSFER_BASE - LIBUSB_TRANSFER_OVERFLOW,
    } uvc_error_t;

static inline uvc_error uvcErrorFromLibUsbTransferStatus(libusb_transfer_status status)
    {
    return (uvc_error)(UVC_ERROR_LIBUSB_TRANSFER_BASE - status);
    }
static inline uvc_error uvcErrorFromErrno(int err)
    {
    #define ERROR_MAP_CASE(old, new) case old: return new
	// We could refine further, but haven't
    switch (err)
        {
        ERROR_MAP_CASE(0,      UVC_SUCCESS);
        ERROR_MAP_CASE(EINVAL, UVC_ERROR_INVALID_PARAM);
        ERROR_MAP_CASE(EIO,    UVC_ERROR_IO);
        }
    #undef ERROR_MAP_CASE
    return UVC_ERROR_ERRNO;
    }
static inline uvc_error uvcErrorFromErrno()
    {
    return uvcErrorFromErrno(errno);
    }
static inline jboolean tojboolean(uvc_error error)
    {
    return error==UVC_SUCCESS ? JNI_TRUE : JNI_FALSE;
    }

static inline LPCSTR uvcErrorName(uvc_error_t err)
    {
    #define ERROR_NAME_CASE(err)   case err: return #err
    switch (err)
        {
        ERROR_NAME_CASE(UVC_SUCCESS);
        ERROR_NAME_CASE(UVC_SUCCESS_FALSE);
        ERROR_NAME_CASE(UVC_ERROR_IO);
        ERROR_NAME_CASE(UVC_ERROR_INVALID_PARAM);
        ERROR_NAME_CASE(UVC_ERROR_ACCESS);
        ERROR_NAME_CASE(UVC_ERROR_NO_DEVICE);
        ERROR_NAME_CASE(UVC_ERROR_NOT_FOUND);
        ERROR_NAME_CASE(UVC_ERROR_BUSY);
        ERROR_NAME_CASE(UVC_ERROR_TIMEOUT);
        ERROR_NAME_CASE(UVC_ERROR_OVERFLOW);
        ERROR_NAME_CASE(UVC_ERROR_PIPE);
        ERROR_NAME_CASE(UVC_ERROR_INTERRUPTED);
        ERROR_NAME_CASE(UVC_ERROR_NO_MEM);
        ERROR_NAME_CASE(UVC_ERROR_NOT_SUPPORTED);
        ERROR_NAME_CASE(UVC_ERROR_INVALID_DEVICE);
        ERROR_NAME_CASE(UVC_ERROR_INVALID_MODE);
        ERROR_NAME_CASE(UVC_ERROR_CALLBACK_EXISTS);
        ERROR_NAME_CASE(UVC_ERROR_ERRNO);
        ERROR_NAME_CASE(UVC_ERROR_INVALID_ARGS);
        ERROR_NAME_CASE(UVC_ERROR_OTHER);
        ERROR_NAME_CASE(UVC_ERROR_TRANSFER_COMPLETED);
        ERROR_NAME_CASE(UVC_ERROR_TRANSFER_ERROR);
        ERROR_NAME_CASE(UVC_ERROR_TRANSFER_TIMED_OUT);
        ERROR_NAME_CASE(UVC_ERROR_TRANSFER_CANCELLED);
        ERROR_NAME_CASE(UVC_ERROR_TRANSFER_STALL);
        ERROR_NAME_CASE(UVC_ERROR_TRANSFER_NO_DEVICE);
        ERROR_NAME_CASE(UVC_ERROR_TRANSFER_OVERFLOW);
        default: return libusb_error_name(err);
        }
    #undef ERROR_NAME_CASE
    }

// Yes, could place this better in the overall header ordering.
inline uvc_error _uvc_originate_err(uvc_error rc, LPCSTR file, int line)
    {
    if (rc != UVC_SUCCESS)
        {
        UVC_DEBUG("[%s:%d] originating err: %d(%s)", ftcBaseNameOfFile(file), line, int(rc), uvcErrorName(rc));
        }
    return rc;
    }

/**
 * Table 4-7 VC Request Error Code Control XXX add saki@serenegiant.com
 */
enum uvc_vc_error_code_control {
	UVC_ERROR_CODECTRL_NO_ERROR = 0x00,
	UVC_ERROR_CODECTRL_NOT_READY = 0x01,
	UVC_ERROR_CODECTRL_WRONG_STATE = 0x02,
	UVC_ERROR_CODECTRL_POWER = 0x03,
	UVC_ERROR_CODECTRL_OUT_OF_RANGE = 0x04,
	UVC_ERROR_CODECTRL_INVALID_UINT = 0x05,
	UVC_ERROR_CODECTRL_INVALID_CONTROL = 0x06,
	UVC_ERROR_CODECTRL_INVALID_REQUEST = 0x07,
	UVC_ERROR_CODECTRL_INVALID_VALUE = 0x08,
	UVC_ERROR_CODECTRL_UNKNOWN = 0xff
};

/**
 * VS Request Error Code Control XXX add saki@serenegiant.com
 */
enum uvc_vs_error_code_control {
	UVC_VS_ERROR_CODECTRL_NO_ERROR = 0,
	UVC_VS_ERROR_CODECTRL_PROTECTED = 1,
	UVC_VS_ERROR_CODECTRL_IN_BUFEER_UNDERRUN = 2,
	UVC_VS_ERROR_CODECTRL_DATA_DISCONTINUITY = 3,
	UVC_VS_ERROR_CODECTRL_OUT_BUFEER_UNDERRUN = 4,
	UVC_VS_ERROR_CODECTRL_OUT_BUFEER_OVERRUN = 5,
	UVC_VS_ERROR_CODECTRL_FORMAT_CHANGE = 6,
	UVC_VS_ERROR_CODECTRL_STILL_CAPTURE_ERROR = 7,
	UVC_VS_ERROR_CODECTRL_UNKNOWN = 8,
};

/** Color coding of stream, transport-independent
 * @ingroup streaming
 *
 * TODO: make the values of these constants match the values in android.graphics.ImageFormat
 */
enum uvc_frame_format {
  UVC_FRAME_FORMAT_UNKNOWN = 0,
  /** Any supported format */
  UVC_FRAME_FORMAT_ANY = 0,
  UVC_FRAME_FORMAT_UNCOMPRESSED,
  UVC_FRAME_FORMAT_COMPRESSED,

  /** YUYV/YUV2/YUV422: YUV encoding with one luminance value per pixel and
   * one UV (chrominance) pair for every two pixels.
   *
   * http://www.fourcc.org/yuv.php
   */
  UVC_FRAME_FORMAT_YUY2,
  UVC_FRAME_FORMAT_YUYV=UVC_FRAME_FORMAT_YUY2,  // is duplicate, per http://www.fourcc.org/yuv.php
  UVC_FRAME_FORMAT_UYVY,

  /** 24-bit RGB */
  UVC_FRAME_FORMAT_RGB,
  UVC_FRAME_FORMAT_BGR,
  /** Motion-JPEG (or JPEG) encoded images */
  UVC_FRAME_FORMAT_MJPEG,
  UVC_FRAME_FORMAT_GRAY8,
  UVC_FRAME_FORMAT_BY8,
  /** Number of formats understood */
  UVC_FRAME_FORMAT_COUNT,
};

static inline int cbPerPixel(uvc_frame_format format)
    {
    switch (format)
        {
        case UVC_FRAME_FORMAT_GRAY8:
        case UVC_FRAME_FORMAT_BY8:
            return 1;

        case UVC_FRAME_FORMAT_YUY2:
        case UVC_FRAME_FORMAT_UYVY:
            return 2;

        case UVC_FRAME_FORMAT_RGB:
        case UVC_FRAME_FORMAT_BGR:
            return 3;

        default:
            return 0;    // unknown
        }
    }


/* UVC_COLOR_FORMAT_* have been replaced with UVC_FRAME_FORMAT_*. Please use
 * UVC_FRAME_FORMAT_* instead of using these. */
#define UVC_COLOR_FORMAT_UNKNOWN UVC_FRAME_FORMAT_UNKNOWN
#define UVC_COLOR_FORMAT_UNCOMPRESSED UVC_FRAME_FORMAT_UNCOMPRESSED
#define UVC_COLOR_FORMAT_COMPRESSED UVC_FRAME_FORMAT_COMPRESSED
#define UVC_COLOR_FORMAT_YUYV UVC_FRAME_FORMAT_YUYV
#define UVC_COLOR_FORMAT_UYVY UVC_FRAME_FORMAT_UYVY
#define UVC_COLOR_FORMAT_RGB UVC_FRAME_FORMAT_RGB
#define UVC_COLOR_FORMAT_BGR UVC_FRAME_FORMAT_BGR
#define UVC_COLOR_FORMAT_MJPEG UVC_FRAME_FORMAT_MJPEG
#define UVC_COLOR_FORMAT_GRAY8 UVC_FRAME_FORMAT_GRAY8

/** VideoStreaming interface descriptor subtype (A.6 in USB Device Class Definition for Video Devices) */
enum uvc_vs_desc_subtype {
  UVC_VS_UNDEFINED = 0x00,
  UVC_VS_INPUT_HEADER = 0x01,
  UVC_VS_OUTPUT_HEADER = 0x02,
  UVC_VS_STILL_IMAGE_FRAME = 0x03,
  UVC_VS_FORMAT_UNCOMPRESSED = 0x04,
  UVC_VS_FRAME_UNCOMPRESSED = 0x05,
  UVC_VS_FORMAT_MJPEG = 0x06,
  UVC_VS_FRAME_MJPEG = 0x07,
  UVC_VS_FORMAT_MPEG2TS = 0x0a,
  UVC_VS_FORMAT_DV = 0x0c,
  UVC_VS_COLORFORMAT = 0x0d,
  UVC_VS_FORMAT_FRAME_BASED = 0x10,
  UVC_VS_FRAME_FRAME_BASED = 0x11,
  UVC_VS_FORMAT_STREAM_BASED = 0x12
};

static inline LPCSTR vsSubtypeName(uvc_vs_desc_subtype err)
    {
    #define VS_SUBTYPE_CASE(err)   case err: return #err
    switch (err)
        {
        VS_SUBTYPE_CASE(UVC_VS_UNDEFINED);
        VS_SUBTYPE_CASE(UVC_VS_INPUT_HEADER);
        VS_SUBTYPE_CASE(UVC_VS_OUTPUT_HEADER);
        VS_SUBTYPE_CASE(UVC_VS_STILL_IMAGE_FRAME);
        VS_SUBTYPE_CASE(UVC_VS_FORMAT_UNCOMPRESSED);
        VS_SUBTYPE_CASE(UVC_VS_FRAME_UNCOMPRESSED);
        VS_SUBTYPE_CASE(UVC_VS_FORMAT_MJPEG);
        VS_SUBTYPE_CASE(UVC_VS_FRAME_MJPEG);
        VS_SUBTYPE_CASE(UVC_VS_FORMAT_MPEG2TS);
        VS_SUBTYPE_CASE(UVC_VS_FORMAT_DV);
        VS_SUBTYPE_CASE(UVC_VS_COLORFORMAT);
        VS_SUBTYPE_CASE(UVC_VS_FORMAT_FRAME_BASED);
        VS_SUBTYPE_CASE(UVC_VS_FRAME_FRAME_BASED);
        VS_SUBTYPE_CASE(UVC_VS_FORMAT_STREAM_BASED);
        default: return "unknown";
        }
    #undef VS_SUBTYPE_CASE
    }


struct uvc_format_desc;
struct uvc_frame_desc;

/** Frame descriptor
 *
 * A "frame" is a configuration of a streaming format
 * for a particular image size at one of possibly several
 * available frame rates.
 */
typedef struct uvc_frame_desc : DL_NONCIRCULAR_LIST_ENTRY<struct uvc_frame_desc> {
  struct uvc_format_desc *parent;
  /** Type of frame, such as JPEG frame or uncompressed frame */
  enum uvc_vs_desc_subtype bDescriptorSubtype;
  /** Index of the frame within the list of specs available for this format */
  uint8_t bFrameIndex;
  uint8_t bmCapabilities;
  /** Image width */
  uint16_t wWidth;
  /** Image height */
  uint16_t wHeight;
  /** Bitrate of corresponding stream at minimal frame rate */
  uint32_t dwMinBitRate;
  /** Bitrate of corresponding stream at maximal frame rate */
  uint32_t dwMaxBitRate;
  /** @deprecated Maximum number of bytes for a video frame. Only valid
   * for "Uncompressed Payload" */
  uint32_t dwMaxVideoFrameBufferSize;
  /** Default frame interval (in 100ns units) */
  uint32_t dwDefaultFrameInterval;
  /** Minimum frame interval for continuous mode (100ns units) */
  uint32_t dwMinFrameInterval;
  /** Maximum frame interval for continuous mode (100ns units) */
  uint32_t dwMaxFrameInterval;
  /** Granularity of frame interval range for continuous mode (100ns) */
  uint32_t dwFrameIntervalStep;
  /** Frame intervals */
  uint8_t bFrameIntervalType;
  /** number of bytes per line */
  uint32_t dwBytesPerLine;
  /** Available frame rates, zero-terminated (in 100ns units) */
  uint32_t *rgIntervals;
} uvc_frame_desc_t;

/** Format descriptor
 *
 * A "format" determines a stream's image type (e.g., raw YUYV or JPEG)
 * and includes many "frame" configurations.
 */
typedef struct uvc_format_desc : DL_NONCIRCULAR_LIST_ENTRY<struct uvc_format_desc> {
  struct uvc_streaming_interface *pStreamingInterface;  // parent
  /** Type of image stream, such as JPEG or uncompressed. */
  enum uvc_vs_desc_subtype bDescriptorSubtype;
  /** Identifier of this format within the VS interface's format list */
  uint8_t bFormatIndex;
  uint8_t bNumFrameDescriptors;
  /** Format specifier */
  union {
    uint8_t guidFormat[16];
    uint8_t fourccFormat[4];
  };
  /** Format-specific data */
  union {
    /** BPP for uncompressed stream */
    uint8_t bBitsPerPixel;
    /** Flags for JPEG stream */
    uint8_t bmFlags;
  };
  /** Default {uvc_frame_desc} to choose given this format */
  uint8_t bDefaultFrameIndex;
  uint8_t bAspectRatioX;
  uint8_t bAspectRatioY;
  uint8_t bmInterlaceFlags;
  uint8_t bCopyProtect;
  uint8_t bVariableSize;
  /** Available frame specifications for this format */
  struct uvc_frame_desc *frame_descs;
} uvc_format_desc_t;

/** UVC request code (A.8) */
enum uvc_req_code {
    UVC_RC_UNDEFINED = 0x00,
	UVC_SET_CUR = 0x01,			// bmRequestType=0x21
	UVC_GET_CUR = 0x81,			// bmRequestType=0xa1 Current setting attribute
	UVC_GET_MIN = 0x82,			// ↑ Minimum setting attribute
	UVC_GET_MAX = 0x83,			// ↑ Maximum setting attribute
	UVC_GET_RES = 0x84,			// ↑ Resolution attribute
	UVC_GET_LEN = 0x85,			// ↑ Data length attribute
	UVC_GET_INFO = 0x86,		// ↑ Information attribute
	UVC_GET_DEF = 0x87			// ↑
};

static inline bool isSet(const uvc_req_code code)
    {
    return code==UVC_SET_CUR;
    }
static inline bool isGet(const uvc_req_code code)
    {
    return !isSet(code);
    }

enum uvc_device_power_mode {
  UVC_VC_VIDEO_POWER_MODE_FULL = 0x000b,
  UVC_VC_VIDEO_POWER_MODE_DEVICE_DEPENDENT = 0x001b,
};

/**
 * Camera terminal control selector
 *      (A.9.4), Table A-12
 */
enum class UvcCtCtrlSelector
    {
    CONTROL_UNDEFINED = 0x00,
    SCANNING_MODE = 0x01,
    AE_MODE = 0x02,
    AE_PRIORITY = 0x03,
    EXPOSURE_TIME_ABSOLUTE = 0x04,
    EXPOSURE_TIME_RELATIVE = 0x05,
    FOCUS_ABSOLUTE = 0x06,
    FOCUS_RELATIVE = 0x07,

    FOCUS_AUTO = 0x08,
    IRIS_ABSOLUTE = 0x09,
    IRIS_RELATIVE = 0x0a,
    ZOOM_ABSOLUTE = 0x0b,
    ZOOM_RELATIVE = 0x0c,
    PANTILT_ABSOLUTE = 0x0d,
    PANTILT_RELATIVE = 0x0e,
    ROLL_ABSOLUTE = 0x0f,

    ROLL_RELATIVE = 0x10,
    PRIVACY = 0x11,
    FOCUS_SIMPLE = 0x12,
    DIGITAL_WINDOW = 0x13,
    REGION_OF_INTEREST = 0x14
    };

/**
 * UVC Spec, 3.7.2.3 Table 3-6 Camera Terminal Descriptor (bmControls)
 */
enum class UvcCtCtrlSupported
    {
    SCANNING_MODE,
    AE_MODE,
    AE_PRIORITY,
    EXPOSURE_TIME_ABSOLUTE,
    EXPOSURE_TIME_RELATIVE,
    FOCUS_ABSOLUTE,
    FOCUS_RELATIVE,
    IRIS_ABSOLUTE,

    IRIS_RELATIVE,
    ZOOM_ABSOLUTE,
    ZOOM_RELATIVE,
    PANTILT_ABSOLUTE,
    PANTILT_RELATIVE,
    ROLL_ABSOLUTE,
    ROLL_RELATIVE,
    RESERVED_0,

    RESERVED_1,
    FOCUS_AUTO,
    PRIVACY,
    FOCUS_SIMPLE,
    DIGITAL_WINDOW,
    REGION_OF_INTEREST,
    RESERVED_2,
    RESERVED_3
    };

/**
 * From UVC spec, 4.2.2.1.8 Focus, Auto Control
 */
enum class UvcSimpleFocusMode : int32_t
    {
    FULL_RANGE,
    MACRO,
    PEOPLE,
    SCENE
    };

/**
 * From UVC spec, 4.2.2.1.9 Focus, Auto Control
 */
enum class UvcAutoFocusMode : int32_t
    {
    FIXED,
    AUTO
    };

/**
 * From UVC spec, 4.2.2.1.2 Auto-Exposure Mode Control
 */
enum class UvcAutoExposureMode : int32_t
    {
    MANUAL            = 1,
    AUTO              = 2,
    SHUTTER_PRIORITY  = 4,
    APERTURE_PRIORITY = 8
    };


/** Processing unit control selector (A.9.5) */
enum uvc_pu_ctrl_selector {
  UVC_PU_CONTROL_UNDEFINED = 0x00,
  UVC_PU_BACKLIGHT_COMPENSATION_CONTROL = 0x01,
  UVC_PU_BRIGHTNESS_CONTROL = 0x02,
  UVC_PU_CONTRAST_CONTROL = 0x03,
  UVC_PU_GAIN_CONTROL = 0x04,
  UVC_PU_POWER_LINE_FREQUENCY_CONTROL = 0x05,
  UVC_PU_HUE_CONTROL = 0x06,
  UVC_PU_SATURATION_CONTROL = 0x07,
  UVC_PU_SHARPNESS_CONTROL = 0x08,
  UVC_PU_GAMMA_CONTROL = 0x09,
  UVC_PU_WHITE_BALANCE_TEMPERATURE_CONTROL = 0x0a,
  UVC_PU_WHITE_BALANCE_TEMPERATURE_AUTO_CONTROL = 0x0b,
  UVC_PU_WHITE_BALANCE_COMPONENT_CONTROL = 0x0c,
  UVC_PU_WHITE_BALANCE_COMPONENT_AUTO_CONTROL = 0x0d,
  UVC_PU_DIGITAL_MULTIPLIER_CONTROL = 0x0e,
  UVC_PU_DIGITAL_MULTIPLIER_LIMIT_CONTROL = 0x0f,
  UVC_PU_HUE_AUTO_CONTROL = 0x10,
  UVC_PU_ANALOG_VIDEO_STANDARD_CONTROL = 0x11,
  UVC_PU_ANALOG_LOCK_STATUS_CONTROL = 0x12,
  UVC_PU_CONTRAST_AUTO_CONTROL = 0x13
};

/** USB terminal type (B.1) */
enum uvc_term_type {
  UVC_TT_VENDOR_SPECIFIC = 0x0100,
  UVC_TT_STREAMING = 0x0101
};

/** Input terminal type (B.2) */
enum uvc_it_type {
  UVC_ITT_VENDOR_SPECIFIC = 0x0200,
  UVC_ITT_CAMERA = 0x0201,
  UVC_ITT_MEDIA_TRANSPORT_INPUT = 0x0202
};

/** Output terminal type (B.3) */
enum uvc_ot_type {
  UVC_OTT_VENDOR_SPECIFIC = 0x0300,
  UVC_OTT_DISPLAY = 0x0301,
  UVC_OTT_MEDIA_TRANSPORT_OUTPUT = 0x0302
};

/** External terminal type (B.4) */
enum uvc_et_type {
  UVC_EXTERNAL_VENDOR_SPECIFIC = 0x0400,
  UVC_COMPOSITE_CONNECTOR = 0x0401,
  UVC_SVIDEO_CONNECTOR = 0x0402,
  UVC_COMPONENT_CONNECTOR = 0x0403
};

/** Context, equivalent to libusb's contexts.
 *
 * May either own a libusb context or use one that's already made.
 *
 * Always create these with uvc_get_context.
 */
struct uvc_context;
typedef struct uvc_context uvc_context_t;

/** UVC device.
 *
 * Get this from uvc_get_device_list() or uvc_find_device().
 */
struct uvc_device;
typedef struct uvc_device uvc_device_t;

/** Handle on an open UVC device.
 *
 * Get one of these from uvc_open(). Once you uvc_close()
 * it, it's no longer valid.
 */
struct uvc_device_handle;
typedef struct uvc_device_handle uvc_device_handle_t;

/** Handle on an open UVC stream.
 *
 * Get one of these from uvc_stream_open*().
 * Once you uvc_stream_close() it, it will no longer be valid.
 */
struct uvc_stream_handle;
typedef struct uvc_stream_handle uvc_stream_handle_t;

/** Representation of the interface that brings data into the UVC device */
typedef struct uvc_input_terminal : DL_NONCIRCULAR_LIST_ENTRY<uvc_input_terminal> {
	/** Index of the terminal within the device */
	uint8_t bTerminalID;
	/** Type of terminal (e.g., camera) */
	enum uvc_it_type wTerminalType;
	uint16_t wObjectiveFocalLengthMin;
	uint16_t wObjectiveFocalLengthMax;
	uint16_t wOcularFocalLength;
	/** Camera controls (meaning of bits given in {@link UvcCtCtrlSupported}) */
	uint64_t bmControls;
	/** request code(wIndex) */
	uint16_t request;

    bool isSupported(UvcCtCtrlSupported ctrl) const
        {
        return (bmControls & (1<<int(ctrl))) != 0;
        }
} uvc_input_terminal_t;

typedef struct uvc_output_terminal : DL_NONCIRCULAR_LIST_ENTRY<uvc_output_terminal>  {
	/** @todo */
	/** Index of the terminal within the device */
	uint8_t bTerminalID;
	/** Type of terminal (e.g., camera) */
	enum uvc_ot_type wTerminalType;
	uint16_t bAssocTerminal;
	uint8_t bSourceID;
	uint8_t iTerminal;
	/** request code(wIndex) */
	uint16_t request;
} uvc_output_terminal_t;

/** Represents post-capture processing functions */
typedef struct uvc_processing_unit : DL_NONCIRCULAR_LIST_ENTRY<uvc_processing_unit>  {
	/** Index of the processing unit within the device */
	uint8_t bUnitID;
	/** Index of the terminal from which the device accepts images */
	uint8_t bSourceID;
	/** Processing controls (meaning of bits given in {uvc_pu_ctrl_selector}) */
	uint64_t bmControls;
	/** request code(wIndex) */
	uint16_t request;
} uvc_processing_unit_t;

/** Represents selector unit to connect other units */
typedef struct uvc_selector_unit : DL_NONCIRCULAR_LIST_ENTRY<uvc_selector_unit> {
  /** Index of the selector unit within the device */
  uint8_t bUnitID;
} uvc_selector_unit_t;

/** Custom processing or camera-control functions */
typedef struct uvc_extension_unit : DL_NONCIRCULAR_LIST_ENTRY<uvc_extension_unit> {
    /** Index of the extension unit within the device */
	uint8_t bUnitID;
	/** GUID identifying the extension unit */
	uint8_t guidExtensionCode[16];
	/** Bitmap of available controls (manufacturer-dependent) */
	uint64_t bmControls;
	/** request code(wIndex) */
	uint16_t request;
} uvc_extension_unit_t;

enum uvc_status_class {
  UVC_STATUS_CLASS_CONTROL = 0x10,
  UVC_STATUS_CLASS_CONTROL_CAMERA = 0x11,
  UVC_STATUS_CLASS_CONTROL_PROCESSING = 0x12,
};

// USB Device Class Definition for Video Devices, Table 2-2 Status Packet Format (VideoControl Interface as the Originator)
enum uvc_status_attribute {
  UVC_STATUS_ATTRIBUTE_VALUE_CHANGE = 0x00,
  UVC_STATUS_ATTRIBUTE_INFO_CHANGE = 0x01,
  UVC_STATUS_ATTRIBUTE_FAILURE_CHANGE = 0x02,
  UVC_STATUS_ATTRIBUTE_MIN_CHANGE = 0x03,
  UVC_STATUS_ATTRIBUTE_MAX_CHANGE = 0x04,
  UVC_STATUS_ATTRIBUTE_UNKNOWN = 0xff
};

/** A callback function to accept status updates
 * @ingroup device
 */
typedef void(uvc_status_callback_t)(enum uvc_status_class status_class,
                                    int event,
                                    int selector,
                                    enum uvc_status_attribute status_attribute,
                                    void *data, size_t data_len,
                                    void *user_ptr);

/** A callback function to accept button events
 * @ingroup device
 */
typedef void(uvc_button_callback_t)(int button,
                                    int state,
                                    void *user_ptr);

/** Structure representing a UVC device descriptor.
 *
 * (This isn't a standard structure.)
 */
typedef struct uvc_device_descriptor {
  /** Vendor ID */
  uint16_t idVendor;
  /** Product ID */
  uint16_t idProduct;
  /** UVC compliance level, e.g. 0x0100 (1.0), 0x0110 */
  uint16_t bcdUVC;
  /** Serial number (null if unavailable) */
  const char *serialNumber;
  /** Device-reported manufacturer name (or null) */
  const char *manufacturer;
  /** Device-reporter product name (or null) */
  const char *product;
} uvc_device_descriptor_t;


typedef int32_t framenumber_t;
const framenumber_t  frameNumberInvalid = -1;

/** An image frame received from the UVC device
 * @ingroup streaming
 */
typedef struct uvc_frame : ZeroOnNew
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    /** Image data for this frame */
    uint8_t *pbData;
    /** The number of valid bytes actually contained in the frame */
    size_t cbData;
    /** The size of the buffer that was allocated *pbData */
    size_t cbAllocated;
    /** size we *expect* to see when complete. We avoid delivering non-full frames to users */
    size_t cbExpected;

    /** Width of image in pixels */
    uint32_t width;
    /** Height of image in pixels */
    uint32_t height;
    /** Pixel data format */
    uvc_frame_format frameFormat;

    /** Number of bytes per horizontal line (undefined/zero for compressed format) */
    size_t cbLineStride;

    /** Frame number. May (rarely) skip, but is strictly monotonically increasing) */
    framenumber_t frameNumber;
    /** Presentation timestamp from the protocol */
    uint32_t pts;
    /** Estimate of system time when the device captured the image. Value in ns */
    jlong captureTime;   // was struct timeval captureTime;
    /** Source Clock Reference from the UVC spec. Not yet understood; more investigation needed */
    uint32_t sourceClockReference;
    /** The context in which we live */
    uvc_context* pContext;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    uvc_frame(uvc_context* pContext, size_t cbFrameAlloc, size_t cbExpected)
        {
        this->frameNumber = frameNumberInvalid;
        this->pContext = pContext;
        this->cbExpected = cbExpected;
        if (cbFrameAlloc > 0)
            {
            cbAllocated = cbFrameAlloc;
            cbData = 0;
            pbData = typedMalloc<uint8_t>(cbFrameAlloc);
            }
        }

    bool ctorOK()
        {
        return cbData==0 || pbData != NULL;
        }

    ~uvc_frame()
        {
        freeData();
        }

    void freeData()
        {
        free(pbData);
        pbData = NULL;
        cbData = 0;
        cbAllocated = 0;
        }

    //----------------------------------------------------------------------------------------------
    // Operations
    //----------------------------------------------------------------------------------------------

    uvc_context* getContext()
        {
        return this->pContext;
        }

    void resetAppend()
        {
        cbData = 0;
        }

    uvc_error_t append(void* pv, size_t cb)
        {
        uvc_error_t rc = UVC_SUCCESS;
        if (cbData + cb <= cbAllocated)
            {
            memcpy(&pbData[cbData], pv, cb);
            cbData += cb;
            }
        else
            rc = uvc_originate_err(UVC_ERROR_OVERFLOW);
        return rc;
        }

    /**
     * Ensures that the allocated size of the frame is AT LEAST big
     * enough to hold the requested number of bytes. On successful exit,
     * the 'cbFrame' field will EXACTLY match the requested size.
     */
    uvc_error_t ensureSize(size_t cbNeeded)
        {
        if (pbData)
            {
            if (cbAllocated < cbNeeded)
                {
                pbData = typedReallocNoPreserve(pbData, cbNeeded);
                cbAllocated = cbNeeded;
                }
            }
        else
            {
            pbData = typedMalloc<uint8_t>(cbNeeded);
            cbAllocated = cbNeeded;
            }
        cbData = cbNeeded;
        return pbData ? UVC_SUCCESS : uvc_originate_err(UVC_ERROR_NO_MEM);
        }

    uvc_error_t copyTo(uvc_frame *pCopy)
        {
        uvc_error_t result = pCopy->ensureSize(cbData);
        if (!result)
            {
            pCopy->cbExpected = cbExpected;
            pCopy->width = width;
            pCopy->height = height;
            pCopy->frameFormat = frameFormat;
            pCopy->cbLineStride = cbLineStride;
            pCopy->frameNumber = frameNumber;
            pCopy->pts = pts;
            pCopy->captureTime = captureTime;
            pCopy->sourceClockReference = sourceClockReference;
            pCopy->pContext = pContext;

            if (cbLineStride != 0 && pCopy->cbLineStride)
                {
                const int cbStrideFrom = this->cbLineStride;
                const int cbStrideTo = pCopy->cbLineStride;
                const int cLine = this->height < pCopy->height ? this->height : pCopy->height;
                const int cbStrideCopy = cbStrideFrom < cbStrideTo ? cbStrideFrom : cbStrideTo;
                uint8_t* pbFrom = this->pbData;
                uint8_t* pbTo = pCopy->pbData;
                for (int iLine = 0; iLine < cLine; iLine ++)
                    {
                    memcpy(pbTo, pbFrom, cbStrideCopy);
                    pbFrom += cbStrideFrom;
                    pbTo += cbStrideTo;
                    }
                }
            else
                {
                memcpy(pCopy->pbData, pbData, cbData);
                }
            }
        return result;
        }

    } uvc_frame_t;

/** A callback function to handle incoming assembled UVC frames
 * @ingroup streaming
 */
typedef void(*PfnUserCallback)(struct uvc_frame *pFrame, void *pvUserCallback);

/** Streaming mode, includes all information needed to select stream
 * @ingroup streaming. Table 4-75, UVC Spec (p135)
 */
typedef struct uvc_stream_ctrl {
  /*
   * Bitfield control indicating to the function
   * what fields shall be kept fixed (indicative
   * only):
   *    D0: dwFrameInterval
   *    D1: wKeyFrameRate
   *    D2: wPFrameRate
   *    D3: wCompQuality
   *    D4: wCompWindowSize
   *    D15..5: Reserved
   */
  uint16_t bmHint;
  uint8_t bFormatIndex;     // Video format index from a Format descriptor for this video interface
  uint8_t bFrameIndex;      // Video frame index from a Frame descriptor. This field selects the video frame resolution from the array of resolutions supported by the selected stream
  uint32_t dwFrameInterval; // Frame interval in 100 ns units.
  uint16_t wKeyFrameRate;
  uint16_t wPFrameRate;
  uint16_t wCompQuality;
  uint16_t wCompWindowSize;
  uint16_t wDelay;
  uint32_t dwMaxVideoFrameSize;
  uint32_t dwMaxPayloadTransferSize;
  // 1.1
  uint32_t dwClockFrequency;
  uint8_t bmFramingInfo;
  uint8_t bPreferredVersion;
  uint8_t bMinVersion;
  uint8_t bMaxVersion;
  // 1.5
  uint8_t bUsage;
  uint8_t bBitDepthLuma;
  uint8_t bmSettings;
  uint8_t bMaxNumberOfRefFramesPlus1;
  uint16_t bmRateControlModes;
  uint64_t bmLayoutPerStream;
  //
  uint8_t bInterfaceNumber;
} uvc_stream_ctrl_t;

uvc_error_t uvc_init(uvc_context_t **ppctx, LPCSTR szUsbfs, int buildVersionSDKInt, LPCSTR szTempFolder, bool forceJavaUsbEnumerationKitKat);
void uvc_exit(uvc_context_t *pctx);

uvc_error_t uvc_is_usb_device_compatible(uvc_context_t *ctx, libusb_device *pLibUsbDevice, /*OUT*/ bool *pIsCompatible);
uvc_error uvc_create_uvc_device(uvc_context_t *ctx, /*optional*/ libusb_device *pUsbDevice, /*OUT*/ uvc_device **ppUvcDevice);
uvc_error_t uvc_device_from_libusb_device(uvc_context_t* ctx, libusb_device *pUsbDevice, /*OUT*/ uvc_device** ppUvcDevice);

uvc_error_t uvc_get_device_listKitKat(
    uvc_context_t *ctx,
    uvc_device_t ***prgpUvcDeviceResult);
void uvc_free_device_list(uvc_device_t **list, uint8_t unref_devices);

uvc_error_t uvc_get_device_descriptor(
    uvc_device_t *dev,
    uvc_device_descriptor_t **desc);
void uvc_free_device_descriptor(
    uvc_device_descriptor_t *desc);

uint8_t uvc_get_bus_number(uvc_device_t *dev);
uint8_t uvc_get_device_address(uvc_device_t *dev);

uvc_error_t uvc_find_device(
    uvc_context_t *ctx,
    uvc_device_t **dev,
    int vid, int pid, const char *sn);

uvc_error_t uvc_find_devices(
    uvc_context_t *ctx,
    uvc_device_t ***devs,
    int vid, int pid, const char *sn);

struct UsbInterfaceManager : RefCounted
    {
    virtual uvc_error claimInterface(int idx) = 0;
    virtual uvc_error releaseInterface(int idx) = 0;
    virtual uvc_error setInterfaceAltSetting(int bInterfaceNumber, int bAlternateSetting) = 0;
    };

uvc_error_t uvc_open(
    uvc_device_t *dev,
    UsbInterfaceManager* pInterfaceManager,
    uvc_device_handle **devh);
void uvc_release_ref(uvc_device_handle*& devh);


uvc_device_t *uvc_get_device(uvc_device_handle_t *devh);
struct libusb_device_handle *uvc_get_libusb_handle(uvc_device_handle_t *devh);

void uvc_ref_device(uvc_device_t *dev);
void uvc_unref_device(uvc_device_t *dev);

void uvc_set_status_callback(uvc_device_handle_t *devh,
                             uvc_status_callback_t cb,
                             void *user_ptr);

void uvc_set_button_callback(uvc_device_handle_t *devh,
                             uvc_button_callback_t cb,
                             void *user_ptr);

const uvc_input_terminal_t *uvc_get_camera_terminal(uvc_device_handle_t *devh);
const uvc_input_terminal_t *uvc_get_input_terminals(uvc_device_handle_t *devh);
const uvc_output_terminal_t *uvc_get_output_terminals(uvc_device_handle_t *devh);
const uvc_selector_unit_t *uvc_get_selector_units(uvc_device_handle_t *devh);
const uvc_processing_unit_t *uvc_get_processing_units(uvc_device_handle_t *devh);
const uvc_extension_unit_t *uvc_get_extension_units(uvc_device_handle_t *devh);

uvc_error uvc_get_stream_ctrl_format_size(
    uvc_device_handle_t *devh,
    uvc_stream_ctrl_t *ctrl,
    enum uvc_frame_format format,
    int width, int height,
    int fps
    );

const uvc_format_desc_t *uvc_get_format_descs(uvc_device_handle_t* );

uvc_error uvc_probe_stream_ctrl(
    uvc_device_handle_t *devh,
    uvc_stream_ctrl_t *ctrl);

uvc_error uvc_start_streaming(
    uvc_device_handle_t *devh,
    uvc_stream_ctrl_t *ctrl,
    PfnUserCallback pfnUserCallback,
    void *pvUserCallback,
    uint8_t flags);

uvc_error uvc_start_iso_streaming(
    uvc_device_handle_t *devh,
    uvc_stream_ctrl_t *ctrl,
    PfnUserCallback pfnUserCallback,
    void *pvUserCallback);

void uvc_stop_streaming(uvc_device_handle_t *devh);

uvc_error_t uvc_stream_open_ctrl(uvc_device_handle_t *devh, uvc_stream_handle_t **ppStreamHandleResult, uvc_stream_ctrl_t *ctrl);
uvc_error_t uvc_commit_stream_ctrl(uvc_stream_handle_t *strmh, uvc_stream_ctrl_t *ctrl);
uvc_error_t uvc_stream_start(uvc_stream_handle_t *strmh,
    PfnUserCallback pfnUserCallback,
    void *pvUserCallback,
    uint8_t flags);
uvc_error_t uvc_stream_start_bandwidth(uvc_stream_handle_t *strmh,
    PfnUserCallback pfnUserCallback,
    void *pvUserCallback,
    float bandwidth_factor,
    uint8_t flags);
uvc_error_t uvc_stream_start_iso(uvc_stream_handle_t *strmh,
    PfnUserCallback pfnUserCallback,
    void *pvUserCallback);
uvc_error_t uvc_stream_get_frame(
	uvc_stream_handle_t *strmh,
	uvc_frame_t **ppFrameResult,
	int32_t timeout_us
);
uvc_error_t uvc_stream_stop(uvc_stream_handle_t *strmh);
void uvc_stream_close(uvc_stream_handle_t *strmh);

int uvc_get_ctrl_len(uvc_device_handle_t *devh, uint8_t unit, uint8_t ctrl);
int uvc_get_ctrl(uvc_device_handle_t *devh, uint8_t unit, uint8_t ctrl, void *data, int len, enum uvc_req_code req_code);
int uvc_set_ctrl(uvc_device_handle_t *devh, uint8_t unit, uint8_t ctrl, void *data, int len);

uvc_error uvc_vs_get_error_code(uvc_device_handle_t *devh, uvc_vs_error_code_control *error_code, enum uvc_req_code req_code);
uvc_error uvc_vc_get_error_code(uvc_device_handle_t *devh, uvc_vc_error_code_control *error_code, enum uvc_req_code req_code);
uvc_error_t uvc_get_power_mode(uvc_device_handle_t *devh, enum uvc_device_power_mode *mode, enum uvc_req_code req_code);
uvc_error_t uvc_set_power_mode(uvc_device_handle_t *devh, enum uvc_device_power_mode mode);

/* AUTO-GENERATED control accessors! Update them with the output of `ctrl-gen.py decl`. */
uvc_error_t uvc_get_scanning_mode(uvc_device_handle_t *devh, uint8_t* mode, enum uvc_req_code req_code);
uvc_error_t uvc_set_scanning_mode(uvc_device_handle_t *devh, uint8_t mode);

uvc_error_t uvc_get_ae_mode(uvc_device_handle_t *devh, uint8_t* mode, enum uvc_req_code req_code);
uvc_error_t uvc_set_ae_mode(uvc_device_handle_t *devh, uint8_t mode);

uvc_error_t uvc_get_ae_priority(uvc_device_handle_t *devh, uint8_t* priority, enum uvc_req_code req_code);
uvc_error_t uvc_set_ae_priority(uvc_device_handle_t *devh, uint8_t priority);

uvc_error_t uvc_get_exposure_abs(uvc_device_handle_t *devh, uint32_t* time, enum uvc_req_code req_code);
uvc_error_t uvc_set_exposure_abs(uvc_device_handle_t *devh, uint32_t time);

uvc_error_t uvc_get_exposure_rel(uvc_device_handle_t *devh, int8_t* step, enum uvc_req_code req_code);
uvc_error_t uvc_set_exposure_rel(uvc_device_handle_t *devh, int8_t step);

uvc_error_t uvc_get_focus_abs(uvc_device_handle_t *devh, uint16_t* focus, enum uvc_req_code req_code);
uvc_error_t uvc_set_focus_abs(uvc_device_handle_t *devh, uint16_t focus);

uvc_error_t uvc_get_focus_rel(uvc_device_handle_t *devh, int8_t* focus_rel, uint8_t* speed, enum uvc_req_code req_code);
uvc_error_t uvc_set_focus_rel(uvc_device_handle_t *devh, int8_t focus_rel, uint8_t speed);

uvc_error_t uvc_get_focus_simple_range(uvc_device_handle_t *devh, uint8_t* focus, enum uvc_req_code req_code);
uvc_error_t uvc_set_focus_simple_range(uvc_device_handle_t *devh, uint8_t focus);

uvc_error_t uvc_get_focus_auto(uvc_device_handle_t *devh, uint8_t* state, enum uvc_req_code req_code);
uvc_error_t uvc_set_focus_auto(uvc_device_handle_t *devh, uint8_t state);

uvc_error_t uvc_get_iris_abs(uvc_device_handle_t *devh, uint16_t* iris, enum uvc_req_code req_code);
uvc_error_t uvc_set_iris_abs(uvc_device_handle_t *devh, uint16_t iris);

uvc_error_t uvc_get_iris_rel(uvc_device_handle_t *devh, uint8_t* iris_rel, enum uvc_req_code req_code);
uvc_error_t uvc_set_iris_rel(uvc_device_handle_t *devh, uint8_t iris_rel);

uvc_error_t uvc_get_zoom_abs(uvc_device_handle_t *devh, uint16_t* focal_length, enum uvc_req_code req_code);
uvc_error_t uvc_set_zoom_abs(uvc_device_handle_t *devh, uint16_t focal_length);

uvc_error_t uvc_get_zoom_rel(uvc_device_handle_t *devh, int8_t* zoom_rel, uint8_t* digital_zoom, uint8_t* speed, enum uvc_req_code req_code);
uvc_error_t uvc_set_zoom_rel(uvc_device_handle_t *devh, int8_t zoom_rel, uint8_t digital_zoom, uint8_t speed);

uvc_error_t uvc_get_pantilt_abs(uvc_device_handle_t *devh, int32_t* pan, int32_t* tilt, enum uvc_req_code req_code);
uvc_error_t uvc_set_pantilt_abs(uvc_device_handle_t *devh, int32_t pan, int32_t tilt);

uvc_error_t uvc_get_pantilt_rel(uvc_device_handle_t *devh, int8_t* pan_rel, uint8_t* pan_speed, int8_t* tilt_rel, uint8_t* tilt_speed, enum uvc_req_code req_code);
uvc_error_t uvc_set_pantilt_rel(uvc_device_handle_t *devh, int8_t pan_rel, uint8_t pan_speed, int8_t tilt_rel, uint8_t tilt_speed);

uvc_error_t uvc_get_roll_abs(uvc_device_handle_t *devh, int16_t* roll, enum uvc_req_code req_code);
uvc_error_t uvc_set_roll_abs(uvc_device_handle_t *devh, int16_t roll);

uvc_error_t uvc_get_roll_rel(uvc_device_handle_t *devh, int8_t* roll_rel, uint8_t* speed, enum uvc_req_code req_code);
uvc_error_t uvc_set_roll_rel(uvc_device_handle_t *devh, int8_t roll_rel, uint8_t speed);

uvc_error_t uvc_get_privacy(uvc_device_handle_t *devh, uint8_t* privacy, enum uvc_req_code req_code);
uvc_error_t uvc_set_privacy(uvc_device_handle_t *devh, uint8_t privacy);

uvc_error_t uvc_get_digital_window(uvc_device_handle_t *devh, uint16_t* window_top, uint16_t* window_left, uint16_t* window_bottom, uint16_t* window_right, uint16_t* num_steps, uint16_t* num_steps_units, enum uvc_req_code req_code);
uvc_error_t uvc_set_digital_window(uvc_device_handle_t *devh, uint16_t window_top, uint16_t window_left, uint16_t window_bottom, uint16_t window_right, uint16_t num_steps, uint16_t num_steps_units);

uvc_error_t uvc_get_digital_roi(uvc_device_handle_t *devh, uint16_t* roi_top, uint16_t* roi_left, uint16_t* roi_bottom, uint16_t* roi_right, uint16_t* auto_controls, enum uvc_req_code req_code);
uvc_error_t uvc_set_digital_roi(uvc_device_handle_t *devh, uint16_t roi_top, uint16_t roi_left, uint16_t roi_bottom, uint16_t roi_right, uint16_t auto_controls);

uvc_error_t uvc_get_backlight_compensation(uvc_device_handle_t *devh, uint16_t* backlight_compensation, enum uvc_req_code req_code);
uvc_error_t uvc_set_backlight_compensation(uvc_device_handle_t *devh, uint16_t backlight_compensation);

uvc_error_t uvc_get_brightness(uvc_device_handle_t *devh, int16_t* brightness, enum uvc_req_code req_code);
uvc_error_t uvc_set_brightness(uvc_device_handle_t *devh, int16_t brightness);

uvc_error_t uvc_get_contrast(uvc_device_handle_t *devh, uint16_t* contrast, enum uvc_req_code req_code);
uvc_error_t uvc_set_contrast(uvc_device_handle_t *devh, uint16_t contrast);

uvc_error_t uvc_get_contrast_auto(uvc_device_handle_t *devh, uint8_t* contrast_auto, enum uvc_req_code req_code);
uvc_error_t uvc_set_contrast_auto(uvc_device_handle_t *devh, uint8_t contrast_auto);

uvc_error_t uvc_get_gain(uvc_device_handle_t *devh, uint16_t* gain, enum uvc_req_code req_code);
uvc_error_t uvc_set_gain(uvc_device_handle_t *devh, uint16_t gain);

uvc_error_t uvc_get_power_line_frequency(uvc_device_handle_t *devh, uint8_t* power_line_frequency, enum uvc_req_code req_code);
uvc_error_t uvc_set_power_line_frequency(uvc_device_handle_t *devh, uint8_t power_line_frequency);

uvc_error_t uvc_get_hue(uvc_device_handle_t *devh, int16_t* hue, enum uvc_req_code req_code);
uvc_error_t uvc_set_hue(uvc_device_handle_t *devh, int16_t hue);

uvc_error_t uvc_get_hue_auto(uvc_device_handle_t *devh, uint8_t* hue_auto, enum uvc_req_code req_code);
uvc_error_t uvc_set_hue_auto(uvc_device_handle_t *devh, uint8_t hue_auto);

uvc_error_t uvc_get_saturation(uvc_device_handle_t *devh, uint16_t* saturation, enum uvc_req_code req_code);
uvc_error_t uvc_set_saturation(uvc_device_handle_t *devh, uint16_t saturation);

uvc_error_t uvc_get_sharpness(uvc_device_handle_t *devh, uint16_t* sharpness, enum uvc_req_code req_code);
uvc_error_t uvc_set_sharpness(uvc_device_handle_t *devh, uint16_t sharpness);

uvc_error_t uvc_get_gamma(uvc_device_handle_t *devh, uint16_t* gamma, enum uvc_req_code req_code);
uvc_error_t uvc_set_gamma(uvc_device_handle_t *devh, uint16_t gamma);

uvc_error_t uvc_get_white_balance_temperature(uvc_device_handle_t *devh, uint16_t* temperature, enum uvc_req_code req_code);
uvc_error_t uvc_set_white_balance_temperature(uvc_device_handle_t *devh, uint16_t temperature);

uvc_error_t uvc_get_white_balance_temperature_auto(uvc_device_handle_t *devh, uint8_t* temperature_auto, enum uvc_req_code req_code);
uvc_error_t uvc_set_white_balance_temperature_auto(uvc_device_handle_t *devh, uint8_t temperature_auto);

uvc_error_t uvc_get_white_balance_component(uvc_device_handle_t *devh, uint16_t* blue, uint16_t* red, enum uvc_req_code req_code);
uvc_error_t uvc_set_white_balance_component(uvc_device_handle_t *devh, uint16_t blue, uint16_t red);

uvc_error_t uvc_get_white_balance_component_auto(uvc_device_handle_t *devh, uint8_t* white_balance_component_auto, enum uvc_req_code req_code);
uvc_error_t uvc_set_white_balance_component_auto(uvc_device_handle_t *devh, uint8_t white_balance_component_auto);

uvc_error_t uvc_get_digital_multiplier(uvc_device_handle_t *devh, uint16_t* multiplier_step, enum uvc_req_code req_code);
uvc_error_t uvc_set_digital_multiplier(uvc_device_handle_t *devh, uint16_t multiplier_step);

uvc_error_t uvc_get_digital_multiplier_limit(uvc_device_handle_t *devh, uint16_t* multiplier_step, enum uvc_req_code req_code);
uvc_error_t uvc_set_digital_multiplier_limit(uvc_device_handle_t *devh, uint16_t multiplier_step);

uvc_error_t uvc_get_analog_video_standard(uvc_device_handle_t *devh, uint8_t* video_standard, enum uvc_req_code req_code);
uvc_error_t uvc_set_analog_video_standard(uvc_device_handle_t *devh, uint8_t video_standard);

uvc_error_t uvc_get_analog_video_lock_status(uvc_device_handle_t *devh, uint8_t* status, enum uvc_req_code req_code);
uvc_error_t uvc_set_analog_video_lock_status(uvc_device_handle_t *devh, uint8_t status);

uvc_error_t uvc_get_input_select(uvc_device_handle_t *devh, uint8_t* selector, enum uvc_req_code req_code);
uvc_error_t uvc_set_input_select(uvc_device_handle_t *devh, uint8_t selector);
/* end AUTO-GENERATED control accessors */

void uvc_perror(uvc_error_t err, const char *msg);
const char* uvc_strerror(uvc_error_t err);
void uvc_print_diag(uvc_device_handle_t *devh, FILE *stream);
void uvc_print_stream_ctrl(uvc_stream_ctrl_t *ctrl, FILE *stream);

uvc_frame *uvc_allocate_frame(uvc_context* pContext, size_t cbFrameAlloc, size_t cbFrameExpected);
void uvc_free_frame(uvc_frame *pFrame);
uvc_error_t uvc_duplicate_frame(uvc_frame *in, uvc_frame *out);
uvc_error_t uvc_yuyv2rgb(uvc_frame_t *in, uvc_frame_t *out);

#endif // !def(LIBUVC_H)

