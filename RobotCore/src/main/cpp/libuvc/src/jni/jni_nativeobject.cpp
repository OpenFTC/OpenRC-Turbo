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
// NativeObject methods
//
#include <jni.h>
#include <unistd.h>
#include <malloc.h>
#include <libuvc.h>
#include <libuvc/libuvc_internal.h>
#include "ftc.h"
#include "../../../include/ftc.h"

#undef TAG
static LPCSTR TAG = "UvcNativeObject";

JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_NativeObject_nativeAllocMemory(JNIEnv *env, jclass type, jlong cbAlloc)
    {
    FTC_TRACE_VERY_VERBOSE();
    if (cbAlloc > 0)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        byte_t* pbResult = typedMallocZero<byte_t>((size_t)cbAlloc);
        if (pbResult)
            {
            }
        else
            outOfMemory();
        //
        return (JNI_NATIVE_POINTER)pbResult;
        }
    else
        return JNI_NATIVE_POINTER_NULL;
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_NativeObject_nativeFreeMemory(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer)
    {
    FTC_TRACE_VERY_VERBOSE();
    void* pv = (void*)pointer;
    if (pv)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        free(pv);
        }
    }

JNIEXPORT jbyteArray JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_NativeObject_nativeGetBytes(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jint ib, jint cb)
    {
    FTC_TRACE_VERY_VERBOSE();
    jbyteArray result = NULL;
    if (pointer)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        byte_t* pbFrom = ((byte_t*)pointer) + ib;
        result = env->NewByteArray(cb);
        if (result)
            {
            jboolean isCopy;
            jbyte* pbTo = env->GetByteArrayElements(result, &isCopy);
            if (pbTo)
                {
                memcpy(pbTo, pbFrom, cb);
                env->ReleaseByteArrayElements(result, pbTo, 0);
                }
            else
                outOfMemory();
            }
        }
    else
        invalidArgs();
    return result;
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_NativeObject_nativeSetBytes(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jint ib, jbyteArray rgb)
    {
    FTC_TRACE_VERY_VERBOSE();
    if (pointer && rgb)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        byte_t* pbTo = ((byte_t*)pointer) + ib;
        size_t cb = (size_t)env->GetArrayLength(rgb);

        jboolean isCopy;
        jbyte* pbFrom = env->GetByteArrayElements(rgb, &isCopy);
        if (pbFrom)
            {
            memcpy(pbTo, pbFrom, cb);
            env->ReleaseByteArrayElements(rgb, pbFrom, 0);
            }
        else
            outOfMemory();
        }
    else
        invalidArgs();
    }


JNIEXPORT jstring JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_NativeObject_nativeGetString(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jint ib)
    {
    FTC_TRACE_VERY_VERBOSE();
    jstring result = NULL;
    if (pointer)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        LPCSTR sz = (LPCSTR)(((byte_t*)pointer) + ib);
        result = env->NewStringUTF(sz);
        }
    else
        invalidArgs();
    return result;
    }

/**
 * Returns all the members of a (non-circular) doubly-linked list
 */
JNIEXPORT jlongArray JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_NativeObject_nativeGetLinkedList(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jint ib)
    {
    FTC_TRACE_VERY_VERBOSE();
    jlongArray result = 0;
    if (pointer)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();

        ABSTRACT_DL_NONCIRCULAR_LIST_ENTRY* pListHead = readPointerField<ABSTRACT_DL_NONCIRCULAR_LIST_ENTRY>(pointer, ib);
        ABSTRACT_DL_NONCIRCULAR_LIST_ENTRY* pListMember;

        int count = 0;
        DL_FOREACH(pListHead, pListMember)
            {
            count++;
            }

        result = env->NewLongArray(count);
        if (result)
            {
            jboolean isCopy;
            jlong* pArrayData = env->GetLongArrayElements(result, &isCopy);
            int i = 0;
            DL_FOREACH(pListHead, pListMember)
                {
                pArrayData[i] = (jlong)pListMember;
                i++;
                }
            env->ReleaseLongArrayElements(result, pArrayData, 0);
            }
        else
            outOfMemory();
        }
    else
        invalidArgs();
    return result;
    }

static jlong fetch(void* pv, int cbStride)
    {
    switch (cbStride)
        {
        case 1: return (jlong)(*(byte_t*)pv);
        case 2: return (jlong)(*(short*)pv);
        case 4: return (jlong)(*(int*)pv);
        case 8: return (jlong)(*(jlong*)pv);
        default: return 0;
        }
    }

JNIEXPORT jlongArray JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_NativeObject_nativeGetNullTerminatedList(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jint ib, jint cbStride)
    {
    FTC_TRACE_VERY_VERBOSE();
    jlongArray result = 0;
    if (pointer)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        byte_t* pbStart = (byte_t*)pointer + ib;

        int count = 0;
        for (byte_t* pb = pbStart; fetch(pb, cbStride) != 0; pb += cbStride)
            {
            count++;
            }

        result = env->NewLongArray(count);
        if (result)
            {
            jboolean isCopy;
            jlong* pArrayData = env->GetLongArrayElements(result, &isCopy);
            int i = 0;
            for (byte_t* pb = pbStart; fetch(pb, cbStride) != 0; pb += cbStride)
                {
                pArrayData[i] = fetch(pb, cbStride);
                i++;
                }
            env->ReleaseLongArrayElements(result, pArrayData, 0);
            }
        else
            outOfMemory();
        }
    else
        invalidArgs();
    return result;
    }

JNIEXPORT JNI_NATIVE_POINTER JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_NativeObject_nativeGetPointer(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jint ib)
    {
    FTC_TRACE_VERY_VERBOSE();
    JNI_NATIVE_POINTER result = JNI_NATIVE_POINTER_NULL;
    if (pointer)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        void* pv = readPointerField<void>(pointer, ib);
        result = (JNI_NATIVE_POINTER)(pv);
        }
    else
        invalidArgs();
    return result;
    }

JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcore_internal_system_NativeObject_nativeSetPointer(JNIEnv *env, jclass type, JNI_NATIVE_POINTER pointer, jint ib, JNI_NATIVE_POINTER pValue)
    {
    FTC_TRACE_VERY_VERBOSE();
    if (pointer)
        {
        NATIVE_API_MANY_CALLERS_VERBOSE();
        writePointerField(pointer, ib, (void*)pValue);
        }
    else
        invalidArgs();
    }




