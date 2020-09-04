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
// BmpWriter methods
//
#include <jni.h>
#include <unistd.h>
#include <libuvc.h>
#include "ftc.h"

#undef TAG
static LPCSTR TAG = "UvcBmpWriter";

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_BmpFileWriter_nativeCopyPixelsRGBA(JNIEnv *env, jclass type,
            const jint width, const jint height, const jint cbPadding,
            const jobject bitmapObject,
            const jlong rgbDest, const jint ibDest)
    {
    FTC_TRACE_VERBOSE();
    if (bitmapObject && rgbDest && width >= 0 && height >= 0 && cbPadding >= 0 && ibDest >= 0)
        {
        JavaBitmapAccess bitmapAccess(env, bitmapObject);
        const ColorInt* pixels = (ColorInt*)bitmapAccess.pbData;
        if (pixels)
            {
            ColorInt* pclrTo = (ColorInt*)(pbyte_t(rgbDest) + ibDest);

            /* "Normally pixels are stored "upside-down" with respect to normal image raster scan order,
             *  starting in the lower left corner, going from left to right, and then row by row from
             *  the bottom to the top of the image." */
            if (cbPadding == 0)
                {
                for (int iRow = height-1; iRow >= 0; iRow--)
                    {
                    const ColorInt* pclrFrom = &pixels[iRow*width];
                    for (int iCol = 0; iCol < width; iCol++)
                        {
                        *pclrTo++ = Color::toRGBA(*pclrFrom++);
                        }
                    }
                }
            else
                {
                for (int iRow = height-1; iRow >= 0; iRow--)
                    {
                    const ColorInt* pColorFrom = &pixels[iRow*width];
                    for (int iCol = 0; iCol < width; iCol++)
                        {
                        *pclrTo++ = Color::toRGBA(*pColorFrom++);
                        }

                    byte_t* pbTo = (byte_t*)pclrTo;
                    for (int i = 0; i < cbPadding; i++)
                        {
                        *(pbTo++) = (uint8_t)0;
                        }
                    pclrTo = (ColorInt*)pbTo;
                    }
                }
            }
        }
    else
        invalidArgs();
    }



