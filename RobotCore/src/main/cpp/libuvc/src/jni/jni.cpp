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
// General native utilities
//
#include <jni.h>
#include <unistd.h>
#include <android/log.h>
#include <ftc.h>
#include <errno.h>
#include "libuvc.h"
#include "libuvc/libuvc_internal.h"
#include "ftc.h"
#include "JniEnv.h"

#undef TAG
static LPCSTR TAG = "UvcJni";

//--------------------------------------------------------------------------------------------------
// Misc globals
//--------------------------------------------------------------------------------------------------

Lock apiOneCallerLock;

//--------------------------------------------------------------------------------------------------
// JVM JNI management
//--------------------------------------------------------------------------------------------------

JavaVM* ScopedJniEnv::g_pJavaVM;
int     ScopedJniEnv::g_jniVersionNeeded;

/*
 * The VM calls JNI_OnLoad when the native library is loaded (for example, through System.loadLibrary). JNI_OnLoad
 * must return the JNI version needed by the native library. In order to use any of the new JNI functions, a
 * native library must export a JNI_OnLoad function that returns JNI_VERSION_1_2. If the native library does
 * not export a JNI_OnLoad function, the VM assumes that the library only requires JNI version JNI_VERSION_1_1.
 * If the VM does not recognize the version number returned by JNI_OnLoad, the VM will unload the library and
 * act as if the library was +never loaded.
 */
jint JNI_OnLoad(JavaVM* pJavaVM, void* reserved)
    {
    FTC_TRACE();
    return ScopedJniEnv::onJniLoad(pJavaVM, reserved);
    }

/** searches the superclass chain of the object for the method with the indicated name and signature */
jmethodID findMethod(JNIEnv *env, jobject object, LPCSTR methodName, LPCSTR args)
    {
    jclass clazz = env->GetObjectClass(object);
    while (clazz != nullptr)
        {
        jmethodID methodId = env->GetMethodID(clazz, methodName, args);
        if (methodId != nullptr)
            {
            return methodId;
            }
        clazz = env->GetSuperclass(clazz);
        }
    LOGE("unable to find method %s:%s", methodName, args);
    return nullptr;
    }

//--------------------------------------------------------------------------------------------------
// Time
//--------------------------------------------------------------------------------------------------

struct Clock
    {
    clockid_t monotonicId;

    Clock()
        {
        #if _POSIX_TIMERS > 0
            struct timespec ts;
            monotonicId = clock_gettime(CLOCK_MONOTONIC, &ts) == 0 ? CLOCK_MONOTONIC : CLOCK_REALTIME;
        #else
            monotonicId = CLOCK_MONOTONIC; // not actually used
        #endif
        }

    int gettime(clockid_t clockid, struct timespec* pts)
        {
        #if _POSIX_TIMERS > 0
            if (CLOCK_MONOTONIC==clockid) clockid = monotonicId;
            return clock_gettime(clockid, pts);
        #else
            struct timeval tv;
            int rc = gettimeofday(&tv, NULL);
            pts->tv_sec = tv.tv_sec;
            pts->tv_nsec = tv.tv_usec * 1000;
            return rc;
        #endif
        }

    static Clock theInstance;
    };

Clock Clock::theInstance = Clock();

int ftc_gettime(clockid_t clockid, timespec* pts)
    {
    return Clock::theInstance.gettime(clockid, pts);
    }

//--------------------------------------------------------------------------------------------------
// File management
//--------------------------------------------------------------------------------------------------

bool TempFile::create()
    {
    bool result = false;
    this->pFile = NULL;

    size_t cbMax = sizeof(szPath); /*LOGD("cbMax=%d", cbMax);*/
    memset(szPath, 0, cbMax);
    snprintf(szPath, cbMax, "%s/ftcuvcTemp-XXXXXX", szTempFolder);

    int fd = mkstemp(this->szPath);
    if (fd > 0)
        {
        this->pFile = fdopen(dup(fd), "r+");
        ::close(fd);
        result = true;
        LOGD("made temp file: %s", this->szPath);
        }
    else
        LOGE("unable to make temp file: %s", this->szPath);

    return result;
    }

void TempFile::close(){
    if (this->pFile)
        {
        fclose(this->pFile);
        this->pFile = NULL;

        LOGD("unlinking: %s", &this->szPath[0]);
        unlink(&this->szPath[0]);
        }
    }

jstring TempFile::getJavaString(JNIEnv *env)
    {
    jstring result = NULL;

    long cbAlloc = ftell(this->pFile); // 'long' is correct per ftell
    if (cbAlloc > 0)
        {
        LPSTR buffer = (LPSTR)malloc((size_t)(cbAlloc + 1));
        if (buffer)
            {
            if (0 == fseek(this->pFile, 0, SEEK_SET))
                {
                size_t cToRead = (size_t)cbAlloc;
                size_t cRead = fread(buffer, 1, cToRead, this->pFile);
                if (cToRead == cRead)
                    {
                    buffer[cRead++] = 0; // nul terminate string
                    result = env->NewStringUTF(buffer);
                    }
                else
                    LOGE("fread: toRead=%zd read=%zd error=%d", cToRead, cRead, errno);
                }
            else
                LOGE("fseek: errno=%d", errno);

            free(buffer);
            }
        else
            outOfMemory();
        }
    else
        LOGE("ftell: errno=%d", errno);

    return result;
    }