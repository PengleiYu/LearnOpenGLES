package com.utopia.demoglsurface.render

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import android.os.SystemClock
import com.utopia.demoglsurface.createShaderProgramStr
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

class NewTriangleRender : Renderer {
    private lateinit var triangle: Triangle

    private var x = 0f
    private var inc = 0.01f

//    private val modelMatrix = FloatArray(16)
//    private val viewMatrix = FloatArray(16)
//    private val projectionMatrix = FloatArray(16)
//    private val mvMatrix = FloatArray(16)
//    private val mvpMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(1f, 0f, 0f, 1f)
        triangle = Triangle()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // p矩阵
//        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

//        // m矩阵
//        val time = SystemClock.uptimeMillis() % 4000L
//        val angle = 0.090f * time.toInt()
//        Matrix.setRotateM(modelMatrix, 0, angle, 0f, 0f, -1.0f)
//        // v矩阵
//        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1f, 0f)
//        // 算mvp矩阵
//        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
//        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

//        triangle.draw(mvpMatrix)

        x += inc
        if (abs(x) >= 1) inc *= -1

        triangle.draw(x)
    }

    companion object {
        class Triangle {
            private val triangleCoords = floatArrayOf(     // in counterclockwise order:
                0.25f, -0.25f, 1f,      // top
                -0.25f, -0.25f, 1.0f,    // bottom left
                0.25f, 0f, 1.0f      // bottom right
            )
            private val vertexBuffer: Buffer =
                ByteBuffer.allocateDirect(triangleCoords.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(triangleCoords)
                    .position(0)

            private val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
            private val coordsPerVertex = 3
            private val vertexStride =
                coordsPerVertex * 4 // 4 bytes per vertex
            private val vertexCount: Int = triangleCoords.size / coordsPerVertex

            private val vs = """
                uniform float offset;
                attribute vec4 vPosition;
                void main(void) {
                    gl_Position = vec4( vPosition.x+offset, vPosition.y, vPosition.z, 1.0);
                }
            """.trimIndent()
            private val fs = """
#ifdef GL_ES
precision mediump float;
#endif

void main(void) {
    if(gl_FragCoord.x<295.0)
        gl_FragColor = vec4(0.0,1.0,0.0,1.0);
    else
        gl_FragColor = vec4(0.0,0.0,1.0,1.0);
}
            """.trimIndent()

            private val program = createShaderProgramStr(vs, fs)

            fun draw(offset: Float) {
                // 统一变量赋值
                val offsetLoc = glGetUniformLocation(program, "offset")
                glUniform1f(offsetLoc, offset)
//                val mvpMatrixLoc = glGetUniformLocation(program, "uMVPMatrix")
//                val colorLoc = glGetUniformLocation(program, "vColor")
//                glUniformMatrix4fv(mvpMatrixLoc, 1, false, mvpMatrix, 0)
//                glUniform4fv(colorLoc, 1, color, 0)
                // 顶点属性赋值
                val positionLoc = glGetAttribLocation(program, "vPosition")
                glVertexAttribPointer(
                    positionLoc,
                    coordsPerVertex,
                    GL_FLOAT,
                    false,
                    vertexStride,
                    vertexBuffer
                )

                glUseProgram(program)

                glEnableVertexAttribArray(positionLoc)
                glDrawArrays(GL_TRIANGLES, 0, vertexCount)
                glDisableVertexAttribArray(positionLoc)
            }
        }
    }
}