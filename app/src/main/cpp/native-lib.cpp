#include <jni.h>
#include <android/log.h>
#include <GLES2/gl2.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/types.hpp>

#define TAG "NATIVE_LIB"

extern "C" JNIEXPORT jint JNICALL
Java_com_example_realtime_MainActivity_nativeProcessFrame(
        JNIEnv *env,
        jobject /* this */,
        jobject inputY,
        jobject inputU,
        jobject inputV,
        jint yRowStride,
        jint uvRowStride,
        jint uvPixelStride,
        jint width,
        jint height,
        jint textureId) {

    // --- 1. Get Y-Plane Pointer ---
    unsigned char *yPtr = (unsigned char *)env->GetDirectBufferAddress(inputY);

    if (!yPtr) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "ERROR: Y Plane buffer address is NULL!");
        return -1;
    }

    cv::Mat yMat(height, width, CV_8UC1, yPtr, yRowStride);

    cv::Mat edgesMat;
    cv::Mat rgbaMat;

    // 🚨 FIX 2: Canny Edge Detection wapas lagao, high thresholds ke saath.
    // Pehle Canny lagao
    cv::Canny(yMat, edgesMat, 50, 150); // High contrast edges capture honge

    // Edges (single channel) ko 4-channel RGBA mein convert karo
    cv::cvtColor(edgesMat, rgbaMat, cv::COLOR_GRAY2RGBA);

    // --- 3. Upload Processed Mat to OpenGL Texture ---
    if (textureId != 0) {
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Upload the processed RGBA image data to the texture
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

    return 0;
}