/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.

@file
    Vuforia_Android.h

@brief
    Header file for global Vuforia methods that are specific to Android
===============================================================================*/

#ifndef _VUFORIA_VUFORIA_ANDROID_H_
#define _VUFORIA_VUFORIA_ANDROID_H_

// Include files
#include <Vuforia/System.h>
#include <jni.h>

namespace Vuforia
{

/// Sets Vuforia initialization parameters
/**
 * <b>Android:</b> Called to set the Vuforia initialization parameters prior to calling Vuforia::init().
 * Refer to the enumeration Vuforia::INIT_FLAGS for applicable flags.
 */
void VUFORIA_API setInitParameters(jobject activity, int flags, const char* licenseKey);

} // namespace Vuforia

#endif //_VUFORIA_VUFORIA_ANDROID_H_
