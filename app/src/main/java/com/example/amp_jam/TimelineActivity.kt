package com.example.amp_jam

import android.os.Bundle
import androidx.activity.ComponentActivity

class TimelineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timeline)

        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }
}