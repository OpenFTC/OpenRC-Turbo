#
# Android.mk in RobotCore
#
# Useful tidbits
#   https://developer.android.com/studio/projects/add-native-code.html
#   https://developer.android.com/ndk/guides/cmake.html#variables
#   https://developer.android.com/ndk/guides/android_mk.html
#   http://android.mk/
#   C:\Android\410c\build\frameworks\rs\java\tests\HelloComputeNDK\libhellocomputendk

# An Android.mk file must begin with the definition of the LOCAL_PATH variable.
# It is used to locate source files in the development tree. Here, the macro function 'my-dir',
# provided by the build system, is used to return the path of the current directory
# (i.e. the directory containing the Android.mk file itself).

LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------------------------------------------

# Do what's necessary to find the Vuforia shared library. And tell the system where the
# headers for same are as well: the LOCAL_EXPORT_C_INCLUDES line will cause the indicated
# path to be added to the C/C++ include path.

include $(CLEAR_VARS)
LOCAL_MODULE := Vuforia-prebuilt
LOCAL_SRC_FILES = $(LOCAL_PATH)/libs/$(TARGET_ARCH_ABI)/libVuforia.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/src/main/cpp/include/Vuforia
include $(PREBUILT_SHARED_LIBRARY)

#---------------------------------------------------------------------------------------------------

# The CLEAR_VARS variable is provided by the build system and points to a special GNU Makefile
# that will clear many LOCAL_XXX variables for you (e.g. LOCAL_MODULE, LOCAL_SRC_FILES,
# LOCAL_STATIC_LIBRARIES, etc...), with the exception of LOCAL_PATH. This is needed because all
# build control files are parsed in a single GNU Make execution context where all variables are global.

include $(CLEAR_VARS)


# The LOCAL_MODULE variable must be defined to identify each module you describe in your Android.mk.
# The name must be *unique* and not contain any spaces. Note that the build system will automatically
# add proper prefix and suffix to the corresponding generated file. In other words, a shared library
# module named 'foo' will generate 'libfoo.so'.

LOCAL_MODULE := RobotCore


# Set OpenGL ES version-specific settings. In actual point of fact, we are not now ourselves 
# actually doing any *native* OpenGL code, but these would be the settings we'd need if we ever
# chose to do so (note we use 2.0 in Java). As they are harmless, we leave them here in place.

OPENGLES_LIB  := -lGLESv2
OPENGLES_DEF  := -DUSE_OPENGL_ES_2_0


# The list of additional linker flags to be used when building your module. This is useful to pass the 
# name of specific system libraries with the "-l" prefix. For example, the following will tell the linker 
# to generate a module that links to /system/lib/libz.so at load time
#
#	LOCAL_LDLIBS := -lz
#
# See docs/STABLE-APIS.html for the list of exposed system libraries you can linked against with this NDK release
# https://developer.android.com/ndk/guides/stable_apis.html
# https://android.googlesource.com/platform/tools/base/+/87cf2a044464d0a389f0c4b7f951703e74271d40/build-system/gradle/src/main/groovy/com/android/build/gradle/tasks/NdkCompile.groovy

LOCAL_LDLIBS := -ljnigraphics -llog $(OPENGLES_LIB)


# This variable stores the list of static libraries modules on which the current module depends.
# If the current module is a shared library or an executable, this variable will force these libraries
# to be linked into the resulting binary. If the current module is a static library, this variable
# simply indicates that other modules depending on the current one will also depend on the listed
# libraries.

LOCAL_STATIC_LIBRARIES :=


# We need at least C++11 features (the code from PTC Vuforia relies on same). We use C++14 for
# extra goodness. In both C and C++, we pass the OpenGL settings

LOCAL_CPPFLAGS := -std=c++14
LOCAL_CFLAGS := $(OPENGLES_DEF)


# This variable is the list of shared libraries modules on which this module depends at runtime. This information
# is necessary at link time, and to embed the corresponding information in the generated file

LOCAL_SHARED_LIBRARIES := Vuforia-prebuilt


# LOCAL_C_INCLUDES is a list of paths to add to the include search path when compiling all sources (C, C++
# and Assembly). This is *relative* to the *build root*, not necessarily the location of the .mk file.
# (though it is in this case since this Android.mk is in the root). Further, absolute paths seem to have
# problems too (though that's not entirely confirmed).

LOCAL_C_INCLUDES = \
             src/main/cpp/libusb							\
             src/main/cpp/libusb/android                    \
             src/main/cpp/libuvc/include                    \
             src/main/cpp/include                           \
             src/main/cpp/vuforia


# The LOCAL_SRC_FILES variables must contain a list of C and/or C++ source files that will be built 
# and assembled into a module. Note that you should not list header and included files here, because 
# the build system will compute dependencies automatically for you; just list the source files that 
# will be passed directly to a compiler, and you should be good.

LOCAL_SRC_FILES := \
             src/main/cpp/system/SerialPort.cpp \
             src/main/cpp/system/time.cpp \
             src/main/cpp/libuvc/src/ctrl.cpp   \
             src/main/cpp/libuvc/src/ctrl-gen.cpp   \
             src/main/cpp/libuvc/src/device.cpp   \
             src/main/cpp/libuvc/src/devicehandle.cpp   \
             src/main/cpp/libuvc/src/diag.cpp   \
             src/main/cpp/libuvc/src/frame.cpp   \
             src/main/cpp/libuvc/src/init.cpp   \
             src/main/cpp/libuvc/src/stream.cpp   \
             src/main/cpp/libuvc/src/streamhandle.cpp   \
             src/main/cpp/libuvc/src/misc.cpp   \
             src/main/cpp/libuvc/src/jni/jni.cpp   \
             src/main/cpp/libuvc/src/jni/jni_bmpwriter.cpp   \
             src/main/cpp/libuvc/src/jni/jni_libusb_device.cpp   \
             src/main/cpp/libuvc/src/jni/jni_nativeobject.cpp   \
             src/main/cpp/libuvc/src/jni/jni_context.cpp   \
             src/main/cpp/libuvc/src/jni/jni_device.cpp   \
             src/main/cpp/libuvc/src/jni/jni_devicehandle.cpp   \
             src/main/cpp/libuvc/src/jni/jni_devicedescriptor.cpp   \
             src/main/cpp/libuvc/src/jni/jni_deviceinfo.cpp   \
             src/main/cpp/libuvc/src/jni/jni_formatdesc.cpp   \
             src/main/cpp/libuvc/src/jni/jni_framedesc.cpp   \
             src/main/cpp/libuvc/src/jni/jni_streamcontrol.cpp   \
             src/main/cpp/libuvc/src/jni/jni_streamhandle.cpp   \
             src/main/cpp/libuvc/src/jni/jni_streaminginterface.cpp   \
             src/main/cpp/libuvc/src/jni/jni_frame.cpp   \
             src/main/cpp/libusb/core.c  \
             src/main/cpp/libusb/descriptor.c  \
             src/main/cpp/libusb/hotplug.c  \
             src/main/cpp/libusb/io.c  \
             src/main/cpp/libusb/sync.c  \
             src/main/cpp/libusb/strerror.c  \
             src/main/cpp/libusb/os/linux_usbfs.c  \
             src/main/cpp/libusb/os/poll_posix.c  \
             src/main/cpp/libusb/os/threads_posix.c  \
             src/main/cpp/libusb/os/linux_netlink.c \
             src/main/cpp/vuforia/NativeVuforiaWebcam.cpp



# The BUILD_SHARED_LIBRARY is a variable provided by the build system that points to a GNU Makefile 
# script that is in charge of collecting all the information you defined in LOCAL_XXX variables since 
# the latest 'include $(CLEAR_VARS)' and determine what to build, and how to do it exactly. There is 
# also BUILD_STATIC_LIBRARY to generate a static library.

include $(BUILD_SHARED_LIBRARY)


