package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map)

        setupAddEventButton()

        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }

    private fun setupAddEventButton() {
        val addBtn = findViewById<Button>(R.id.addEventButton)
        addBtn.setOnClickListener {
            val intent = Intent(this, MapEventActivity::class.java)
            startActivity(intent)
        }
    }
}
