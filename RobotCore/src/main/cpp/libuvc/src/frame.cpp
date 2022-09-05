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

uvc_frame* uvc_allocate_frame(uvc_context* pContext, size_t cbFrameAlloc, size_t cbFrameExpected)
    {
    uvc_frame* pFrame = new uvc_frame(pContext, cbFrameAlloc, cbFrameExpected);
    if (pFrame && !pFrame->ctorOK())
        {
        delete pFrame;
        pFrame = NULL;
        }
    return pFrame;
    }

void uvc_free_frame(uvc_frame *pFrame)
    {
    delete pFrame;
    }

uvc_error_t uvc_duplicate_frame(uvc_frame *in, uvc_frame *out)
    {
    return in->copyTo(out);
    }

static inline unsigned char sat(int i) {
    return (unsigned char)( i >= 255 ? 255 : (i < 0 ? 0 : i));
}

#define IYUYV2RGB_2(pyuv, prgb) { \
    int r = (22987 * ((pyuv)[3] - 128)) >> 14; \
    int g = (-5636 * ((pyuv)[1] - 128) - 11698 * ((pyuv)[3] - 128)) >> 14; \
    int b = (29049 * ((pyuv)[1] - 128)) >> 14; \
    (prgb)[0] = sat(*(pyuv) + r); \
    (prgb)[1] = sat(*(pyuv) + g); \
    (prgb)[2] = sat(*(pyuv) + b); \
    (prgb)[3] = sat((pyuv)[2] + r); \
    (prgb)[4] = sat((pyuv)[2] + g); \
    (prgb)[5] = sat((pyuv)[2] + b); \
    }

#define IYUYV2RGB_8(pyuv, prgb) IYUYV2RGB_4(pyuv, prgb); IYUYV2RGB_4(pyuv + 8, prgb + 12);
#define IYUYV2RGB_4(pyuv, prgb) IYUYV2RGB_2(pyuv, prgb); IYUYV2RGB_2(pyuv + 4, prgb + 6);

/** @brief Convert a frame from YUYV to RGB
 * @ingroup frame
 *
 * @param in YUYV frame
 * @param out RGB frame
 */
uvc_error_t uvc_yuyv2rgb(uvc_frame_t *in, uvc_frame_t *out) {
    if (in->frameFormat != UVC_FRAME_FORMAT_YUYV)
        return UVC_ERROR_INVALID_PARAM;

    if (out->ensureSize(in->width * in->height * 3) < 0)
        return UVC_ERROR_NO_MEM;

    out->width = in->width;
    out->height = in->height;
    out->frameFormat = UVC_FRAME_FORMAT_RGB;
    out->cbLineStride = in->width * 3;
    out->captureTime = in->captureTime;

    uint8_t *pyuv = in->pbData;
    uint8_t *prgb = out->pbData;
    uint8_t *prgb_end = prgb + out->cbData;

    while (prgb < prgb_end) {
        IYUYV2RGB_8(pyuv, prgb);

        prgb += 3 * 8;
        pyuv += 2 * 8;
    }

    return UVC_SUCCESS;
}