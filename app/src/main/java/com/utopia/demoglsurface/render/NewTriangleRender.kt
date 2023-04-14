package com.utopia.demoglsurface.render

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import com.utopia.demoglsurface.createShaderProgramStr
import com.utopia.demoglsurface.toBuffer
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class NewTriangleRender : Renderer {
    private lateinit var triangle: Triangle

    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 1f)
        triangle = Triangle()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // p矩阵
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 20f)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
        glClear(GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)

        Matrix.setRotateM(modelMatrix, 0, 45f, 0f, 1f, 0f)
        Matrix.setLookAtM(
            viewMatrix, 0,
            0F, 5F, 10F,
            0F, 0F, 0F,
            0F, 1F, 0F
        )
        Matrix.setIdentityM(mvpMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        triangle.draw(mvpMatrix)
    }

    companion object {
        class Triangle {
            private val r: Float = 0.5f

            private val triangleCoords = floatArrayOf(     // in counterclockwise order:
                -r, r, r,//0
                -r, -r, r,//1
                r, -r, r,//2
                r, r, r,//3
                r, -r, -r,//4
                r, r, -r,//5
                -r, -r, -r,//6
                -r, r, -r//7
            )

            private val vertexBuffer: Buffer =
                floatArrayOf(
                    -r, r, r,//0
                    -r, -r, r,//1
                    r, -r, r,//2
                    r, r, r,//3
                    r, -r, -r,//4
                    r, r, -r,//5
                    -r, -r, -r,//6
                    -r, r, -r//7
                ).toBuffer()
            private val indices = shortArrayOf(
                0, 1, 2, 0, 2, 3,
                3, 2, 4, 3, 4, 5,
                5, 4, 6, 5, 6, 7,
                7, 6, 1, 7, 1, 0,
                7, 0, 3, 7, 3, 5,
                6, 1, 2, 6, 2, 4
            )
            private val mIndicesBuffer = indices.toBuffer()
            private val colorBuffer =
                floatArrayOf(
                    1f, 1f, 0f, 1f,
                    1f, 1f, 0f, 1f,
                    1f, 1f, 0f, 1f,
                    1f, 1f, 0f, 1f,
                    1f, 0f, 0f, 1f,
                    1f, 0f, 0f, 1f,
                    1f, 0f, 0f, 1f,
                    1f, 0f, 0f, 1f
                ).toBuffer()
            private val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
            private val coordsPerVertex = 3
            private val vertexStride =
                coordsPerVertex * 4 // 4 bytes per vertex
            private val vertexCount: Int = triangleCoords.size / coordsPerVertex

            private val vs =
                """
                    attribute vec4 a_Position;
                    attribute vec4 a_color;
                    uniform mat4 mvpMatrix;

                    varying vec4 v_color;
                    void main()
                    {
                        v_color = a_color;
                        gl_Position = mvpMatrix * a_Position;
                    }
                """.trimIndent()
            private val fs =
                """
                    precision mediump float;
                    uniform vec4 u_color;
                    varying vec4 v_color;
                    void main()
                    {
                        gl_FragColor = v_color;
                    }
                """.trimIndent()

            private val program = createShaderProgramStr(vs, fs)

            fun draw(mvpMatrix: FloatArray) {
                // 统一变量赋值
                val mvpMatrixLoc = glGetUniformLocation(program, "mvpMatrix")
//                val colorLoc = glGetUniformLocation(program, "vColor")
                glUniformMatrix4fv(mvpMatrixLoc, 1, false, mvpMatrix, 0)
//                glUniform4fv(colorLoc, 1, color, 0)
                // 顶点属性赋值
                val positionLoc = glGetAttribLocation(program, "a_Position")
                glVertexAttribPointer(positionLoc, 3, GL_FLOAT, false, 0, vertexBuffer)
                val colorLoc = glGetAttribLocation(program, "a_color")
                glVertexAttribPointer(colorLoc, 3, GL_FLOAT, false, 0, colorBuffer)

                glUseProgram(program)

                glEnableVertexAttribArray(positionLoc)
                glEnableVertexAttribArray(colorLoc)
//                glDrawArrays(GL_TRIANGLES, 0, vertexCount)
                glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_SHORT, mIndicesBuffer)
            }
        }
    }
}