/*********************************************************************
* Software License Agreement (BSD License)
*
*  Copyright (C) 2010-2012 Ken Tossell
*  All rights reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions
*  are met:
*
*   * Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
*   * Redistributions in binary form must reproduce the above
*     copyright notice, this list of conditions and the following
*     disclaimer in the documentation and/or other materials provided
*     with the distribution.
*   * Neither the name of the author nor other contributors may be
*     used to endorse or promote products derived from this software
*     without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
*  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
*  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
*  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
*  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
*  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
*  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
*  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
*  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
*  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
*  POSSIBILITY OF SUCH DAMAGE.
*********************************************************************/

#include "libuvc.h"
#include "libuvc/libuvc_internal.h"

void usbEventThreadMain(uvc_context *pctx)
    {
    UVC_ENTER_VERBOSE();
    ThreadInterlock* pInterlock = pctx->pUsbThreadInterlock;  // capture locally
    pInterlock->addRef();
    pInterlock->signalThreadStart();
    //
    while (!pctx->stopEventThread)
        {
        timeval tv;
        tv.tv_sec = 365 * 86400;  // a year!
        tv.tv_usec = 0;
        libusb_handle_events_timeout_completed(pctx->pLibUsbContext, &tv, NULL);
        }
    //
    pInterlock->signalThreadCompletion();
    releaseRef(pInterlock);
    UVC_EXIT_VOID();
    }
void uvc_context::signalEventThread()
    {
    stopEventThread = true;
    libusb_interrupt_event_handler(pLibUsbContext);
    }

uvc_error uvc_context::init(/*nullable*/ LPCSTR szUsbfs, int buildVersionSDKInt, LPCSTR szTempFolder, bool forceJavaUsbEnumerationKitKat)
    {
    UVC_ENTER_VERBOSE();
    uvc_error rc = UVC_SUCCESS;
    if (szUsbfs)
        {
        this->szUsbfs = strdup(szUsbfs);
        if (!this->szUsbfs)
            {
            rc = outOfMemory();
            }
        }
    if (szTempFolder)
        {
        this->szTempFolder = strdup(szTempFolder);
        if (!this->szTempFolder)
            {
            rc = outOfMemory();
            }
        }
    if (!rc)
        {
        pUsbThreadInterlock = new ThreadInterlock();
        if (NULL == pUsbThreadInterlock)
            {
            rc = outOfMemory();
            }
        else
            {
            rc = uvc_error(libusb_init(&pLibUsbContext, this->szUsbfs, buildVersionSDKInt, forceJavaUsbEnumerationKitKat));
            UVC_DEBUG_VERBOSE("called libusb_init(%s, %d, %d): rc=%d(%s)", szUsbfs ? szUsbfs : "null", buildVersionSDKInt, forceJavaUsbEnumerationKitKat, rc, uvcErrorName(rc));
            if (!rc)
                {
                rc = uvcErrorFromErrno(createDetachedThread(usbEventThreadMain, this));
                if (!rc)
                    {
                    usbThreadStarted = true;
                    pUsbThreadInterlock->waitForThreadStart();  // to ensure they've grabbed the refcnt they need
                    }
                }
            else
                {
                pLibUsbContext = NULL;
                }
            }
        }
    UVC_RETURN(rc);
    }

uvc_context::~uvc_context()
    {
    uvc_error rc = UVC_SUCCESS;
    uvc_device_handle_t *devh;
    DL_FOREACH(openDevicesList, devh)
        {
        devh->stop();
        ::releaseRef(devh);
        }

    signalEventThread();
    if (usbThreadStarted)
        {
        pUsbThreadInterlock->waitForThreadCompletion(1000);
        }
    ::releaseRef(pUsbThreadInterlock);

    LOGD("calling libusb_exit()");
    libusb_exit(pLibUsbContext);
    free(const_cast<LPSTR>(szUsbfs));
    free(const_cast<LPSTR>(szTempFolder));
    }



/** @brief Initializes the UVC context
 * @ingroup init
 *
 * @note If you provide your own USB context, you must handle
 * libusb event processing using a function such as libusb_handle_events.
 *
 * @param[out] ppctx The location where the context reference should be stored.
 * @param[in]  pUsbContext Optional USB context to use
 * @return Error opening context or UVC_SUCCESS
 */
uvc_error_t uvc_init(uvc_context_t **ppctx, LPCSTR szUsbfs, int buildVersionSDKInt, LPCSTR szTempFolder, bool forceJavaUsbEnumerationKitKat)
    {
    uvc_error_t rc = UVC_SUCCESS;
    uvc_context *pctx = new uvc_context();
    if (pctx)
        {
        rc = pctx->init(szUsbfs, buildVersionSDKInt, szTempFolder, forceJavaUsbEnumerationKitKat);
        if (!!rc)
            {
            delete pctx;
            pctx = NULL;
            }
        }
    else
        rc = UVC_ERROR_NO_MEM;
    *ppctx = pctx;
    return rc;
    }

/**
 * @brief Closes the UVC context, shutting down any active cameras.
 * @ingroup init
 *
 * @note This function invalides any existing references to the context's
 * cameras.
 *
 * If no USB context was provided to #uvc_init, the UVC-specific USB
 * context will be destroyed.
 *
 * @param pctx UVC context to shut down
 */
void uvc_exit(uvc_context *pctx)
    {
    delete pctx;
    }

