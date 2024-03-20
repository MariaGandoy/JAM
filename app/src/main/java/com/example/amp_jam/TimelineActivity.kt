package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.ComponentActivity

class TimelineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timeline)

        setUpSeeMapButton()

        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }

    private fun setUpSeeMapButton() {
        val addBtn = findViewById<Button>(R.id.verMapaButton)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[TimelineActivity] Click SEE MAP button")
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }
}