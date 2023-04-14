package com.utopia.demoglsurface

import android.opengl.GLES32.*
import android.util.Log
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

fun createShaderProgramStr(vertxShader: String, fragShader: String): Int {
    val vertShaderRef = prepareShader(GL_VERTEX_SHADER, vertxShader)
    val fragShaderRef = prepareShader(GL_FRAGMENT_SHADER, fragShader)
    val program = glCreateProgram()
    glAttachShader(program, vertShaderRef)
    glAttachShader(program, fragShaderRef)
    finalizeShaderProgram(program)
    return program
}

fun finalizeShaderProgram(program: Int): Int {
    glLinkProgram(program)
    checkOpenGLError()
    val buffer = IntBuffer.allocate(1)
    glGetProgramiv(program, GL_LINK_STATUS, buffer)
    if (buffer.get() != 1) {
        Log.d(TAG, "linking failed")
        printProgramLog(program)
    }
    return program
}

// TODO: shaderStr 换成path
private fun prepareShader(shaderTYPE: Int, shaderStr: String): Int {
    val shaderRef = glCreateShader(shaderTYPE)
    glShaderSource(shaderRef, shaderStr)
    glCompileShader(shaderRef)

    checkOpenGLError()

    val buffer = IntBuffer.allocate(1)
    glGetShaderiv(shaderRef, GL_COMPILE_STATUS, buffer)
    if (buffer.get() != GL_TRUE) {
        val typeStr = when (shaderTYPE) {
            GL_VERTEX_SHADER -> "Vertex "
            GL_TESS_CONTROL_SHADER -> "Tess Control "
            GL_TESS_EVALUATION_SHADER -> "Tess Eval "
            GL_GEOMETRY_SHADER -> "Geometry "
            GL_FRAGMENT_SHADER -> "Fragment "
            GL_COMPUTE_SHADER -> "Compute "
            else -> throw RuntimeException("未知type")
        }
        Log.d(TAG, "$typeStr shader compilation error for shader: '${shaderStr}',")
        printShaderLog(shaderRef);
    }
    return shaderRef
}

fun printProgramLog(program: Int) {
    val log = glGetProgramInfoLog(program)
    if (log.isNullOrEmpty()) return
    Log.d(TAG, "Program Info Log: $log")
}

fun printShaderLog(shaderRef: Int) {
    val log = glGetShaderInfoLog(shaderRef)
    if (log.isNullOrEmpty()) return
    Log.d(TAG, "Shader Info Log: $log")
}

private fun checkOpenGLError(): Boolean {
    var foundErr = false;
    var glErr = glGetError()
    while (glErr != GL_NO_ERROR) {
        Log.d(TAG, "glError: $glErr")
        foundErr = true
        glErr = glGetError()
    }
    return foundErr
}

fun FloatArray.toBuffer(): Buffer {
    return ByteBuffer.allocateDirect(size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(this)
        .position(0)
}

fun ShortArray.toBuffer(): Buffer {
    return ByteBuffer.allocateDirect(size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .put(this)
        .position(0)
}

private const val TAG = "Utils"