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
/*
// format_convert.rs
//
// This gets compiled both in Java and in native code. That helps facilitate testing and
// makes source more visible in the IDE while at the same time offering maximum performance (in
// particular, allowing us to avoid copying frame buffers into Java and back down just to be able
// to hand off to the engine!). The cost (for the unused Java part) is a small class and a few
// hundred bytes in a resource. 'Seems worth it.
//
// Future thoughts:
//      * drive this with RS_FOR_EACH_STRATEGY_TILE_SMALL?
//      * make this a functionscript (.fs) file instead of a renderscript (.rs) for perf reasons?
//
// https://developer.android.com/guide/topics/renderscript/index.html
// https://developer.android.com/guide/topics/renderscript/compute.html
// https://developer.android.com/guide/topics/renderscript/reference/index.html
//
// https://developer.android.com/reference/android/renderscript/RenderScript.html
// http://mobilepearls.com/labs/native-android-api/ndk/docs/renderscript/index.html
// https://www.amazon.com/RenderScript-parallel-computing-Android-easy-ebook/dp/B01HOWDJ5O/ref=sr_1_1?s=digital-text&ie=UTF8&qid=1498930414&sr=1-1&keywords=renderscript
*/
#pragma version(1)
#pragma rs java_package_name(org.firstinspires.ftc.robotcore.internal.camera);

//--------------------------------------------------------------------------------------------------
// Globals
//--------------------------------------------------------------------------------------------------

rs_allocation inputAllocation;
int           outputWidth;
int           outputHeight;

//--------------------------------------------------------------------------------------------------
// Utility
//--------------------------------------------------------------------------------------------------

static inline short clip(short value)
    {
    return clamp(value, (short)0, (short)255);
    }

//--------------------------------------------------------------------------------------------------
// YUV processing
//--------------------------------------------------------------------------------------------------

static inline uchar4 rgbaFromYUV(uchar y, uchar u, uchar v)
    {
    uchar4 out = rsYuvToRGBA_uchar4(y, u, v);
    out.a = 255;
    return out;

    /* See also from: https://msdn.microsoft.com/en-us/library/ms893078.aspx,
       The built-in rsYuvToRGBA_uchar4() seems to implement this algorithm, so we use same.

    short c = (short)y - 16;
    short d = (short)u - 128;
    short e = (short)v - 128;

    uchar4 out;
    out.r = clip(( 298 * c           + 409 * e + 128) >> 8);
    out.g = clip(( 298 * c - 100 * d - 208 * e + 128) >> 8);
    out.b = clip(( 298 * d + 516 * d           + 128) >> 8);
    out.a = 255;

    return out;*/
    }

static inline short yuv420to422(short ci, short ciPlus1, short ciMinus1, short ciPlus2)
    {
    // https://msdn.microsoft.com/en-us/library/windows/desktop/dd206750(v=vs.85).aspx
    return clip((9 * (ci + ciPlus1) - (ciMinus1 + ciPlus2) + 8) >> 4);
    }

static inline uchar uOf(uchar4 yuv)
    {
    return yuv.s1;
    }
static inline uchar vOf(uchar4 yuv)
    {
    return yuv.s3;
    }


uchar4 RS_KERNEL yuv2_to_argb8888(uint32_t x, uint32_t y) // x & y are in the *output* allocation, which is the *logical* size
    {
    /*
     * We're following https://msdn.microsoft.com/en-us/library/windows/desktop/dd206750(v=vs.85).aspx,
     * from the section entitled "Converting 4:2:2 YUV to 4:4:4 YUV", which in turn draws heavily
     * from the section "Converting 4:2:0 YUV to 4:2:2 YUV".
     *
     * What we're doing is horizontal upconversion by a factor of two, using interpolation.
     *
     * "Let each horizontal line of input chroma samples be an array Cin[] that ranges from 0 to N-1.
     * The corrsponding horizontal line on the output image will be an array Cout[] that ranges from
     * j: 0 to 2N-1."
     *
     * 2N==outputWidth
     */

    uint32_t j = x;
    uint32_t i = j >> 1;

    uchar4 ci = rsGetElementAt_uchar4(inputAllocation, i, y);

    if ((j&1)==0)
        {
        // Even case is easy
        return rgbaFromYUV(ci.s0, uOf(ci), vOf(ci));
        }
    else
        {
        // Odd case needs interpolation.
        int iPlus1;
        int iMinus1;
        int iPlus2;

        if (j < 3)  // first three
            {
            iPlus1  = i+1;
            iMinus1 = 0;
            iPlus2  = i+2;
            }
        else if (j > (outputWidth-4)) // last three
            {
            int n = (outputWidth>>2);
            iPlus1 = iPlus2 = n-1;
            iMinus1 = i-1;
            }
        else
            {
            iPlus1  = i+1;
            iMinus1 = i-1;
            iPlus2  = i+2;
            }

        uchar4 ciMinus1 = rsGetElementAt_uchar4(inputAllocation, iMinus1, y);
        uchar4 ciPlus1  = rsGetElementAt_uchar4(inputAllocation, iPlus1, y);
        uchar4 ciPlus2  = rsGetElementAt_uchar4(inputAllocation, iPlus2, y);

        uchar u = (uchar)yuv420to422(uOf(ci), uOf(ciMinus1), uOf(ciPlus1), uOf(ciPlus2));
        uchar v = (uchar)yuv420to422(vOf(ci), vOf(ciMinus1), vOf(ciPlus1), vOf(ciPlus2));

        return rgbaFromYUV(ci.s2, u, v);
        }
    }