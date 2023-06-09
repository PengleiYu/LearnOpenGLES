package com.utopia.demoglsurface

import android.icu.util.TimeUnit
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RotateTriangleRender : GLSurfaceView.Renderer {

    private lateinit var mTriangle: Triangle
    private lateinit var mSquare: Square
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated() called with: gl = $gl, config = $config")
        GLES20.glClearColor(1f, 0f, 0f, 1f)

        // initialize a triangle
        mTriangle = Triangle()
        // initialize a square
        mSquare = Square()
    }

    private val rotationMatrix = FloatArray(16)

    override fun onDrawFrame(gl: GL10) {
        Log.d(TAG, "onDrawFrame() called with: gl = $gl")
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
//
//        mTriangle.draw(vPMatrix)


        val scratch = FloatArray(16)

        // Create a rotation transformation for the triangle
        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1.0f)

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        // Draw triangle
        mTriangle.draw(scratch)
    }

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged() called with: gl = $gl, width = $width, height = $height")
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }


    companion object {
        private const val TAG = "MyGLRender"

        // number of coordinates per vertex in this array
        const val COORDS_PER_VERTEX = 3
        var triangleCoords = floatArrayOf(     // in counterclockwise order:
            0.0f, 0.622008459f, 0.0f,      // top
            -0.5f, -0.311004243f, 0.0f,    // bottom left
            0.5f, -0.311004243f, 0.0f      // bottom right
        )

        class Triangle {

            // Set color with red, green, blue and alpha (opacity) values
            val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

            private var vertexBuffer: FloatBuffer =
                // (number of coordinate values * 4 bytes per float)
                ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
                    // use the device hardware's native byte order
                    order(ByteOrder.nativeOrder())

                    // create a floating point buffer from the ByteBuffer
                    asFloatBuffer().apply {
                        // add the coordinates to the FloatBuffer
                        put(triangleCoords)
                        // set the buffer to read the first coordinate
                        position(0)
                    }
                }

            private val vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "void main() {" +
                        // the matrix must be included as a modifier of gl_Position
                        // Note that the uMVPMatrix factor *must be first* in order
                        // for the matrix multiplication product to be correct.
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}"

            // Use to access and set the view transformation
            private var vPMatrixHandle: Int = 0

            private val fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}"

            private var mProgram: Int = createShaderProgramStr(vertexShaderCode, fragmentShaderCode)

            private var positionHandle: Int = 0
            private var mColorHandle: Int = 0

            private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
            private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

            fun draw(mvpMatrix: FloatArray) { // pass in the calculated transformation matrix
                // get handle to shape's transformation matrix
                vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

                // Pass the projection and view transformation to the shader
                GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

                // Draw the triangle
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

                // Disable vertex array
                GLES20.glDisableVertexAttribArray(positionHandle)
                // Add program to OpenGL ES environment
                GLES20.glUseProgram(mProgram)


                // get handle to vertex shader's vPosition member
                positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {

                    // Enable a handle to the triangle vertices
                    GLES20.glEnableVertexAttribArray(it)

                    // Prepare the triangle coordinate data
                    GLES20.glVertexAttribPointer(
                        it,
                        COORDS_PER_VERTEX,
                        GLES20.GL_FLOAT,
                        false,
                        vertexStride,
                        vertexBuffer
                    )

                    // get handle to fragment shader's vColor member
                    mColorHandle =
                        GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->

                            // Set color for drawing the triangle
                            GLES20.glUniform4fv(colorHandle, 1, color, 0)
                        }

                    // Draw the triangle
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

                    // Disable vertex array
                    GLES20.glDisableVertexAttribArray(it)
                }
            }

        }


        // number of coordinates per vertex in this array
//        const val COORDS_PER_VERTEX = 3
        var squareCoords = floatArrayOf(
            -0.5f, 0.5f, 0.0f,      // top left
            -0.5f, -0.5f, 0.0f,      // bottom left
            0.5f, -0.5f, 0.0f,      // bottom right
            0.5f, 0.5f, 0.0f       // top right
        )

        class Square {

            private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

            // initialize vertex byte buffer for shape coordinates
            private val vertexBuffer: FloatBuffer =
                // (# of coordinate values * 4 bytes per float)
                ByteBuffer.allocateDirect(squareCoords.size * 4).run {
                    order(ByteOrder.nativeOrder())
                    asFloatBuffer().apply {
                        put(squareCoords)
                        position(0)
                    }
                }

            // initialize byte buffer for the draw list
            private val drawListBuffer: ShortBuffer =
                // (# of coordinate values * 2 bytes per short)
                ByteBuffer.allocateDirect(drawOrder.size * 2).run {
                    order(ByteOrder.nativeOrder())
                    asShortBuffer().apply {
                        put(drawOrder)
                        position(0)
                    }
                }
        }

    }
}