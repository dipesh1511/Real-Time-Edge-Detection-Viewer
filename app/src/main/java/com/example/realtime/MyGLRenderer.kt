package com.example.realtime

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {

    private val TAG = "MyGLRenderer"

    private var mProgramHandle: Int = 0
    private var mTextureID: Int = 0

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer

    // Top-left, Bottom-left, Bottom-right, Top-right
    private val mVerticesData = floatArrayOf(
        -1.0f, -1.0f,  // Bottom-left -> Top-left (after rotation)
        1.0f, -1.0f,   // Bottom-right -> Bottom-left
        1.0f, 1.0f,    // Top-right -> Bottom-right
        -1.0f, 1.0f     // Top-left -> Top-right
    )

    // Texture coordinates (0 to 1)
    private val mTextureCoordsData = floatArrayOf(
        1.0f, 0.0f,    // Top-left ko Top-right (1.0f) se swap kiya
        1.0f, 1.0f,    // Bottom-left ko Bottom-right (1.0f) se swap kiya
        0.0f, 1.0f,    // Bottom-right ko Bottom-left (0.0f) se swap kiya
        0.0f, 0.0f     // Top-right ko Top-left (0.0f) se swap kiya
    )

    // 🚨 FIX 1: uMVPMatrix uniform hata diya, aPosition ab seedhe gl_Position mein jaayega.
    private val mVertexShaderCode =
        """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        void main() {
            gl_Position = aPosition; 
            vTexCoord = aTexCoord;
        }
        """

    private val mFragmentShaderCode =
        """
        precision mediump float;
        uniform sampler2D sTexture;
        varying vec2 vTexCoord;
        void main() {
            gl_FragColor = texture2D(sTexture, vTexCoord);
        }
        """

    init {
        mVertexBuffer = ByteBuffer.allocateDirect(mVerticesData.size * 4).apply {
            order(ByteOrder.nativeOrder())
        }.asFloatBuffer().apply {
            put(mVerticesData)
            position(0)
        }

        mTextureBuffer = ByteBuffer.allocateDirect(mTextureCoordsData.size * 4).apply {
            order(ByteOrder.nativeOrder())
        }.asFloatBuffer().apply {
            put(mTextureCoordsData)
            position(0)
        }
    }

    fun getTextureId(): Int {
        return mTextureID
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated: Initializing OpenGL ES")

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode)

        mProgramHandle = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgramHandle, vertexShader)
        GLES20.glAttachShader(mProgramHandle, fragmentShader)
        GLES20.glLinkProgram(mProgramHandle)

        GLES20.glGetAttribLocation(mProgramHandle, "aPosition")
        GLES20.glGetAttribLocation(mProgramHandle, "aTexCoord")

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mTextureID = textures[0]
        Log.d(TAG, "Generated Texture ID: $mTextureID")

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (mTextureID == 0) {
            return
        }

        GLES20.glUseProgram(mProgramHandle)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID)

        val sTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "sTexture")
        GLES20.glUniform1i(sTextureLoc, 0)

        val aPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition")
        GLES20.glEnableVertexAttribArray(aPositionLoc)
        GLES20.glVertexAttribPointer(
            aPositionLoc, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer
        )

        val aTexCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTexCoord")
        GLES20.glEnableVertexAttribArray(aTexCoordLoc)
        GLES20.glVertexAttribPointer(
            aTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer
        )

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

        GLES20.glDisableVertexAttribArray(aPositionLoc)
        GLES20.glDisableVertexAttribArray(aTexCoordLoc)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader $type: ${GLES20.glGetShaderInfoLog(shader)}")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }
}