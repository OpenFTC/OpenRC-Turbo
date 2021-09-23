# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)


# These flags are used for both of the libraries below
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DSTANDALONE_DEMO_LIB \
                    -std=c++11 -fno-exceptions -fno-rtti -O2 -Wno-narrowing \
                    -fPIE")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} \
                              -Wl,--allow-multiple-definition \
                              -Wl,--whole-archive -fPIE -v")

# Create the image utils library
file(GLOB_RECURSE image_utils_sources src/main/cpp/image_utils/*.*)
add_library(image_utils
            SHARED
            ${image_utils_sources})
target_link_libraries(image_utils
        android
        log
        jnigraphics
        m
        atomic
        z
        )

# Create the object tracking library
file(GLOB_RECURSE object_tracking_sources src/main/cpp/object_tracking/*.*)
add_library(object_tracking
            SHARED
            ${object_tracking_sources})
target_link_libraries(object_tracking
        android
        log
        jnigraphics
        m
        atomic
        z
)
