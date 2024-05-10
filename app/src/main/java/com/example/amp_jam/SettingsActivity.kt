package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class SettingsActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configurations)

        setupLogOut()
        setUpBackArrow()
    }

    private fun setUpBackArrow() {
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Close the activity and return to the fragment
        }
    }

    private fun setupLogOut() {
        val toolbarLogout = findViewById<ImageView>(R.id.toolbarLogout)
        toolbarLogout.setOnClickListener {
            Log.d("ProfileFragment", Firebase.auth.currentUser.toString())
            Firebase.auth.signOut()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            this.finish()

            Log.d("ProfileFragment", Firebase.auth.currentUser.toString())
        }
    
    }

}