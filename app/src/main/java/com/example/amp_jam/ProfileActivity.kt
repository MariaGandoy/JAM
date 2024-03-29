package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity

class ProfileActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        setupBackButton()
        setupLogOut()
        setupAddFriends()

        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }

    private fun setupBackButton() {
        val addBtn = findViewById<Button>(R.id.button)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileActivity] Click BACK EVENT button")
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupLogOut() {
        val addBtn = findViewById<Button>(R.id.button6)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileActivity] Click LOG OUT EVENT button")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriends() {
        val addBtn = findViewById<Button>(R.id.button5)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileActivity] Click ADD FRIENDS MENU EVENT button")
            val intent = Intent(this, AddFriendsActivity::class.java)
            startActivity(intent)
        }
    }
}