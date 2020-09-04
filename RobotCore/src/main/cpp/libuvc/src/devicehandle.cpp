//
// devicehandle.cpp
//

#include "libuvc.h"
#include "libuvc/libuvc_internal.h"
#include "errno.h"

uvc_stream_handle *uvc_device_handle::getStreamByInterface(int index)
    {
    uvc_stream_handle_t *strmh;
    DL_FOREACH(this->openStreamsList, strmh)
        {
        if (strmh->stream_if->bInterfaceNumber == index)
            return strmh;
        }

    return NULL;
    }

uvc_streaming_interface* uvc_device_handle::getStreamInterface(int index)
    {
    uvc_streaming_interface_t *stream_if;
    DL_FOREACH(this->info->stream_ifs, stream_if)
        {
        if (stream_if->bInterfaceNumber == index)
            return stream_if;
        }
    return NULL;
    }

/** Open a new video stream.
 * @ingroup streaming
 *
 * @param devh UVC device
 * @param ctrl Control block, processed using {uvc_probe_stream_ctrl} or
 *             {uvc_get_stream_ctrl_format_size}
 */
uvc_error_t uvc_stream_open_ctrl(uvc_device_handle_t *devh, uvc_stream_handle_t**ppStreamHandleResult, uvc_stream_ctrl_t *ctrl)
    {
    return devh->openStreamControl(ctrl, ppStreamHandleResult);
    }

uvc_error_t uvc_device_handle::openStreamControl(uvc_stream_ctrl_t *ctrl, uvc_stream_handle_t**ppStreamHandleResult)
    {
    UVC_ENTER();
    *ppStreamHandleResult = NULL;

    uvc_error_t ret = UVC_SUCCESS;
    if (this->getStreamByInterface(ctrl->bInterfaceNumber) == NULL)
        {
        uvc_streaming_interface* stream_if = this->getStreamInterface(ctrl->bInterfaceNumber);
        if (stream_if)
            {
            uvc_stream_handle *strmh = new uvc_stream_handle(this, stream_if);
            if (strmh)
                {
                if (strmh->ctorOK())
                    {
                    ret = strmh->claimInterface();
                    if (!ret)
                        {
                        ret = uvc_commit_stream_ctrl(strmh, ctrl);
                        if (!ret)
                            {
                            *ppStreamHandleResult = strmh;
                            }
                        }
                    }
                else
                    ret = uvc_originate_err(UVC_ERROR_NO_MEM);

                if (!ret)
                    {
                    *ppStreamHandleResult = strmh;
                    }
                else
                    {
                    delete strmh;
                    strmh = NULL;
                    }
                }
            else
                {
                outOfMemory();
                ret = uvc_originate_err(UVC_ERROR_NO_MEM);
                }
            }
        else
            {
            ret = uvc_originate_err(UVC_ERROR_INVALID_PARAM);
            }
        }
    else
        {
        ret = uvc_originate_err(UVC_ERROR_BUSY); /* Stream is already opened */
        }

    UVC_EXIT(ret);
    return ret;
    }