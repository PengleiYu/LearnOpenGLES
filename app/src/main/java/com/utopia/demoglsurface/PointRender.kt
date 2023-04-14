package com.utopia.demoglsurface

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PointRender : GLSurfaceView.Renderer {
    @Volatile
    var angle: Float = 0f
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(1f, 1f, 0f, 1f)
    }


    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        val program = createShaderProgramStr(vs, fs)

        glUseProgram(program)

        glDrawArrays(GL_POINTS, 0, 1)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    }

    companion object {
        const val vs = """
        #version 300 es
// vertex shader
        void main() {
            gl_Position = vec4(0, 0, 0, 1);  // center
            gl_PointSize = 120.0;
        }
       """

        const val fs = """#version 300 es
// fragment shader
        precision highp float;

        out vec4 outColor;

        void main() {
            outColor = vec4(1, 0, 0, 1);  // red
        }
        """
    }
}