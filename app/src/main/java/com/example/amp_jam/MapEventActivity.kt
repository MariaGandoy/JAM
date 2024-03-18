package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity

class MapEventActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_event)

        setupAddEventButton()

        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }

    private fun setupAddEventButton() {
        val addBtn = findViewById<Button>(R.id.idBtnAdd)
        addBtn.setOnClickListener {
            Log.d(TAG, "[MapEvent] Click ADD EVENT button")
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        private val TAG: String = MapEventActivity::class.java.simpleName
    }
}