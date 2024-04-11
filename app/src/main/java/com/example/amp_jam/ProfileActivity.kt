package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class ProfileActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        setupBackButton()
        setupLogOut()
        setupAddFriends()

    }

    private fun setupBackButton() {
        val toolbarBack = findViewById<ImageView>(R.id.toolbarBack)
        toolbarBack.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileActivity] Click BACK EVENT button")
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupLogOut() {
        val toolbarLogout = findViewById<ImageView>(R.id.toolbarLogout)
        toolbarLogout.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileActivity] Click LOG OUT EVENT button")
            Firebase.auth.signOut()
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