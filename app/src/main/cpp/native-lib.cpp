#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <GLES2/gl2.h>

#define TAG "NATIVE_LIB"

extern "C" JNIEXPORT jint JNICALL
Java_com_example_realtime_MainActivity_nativeProcessFrame(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray inputY,
        jbyteArray inputU,
        jbyteArray inputV,
        jint yRowStride,
        jint uvRowStride,
        jint uvPixelStride,
        jint width,
        jint height,
        jint textureId) {

    // --- 1. Get YUV Plane Pointers ---
    // Get the raw byte arrays from Java
    jbyte *yPtr = env->GetByteArrayElements(inputY, nullptr);

    // --- 2. OpenCV Processing on Y-Plane (Luminance) ---
    // Create an OpenCV Mat wrapper around the Y-plane data
    cv::Mat yMat(height, width, CV_8UC1, yPtr, yRowStride);

    cv::Mat edgesMat;
    // Canny Edge Detection
    cv::Canny(yMat, edgesMat, 50, 150);

    // Convert single-channel edge image back to 4-channel RGBA for OpenGL
    cv::Mat rgbaMat;
    cv::cvtColor(edgesMat, rgbaMat, cv::COLOR_GRAY2RGBA);

    // --- 3. Upload Processed Mat to OpenGL Texture ---
    if (textureId != 0) {
        // Bind the target texture (created in MyGLRenderer)
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Upload the processed RGBA image data to the texture
        // This is the key step for real-time update
        glTexImage2D(GL_TEXTURE_2D,
                     0,
                     GL_RGBA,
                     width,
                     height,
                     0,
                     GL_RGBA,
                     GL_UNSIGNED_BYTE,
                     rgbaMat.data);
    } else {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Invalid OpenGL Texture ID: %d", textureId);
    }

    // --- 4. Release Resources ---
    // Release the Y array elements (JNI_ABORT means changes are not copied back)
    env->ReleaseByteArrayElements(inputY, yPtr, JNI_ABORT);
    // U and V planes are not used here, but must be released if elements were obtained
    // (Here we only obtained Y for simplicity, but if you got all three, release all three)
    // env->ReleaseByteArrayElements(inputU, uPtr, JNI_ABORT);
    // env->ReleaseByteArrayElements(inputV, vPtr, JNI_ABORT);

    return 0;
}