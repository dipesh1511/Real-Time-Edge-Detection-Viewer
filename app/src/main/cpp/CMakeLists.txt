# CMakeLists.txt (The Final Correct Version)

cmake_minimum_required(VERSION 3.10.2)
project("realtime")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED TRUE)

# --- 1. Include Headers ---
# OpenCV Headers ka path define karein
include_directories(
        C:/Users/Asus/Downloads/opencv-4.12.0-android-sdk/OpenCV-android-sdk/sdk/native/jni/include
)

# --- 2. Define OpenCV as an IMPORTED Library (CRITICAL FIX) ---
# JNI_LIBS_DIR: Path to where your libopencv_java4.so files are located.
# ${ANDROID_ABI} will be 'arm64-v8a' or 'x86_64'.
set(JNI_LIBS_DIR "${CMAKE_SOURCE_DIR}/../jniLibs")

# Define the OpenCV library as a SHARED (dynamic) IMPORTED library
add_library(opencv_java SHARED IMPORTED)

# Set the path to the .so file for the current ABI (This links the library)
set_target_properties(opencv_java PROPERTIES
        IMPORTED_LOCATION ${JNI_LIBS_DIR}/${ANDROID_ABI}/libopencv_java4.so
)

# --- 3. Add the Main Native Library ---
add_library(
        realtime
        SHARED
        native-lib.cpp
)

# --- 4. Linking Libraries (CRITICAL FIX) ---
target_link_libraries(
        realtime

        # 🚨 FIX 1: Sirf IMPORTED library ko link karein (fixes undefined symbol error)
        opencv_java

        # NDK system libraries
        android
        log

        # FIX 2: OpenGL ES 2.0 (for GLES2/gl2.h)
        GLESv2
)