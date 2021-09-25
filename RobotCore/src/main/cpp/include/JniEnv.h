/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.

Adapted by Robert Atkinson, Copyright (c) 2018
===============================================================================*/

#ifndef __SCOPEDJNIENV_H__
#define __SCOPEDJNIENV_H__

#include <jni.h>

// Wrapper that gets the JNIEnv pointer from JVM and attaches it to the current
// thread if necessary. In case the thread was attached, it will be released
// in the destructor.
class ScopedJniEnv
    {
    //----------------------------------------------------------------------------------------------
    // Global state
    //----------------------------------------------------------------------------------------------
private:
    static JavaVM* g_pJavaVM;
    static int     g_jniVersionNeeded;

public:
    static int onJniLoad(JavaVM* pJavaVM, void* reserved)
        {
        FTC_TRACE();
        g_pJavaVM = pJavaVM;
        g_jniVersionNeeded = JNI_VERSION_1_6;
        return g_jniVersionNeeded;
        }

    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------
private:
    JavaVM* _pJavaVM = nullptr;
    int     _jniVersion = 0;
    bool    _threadWasAttached = false;
    JNIEnv* _env = nullptr;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------
public:

    ScopedJniEnv()
        {
        _pJavaVM = g_pJavaVM;
        _jniVersion = g_jniVersionNeeded;
        if (_pJavaVM->GetEnv((void**)&_env, _jniVersion) == JNI_EDETACHED)
            {
            _pJavaVM->AttachCurrentThread(&_env, nullptr);
            _threadWasAttached = true;
            }
        if (_env == nullptr)
            {
            fatalError("JNI environment unexpectedly unavailable");
            }
        }

    ~ScopedJniEnv()
        {
        if (_threadWasAttached)
            {
            _pJavaVM->DetachCurrentThread();
            }
        }

    //----------------------------------------------------------------------------------------------
    // Access
    //----------------------------------------------------------------------------------------------

    JNIEnv* getPointer() const
        {
        return _env;
        }

    JNIEnv* operator->() const
        {
        return getPointer();
        }
    };

#endif
