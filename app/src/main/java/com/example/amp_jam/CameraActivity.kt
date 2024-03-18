package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.ComponentActivity

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera)

        setupCameraButtons()

        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }

    private fun setupCameraButtons() {
        val folderBtn = findViewById<ImageButton>(R.id.imageButton2)
        folderBtn.setOnClickListener {
            Log.d(TAG, "[Camera] Click FOLDER button")
        }

        val pictureBtn = findViewById<ImageButton>(R.id.imageButton4)
        pictureBtn.setOnClickListener {
            Log.d(TAG, "[Camera] Click TAKE PICTURE button")
        }

        val filtersBtn = findViewById<ImageButton>(R.id.imageButton3)
        filtersBtn.setOnClickListener {
            Log.d(TAG, "[Camera] Click FILTERS button")
        }

        val settingsBtn = findViewById<ImageButton>(R.id.imageButton5)
        settingsBtn.setOnClickListener {
            Log.d(TAG, "[Camera] Click SETTINGS button")
        }
    }

    companion object {
        private val TAG: String = CameraActivity::class.java.simpleName
    }
}