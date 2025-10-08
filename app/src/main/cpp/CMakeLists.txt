# CMakeLists.txt (The Final Correct Version)

cmake_minimum_required(VERSION 3.10.2)
project("realtime")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED TRUE)

# 🚨 FIX 1: Correct Header Path (Aapne ab absolute path diya hai, Good!)
include_directories(
        C:/Users/Asus/Downloads/opencv-4.12.0-android-sdk/OpenCV-android-sdk/sdk/native/jni/include
)

# ----------------------------------------------------------------------
# FIX 2: Dynamic Linking Logic (Simplified and Corrected Path)
# JNI_LIBS_DIR is relative to the CMakeLists.txt location (app/src/main/cpp)
# Correct path should be: app/src/main/cpp/../../../jniLibs -> app/src/main/jniLibs
# Use CMAKE_SOURCE_DIR/../jniLibs for simpler relative path if jniLibs is inside app/src/main
set(JNI_LIBS_DIR ${CMAKE_SOURCE_DIR}/../jniLibs)

# Define the OpenCV library as an IMPORTED library (dynamic path)
add_library(opencv_java SHARED IMPORTED)
set_target_properties(opencv_java PROPERTIES
        # Use the correct NDK variable: ANDROID_ABI
        IMPORTED_LOCATION ${JNI_LIBS_DIR}/${ANDROID_ABI}/libopencv_java4.so
)
# ----------------------------------------------------------------------

# NDK logging library (log_lib variable defined)
find_library(log_lib log)

# Add the main native library
add_library(
        realtime
        SHARED
        native-lib.cpp
)

# FIX 3 & 4: Link only once and correctly
target_link_libraries(
        realtime
        opencv_java         # Links the IMPORTED OpenCV library
        ${log_lib}          # Links the Android log library (only once)
        EGL
        GLESv2
)