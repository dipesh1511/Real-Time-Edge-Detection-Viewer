package com.example.realtime

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.opengl.GLSurfaceView
import android.os.*
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var imageReader: ImageReader
    private lateinit var cameraManager: CameraManager

    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null

    private val PREVIEW_WIDTH = 640
    private val PREVIEW_HEIGHT = 480

    private val CAMERA_PERMISSION_REQUEST_CODE = 200

    // Native method call: JNI (Java Native Interface)
    external fun nativeProcessFrame(
        inputY: ByteArray, inputU: ByteArray, inputV: ByteArray,
        yRowStride: Int, uvRowStride: Int, pixelStride: Int,
        width: Int, height: Int, textureId: Int): Int

    companion object {
        init {
            // Load the native library (name must match CMakeLists.txt)
            System.loadLibrary("realtime")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup OpenGL View
        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(MyGLRenderer(this@MainActivity))
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }
        setContentView(glSurfaceView)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // 2. Check Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission granted, proceed to open camera
            startCameraSetup()
        }
    }

    private fun startCameraSetup() {
        startBackgroundThread()
        openCamera()
    }

    // --- Background Thread Management ---
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        // ... (Cleanup logic as provided earlier)
    }

    // --- Camera Control ---
    private fun openCamera() {
        try {
            val cameraId = cameraManager.cameraIdList[0]

            // 1. Setup ImageReader for processing frames
            imageReader = ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT,
                ImageFormat.YUV_420_888, 2)
            imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)

            // 2. Open the Camera Device
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e("Camera", "Camera access error", e)
        } catch (e: SecurityException) {
            // Should be caught by permission check
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreviewSession()
        }
        // ... (onDisconnected and onError logic)
        override fun onDisconnected(camera: CameraDevice) { camera.close() }
        override fun onError(camera: CameraDevice, error: Int) { camera.close() }
    }

    private fun createCameraPreviewSession() {
        try {
            val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            val imageReaderSurface = imageReader.surface

            // NOTE: We don't need a TextureView Surface if we are using the
            // ImageReader to get frames and then drawing with OpenGL.
            // We only need the ImageReader surface in the session and request.

            val surfaces = listOf(imageReaderSurface) // Only use ImageReader surface

            captureRequestBuilder.addTarget(imageReaderSurface)
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            cameraDevice.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    session.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(this@MainActivity, "Session failed", Toast.LENGTH_SHORT).show()
                }
            }, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e("Camera", "Failed to create session", e)
        }
    }

    // --- Frame Processing (The main loop) ---

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage()
        if (image != null) {
            val planes = image.planes

            // Get the YUV planes' data and related parameters
            val yPlane = planes[0]
            val uPlane = planes[1]
            val vPlane = planes[2]

            // Get the byte arrays from the buffers
            val yArray = ByteArray(yPlane.buffer.remaining()).also { yPlane.buffer.get(it) }
            val uArray = ByteArray(uPlane.buffer.remaining()).also { uPlane.buffer.get(it) }
            val vArray = ByteArray(vPlane.buffer.remaining()).also { vPlane.buffer.get(it) }

            // Pass to OpenGL thread for native processing
            glSurfaceView.queueEvent {
                nativeProcessFrame(
                    yArray, uArray, vArray,
                    yPlane.rowStride,
                    uPlane.rowStride,
                    uPlane.pixelStride,
                    image.width,
                    image.height,
                    (glSurfaceView.renderer as MyGLRenderer).getTextureId()
                )
                // Request GLSurfaceView to draw the newly updated texture
                glSurfaceView.requestRender()
            }
            image.close()
        }
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        // closeCamera() and stopBackgroundThread() should be here
        glSurfaceView.onPause()
    }
}