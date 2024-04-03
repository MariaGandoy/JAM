package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity

class MapActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map)

        setupAddEventButton()
        setUpUbicationListener()

        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }

    private fun setupAddEventButton() {
        val addBtn = findViewById<Button>(R.id.addEventButton)
        addBtn.setOnClickListener {
            val eventDialog = MapEventFragment()

            eventDialog.show(supportFragmentManager, "MapEvent")
        }
    }

    private fun setUpUbicationListener() {
        val textInputUbication = findViewById<EditText>(R.id.textInputUbication)
        textInputUbication.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("JAM_NAVIGATION", "[Map] Ubication text changed to: $s")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
