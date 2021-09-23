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
// Utilities related to the system clock
//

#include <libuvc.h>
#include "ftc.h"

#undef TAG
static LPCSTR TAG = "JniTime";

/**
 * Attempts to set the current system wall clock time.
 *
 * Note that w/oa modified Android kernel, this will be unsuccessful, due to a check in
 * kernel/security/commoncap.c. On the Control Hub, that has been disabled, and so this should
 * succeed.
 *
 * UPDATE: on recent Android versions, calling this actually crashes the app instead of
 * just failing silently: https://source.android.com/devices/tech/debug/native-crash#seccomp
 *
 *      The seccomp system (specifically seccomp-bpf) restricts access to system calls.
 *      For more information about seccomp for platform developers, see the blog post
 *      Seccomp filter in Android O. A thread that calls a restricted system call will
 *      receive a SIGSYS signal with code SYS_SECCOMP.
 *
 * Return 0 on success, -errno on failure.
 */
JNIEXPORT jboolean JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_AppUtil_nativeSetCurrentTimeMillis(JNIEnv *env, jobject type, jlong millis)
    {
    uvc_error rc = UVC_SUCCESS;
    FTC_TRACE();
    NATIVE_API_MANY_CALLERS_VERBOSE();
    if (millis > 0) // At least make the the *math* work :-)
        {
        /* The Android implementation of System.currentTimeMillis() is thus:
            // from build\libcore\luni\src\main\native\java_lang_System.cpp
            static jlong System_currentTimeMillis(JNIEnv*, jclass) {
                timeval now;
                gettimeofday(&now, NULL);
                jlong when = now.tv_sec * 1000LL + now.tv_usec / 1000;
                return when;
            }
           We just attempt to do the inverse here.
         */
        timeval now;
        now.tv_sec = __kernel_time_t(millis / 1000LL);
        now.tv_usec = __kernel_suseconds_t((millis - 1000LL * now.tv_sec) * 1000);
        if (!settimeofday(&now, nullptr))
            {
            LOGI("settimeofday() succeeded");
            }
        else
            {
            LOGE("settimeofday() failed: errno=%d", errno);
            rc = UVC_ERROR_OTHER;
            }
        }
    else
        rc = uvcInvalidArgs();
    return tojboolean(rc);
    }
