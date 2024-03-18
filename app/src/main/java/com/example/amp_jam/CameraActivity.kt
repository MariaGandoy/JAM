package com.example.amp_jam

import android.os.Bundle
import androidx.activity.ComponentActivity

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera)

        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }
}