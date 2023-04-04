package com.utopia.demoglsurface

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val glSurfaceView = MyGLSurfaceView(this)
        setContentView(glSurfaceView)
    }
}