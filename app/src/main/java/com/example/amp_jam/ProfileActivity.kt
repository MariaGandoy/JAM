package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
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
            val intent = Intent(this, MapEventActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupLogOut() {
        val addBtn = findViewById<Button>(R.id.button6)
        addBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriends() {
        val addBtn = findViewById<Button>(R.id.button5)
        addBtn.setOnClickListener {
            val intent = Intent(this, AddFriendsActivity::class.java)
            startActivity(intent)
        }
    }
}