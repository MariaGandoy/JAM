package com.example.amp_jam

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


class SettingsActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configurations)

        setupLogOut()
        setupDeleteAccount()
        setUpBackArrow()
        setupDarkModeSwitch()
        setUpShareLocationSwitch()
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
                    clearUserSession()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun clearUserSession() {
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("user_id")
            putBoolean("isLoggedIn", false)
            apply()
        }
    }

    private fun setupDarkModeSwitch() {
        val darkModeSwitch = findViewById<Switch>(R.id.darkModeSwitch)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        darkModeSwitch.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setUpShareLocationSwitch() {
        val shareUbication = findViewById<Switch>(R.id.locationSwitch)
        shareUbication.isChecked =  SharedPreferencesHelper.getShareLocation(this)

        shareUbication.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                SharedPreferencesHelper.setShareLocation(this, true)
            } else {
                SharedPreferencesHelper.setShareLocation(this, false)
                val database = FirebaseFirestore.getInstance()
                val currentUser = FirebaseAuth.getInstance().currentUser

                var userData = hashMapOf<String, Any?>(
                    "lugar" to null
                )

                // Update location to null
                if (currentUser != null) {
                    database.collection("usuarios").document(currentUser!!.uid)
                        .update(userData)
                        .addOnFailureListener { e ->
                            Log.d("Firestore", "Error updating user data")
                        }
                }
            }
        }

    }


    private fun setupDeleteAccount() {
        val toolbarLogout = findViewById<ImageView>(R.id.deleteAccountButton)
        toolbarLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta?")
                .setPositiveButton("Sí") { _, _ ->
                    val db = FirebaseFirestore.getInstance()
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    val user = FirebaseAuth.getInstance().currentUser
                    val userDocRef = db.collection("usuarios").document(userId)

                    userDocRef.collection("friends").get().addOnSuccessListener { friendsSnapshot ->
                        val friendsIds = friendsSnapshot.documents.map { it.id }
                        userDocRef.collection("send_friend").get().addOnSuccessListener { sendFriendsSnapshot ->
                            val sendFriendIds = sendFriendsSnapshot.documents.map { it.id }
                            userDocRef.collection("receive_friend").get().addOnSuccessListener { receiveFriendsSnapshot ->
                                val receiveFriendIds = receiveFriendsSnapshot.documents.map { it.id }

                                db.runTransaction { transaction ->
                                    // Borrar documento del usuario
                                    transaction.delete(userDocRef)

                                    // Borrado de las listas de amigos
                                    friendsIds.forEach { friendId ->
                                        val friendRef = db.collection("usuarios").document(friendId)
                                        transaction.delete(friendRef.collection("friends").document(userId))
                                    }

                                    // Borrado de las listas de solicitudes recibidas
                                    sendFriendIds.forEach { senderId ->
                                        val senderRef = db.collection("usuarios").document(senderId)
                                        transaction.delete(senderRef.collection("receive_friend").document(userId))
                                    }

                                    // Borrado de las listas de solicitudes enviadas
                                    receiveFriendIds.forEach { receiverId ->
                                        val receiverRef = db.collection("usuarios").document(receiverId)
                                        transaction.delete(receiverRef.collection("send_friend").document(userId))
                                    }

                                    null
                                }.addOnSuccessListener {
                                    // Eliminar el usuario de Authentication
                                    user?.delete()?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("DeleteAccount", "Cuenta eliminada con éxito.")
                                            clearUserSession()
                                            FirebaseAuth.getInstance().signOut() // Cerrar sesión después de eliminar la cuenta
                                            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Log.e("DeleteAccount", "Error al eliminar cuenta de autenticación", task.exception)
                                        }
                                    }
                                }.addOnFailureListener { e ->
                                    Log.e("DeleteAccount", "Error al eliminar los datos de Firestore", e)
                                }
                            }
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }


}