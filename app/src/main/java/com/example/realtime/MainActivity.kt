package com.example.realtime

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.view.Surface

class MainActivity : AppCompatActivity() {

    private lateinit var glRenderer: MyGLRenderer
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var cameraExecutor: ExecutorService

    private val PREVIEW_WIDTH = 640
    private val PREVIEW_HEIGHT = 480

    private val CAMERA_PERMISSION_REQUEST_CODE = 200

    external fun nativeProcessFrame(
        inputY: ByteBuffer, inputU: ByteBuffer, inputV: ByteBuffer,
        yRowStride: Int, uvRowStride: Int, pixelStride: Int,
        width: Int, height: Int, textureId: Int
    ): Int

    companion object {
        private const val TAG = "MainActivity"

        init {
            System.loadLibrary("realtime")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glRenderer = MyGLRenderer()
        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(glRenderer)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }
        setContentView(glSurfaceView)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            startCameraX()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCameraX() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    // MainActivity.kt

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {


        val rotation = android.view.Surface.ROTATION_0

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(PREVIEW_WIDTH, PREVIEW_HEIGHT))
            // 🚀 FIX: Current display rotation set karein
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor, OpenCVAnalyzer(glRenderer, glSurfaceView, ::nativeProcessFrame))

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                this as LifecycleOwner, cameraSelector, imageAnalysis
            )
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
            Toast.makeText(this, "Camera setup failed: ${exc.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Analyzer Class
    private class OpenCVAnalyzer(
        private val glRenderer: MyGLRenderer,
        private val glSurfaceView: GLSurfaceView,
        private val nativeProcessFunc: (ByteBuffer, ByteBuffer, ByteBuffer, Int, Int, Int, Int, Int, Int) -> Int
    ) : ImageAnalysis.Analyzer {

        private var isProcessing = false

        @OptIn(androidx.camera.core.ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {

            if (imageProxy.format != ImageFormat.YUV_420_888) {
                imageProxy.close()
                return
            }

            if (isProcessing) {
                imageProxy.close()
                return
            }

            val image = imageProxy.image ?: run {
                imageProxy.close()
                return
            }

            val planes = image.planes
            if (planes.size < 3) {
                imageProxy.close()
                return
            }

            val textureId = glRenderer.getTextureId()
            if (textureId == 0) {
                imageProxy.close()
                return
            }

            isProcessing = true

            // 🚨 FIX IS HERE: imageProxy.close() is moved inside queueEvent
            glSurfaceView.queueEvent {

                val yPlane = planes[0]
                val uPlane = planes[1]
                val vPlane = planes[2]

                yPlane.buffer.rewind()
                uPlane.buffer.rewind()
                vPlane.buffer.rewind()

                nativeProcessFunc(
                    yPlane.buffer, uPlane.buffer, vPlane.buffer,
                    yPlane.rowStride, uPlane.rowStride, uPlane.pixelStride,
                    image.width, image.height, textureId
                )

                isProcessing = false

                glSurfaceView.requestRender()

                // 🚀 CRITICAL FIX: Close the ImageProxy ONLY after OpenGL/Native thread is done with the buffers.
                imageProxy.close()
            }

            // 💥 REMOVAL: Yeh line FATAL EXCEPTION de rahi thi. Hata do!
            // imageProxy.close()
        }
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }
}