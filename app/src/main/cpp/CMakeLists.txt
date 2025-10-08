cmake_minimum_required(VERSION 3.10.2)
project("realtime")

# Set C++ Standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED TRUE)

# NDK logging library
find_library(log-lib log)

# Add the main native library
add_library(
        realtime
        SHARED
        native-lib.cpp
)

# Link all required libraries: Log, EGL, GLESv2 (for OpenGL), and OpenCV
target_link_libraries(
        realtime
        log-lib
        EGL                 # For EGL surface management (optional, but good practice)
        GLESv2              # For OpenGL ES 2.0 rendering
        # Link the OpenCV shared library using the path passed from Gradle
        "${OPENCV_LIB_DIR}/libopencv_java4.so"
)