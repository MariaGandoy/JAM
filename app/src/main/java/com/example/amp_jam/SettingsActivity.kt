package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.activity.ComponentActivity
import android.app.AlertDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class SettingsActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configurations)

        setupLogOut()
        setupDeleteAccount()
        setUpBackArrow()
    }

    private fun setUpBackArrow() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener  {
            finish()
        }
    }

    private fun setupLogOut() {
        val toolbarLogout = findViewById<ImageView>(R.id.toolbarLogout)
        toolbarLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    Firebase.auth.signOut()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    this.finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun setupDeleteAccount() {
        val toolbarLogout = findViewById<ImageView>(R.id.deleteAccountButton)
        toolbarLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta?")
                .setPositiveButton("Sí") { _, _ ->
                    // TODO eliminar cuenta
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

}