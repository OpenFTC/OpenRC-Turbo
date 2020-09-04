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
// UvcStreamHandle methods
//
#include <jni.h>
#include <unistd.h>
#include <libuvc.h>
#include <errno.h>
#include <ftc.h>
#include <libuvc/libuvc_internal.h>
#include "ftc.h"
#include "JniEnv.h"

#undef TAG
static LPCSTR TAG = "UvcStreamHandle";

class StreamingCallbackState : public RefCounted
    {
protected:
    jobject     frameCallback;
    jmethodID   methodID;
    bool        initialized;

public:
    StreamingCallbackState(jobject frameCallback)
        {
        ScopedJniEnv env;
        this->frameCallback = env.getPointer()->NewGlobalRef(frameCallback);
        this->methodID      = NULL;
        this->initialized   = false;
        }

    bool ctorOK()
        {
        return frameCallback != NULL;
        }

    void doCallback(uvc_frame* pFrame)
        {
        ScopedJniEnv env;
        initialize(env.getPointer());
        if (methodID != NULL)
            {
            env.getPointer()->CallVoidMethod(frameCallback, methodID, (JNI_NATIVE_POINTER)pFrame);
            }
        }

protected:
    void initialize(JNIEnv* env)
        {
        // The value from GetMethodID() is, apparently, only useful on the current thread. We think.
        if (!initialized)
            {
            jclass frameCallbackClass = env->GetObjectClass(frameCallback);
            // http://journals.ecs.soton.ac.uk/java/tutorial/native1.1/implementing/method.html
            methodID = env->GetMethodID(frameCallbackClass, "onFrame", "(J)V");
            if (NULL == methodID)
                {
                LOGE("methodID failed to initialize");
                }
            initialized = true;
            }
        }
    virtual ~StreamingCallbackState()
        {
        if (frameCallback)
            {
            ScopedJniEnv env;
            env.getPointer()->DeleteGlobalRef(frameCallback);
            }
        }
    };

void frameCallbackFunction(struct uvc_frame *pFrame, void *user_ptr)
    {
    StreamingCallbackState* pCallbackData = (StreamingCallbackState*)user_ptr;
    pCallbackData->doCallback(pFrame);
    }

JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcFrameCallback_nativeAllocCallbackState(JNIEnv *env, jclass type, jobject uvcFrameCallbackObject)
    {
    JNI_NATIVE_POINTER result = JNI_NATIVE_POINTER_NULL;
    StreamingCallbackState* pStreamingCallbackState = new StreamingCallbackState(uvcFrameCallbackObject);
    if (pStreamingCallbackState && pStreamingCallbackState->ctorOK())
        {
        result = (JNI_NATIVE_POINTER)pStreamingCallbackState;
        pStreamingCallbackState->addRef();
        }
    releaseRef(pStreamingCallbackState);
    return result;
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcFrameCallback_nativeReleaseCallbackState(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    StreamingCallbackState* pStreamingCallbackState = (StreamingCallbackState*)pointer;
    if (pointer)
        {
        NATIVE_API_ONE_CALLER();
        releaseRef(pStreamingCallbackState);
        }
    else
        invalidArgs();
    }

JNIEXPORT jboolean JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcStreamHandle_nativeStartStreaming(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, JNI_NATIVE_POINTER callbackPointer)
    {
    jboolean result = JNI_FALSE;
    uvc_stream_handle* pStreamHandle = (uvc_stream_handle*)pointer;
    StreamingCallbackState* pCallbackData = (StreamingCallbackState*)callbackPointer;
    if (pStreamHandle && pCallbackData)
        {
        NATIVE_API_ONE_CALLER();
        uvc_error rc = uvc_stream_start(pStreamHandle, frameCallbackFunction, pCallbackData, 0);
        if (!rc)
            {
            LOGD("successfully started streaming");
            result = JNI_TRUE;
            }
        else
            {
            LOGE("failed to start streaming: uvc_stream_start()=%d(%s)", rc, uvcErrorName(rc));
            }
        }
    else
        {
        invalidArgs();
        }
    return result;
    }

JNIEXPORT jboolean JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcStreamHandle_nativeIsStreaming(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    jboolean result = 0;
    uvc_stream_handle* pStreamHandle = (uvc_stream_handle*)pointer;
    if (pStreamHandle)
        {
        NATIVE_API_ONE_CALLER();
        result = (jboolean)(pStreamHandle->isRunning ? JNI_TRUE : JNI_FALSE);
        }
    else
        invalidArgs();

    return result;
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcStreamHandle_nativeStopStreaming(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    UVC_ENTER();
    jint result = 0;
    uvc_stream_handle* pStreamHandle = (uvc_stream_handle*)pointer;
    if (pStreamHandle)
        {
        NATIVE_API_ONE_CALLER();
        result = uvc_stream_stop(pStreamHandle); // idempotent
        }
    else
        invalidArgs();
    UVC_EXIT_VOID();
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_camera_libuvc_nativeobject_UvcStreamHandle_nativeCloseStreamHandle(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    UVC_ENTER();
    uvc_stream_handle* pStreamHandle = (uvc_stream_handle*)pointer;
    if (pStreamHandle)
        {
        NATIVE_API_ONE_CALLER();
        uvc_stream_close(pStreamHandle);
        }
    else
        invalidArgs();
    UVC_EXIT_VOID();
    }
