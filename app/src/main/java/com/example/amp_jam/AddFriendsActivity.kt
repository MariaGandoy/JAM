package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.LinearLayout



class AddFriendsActivity : ComponentActivity(){

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friends)

        firestore = FirebaseFirestore.getInstance()

        setupLogOut()
        loadAllUsers()


    }

    private fun setupLogOut() {
        val toolbarLogout = findViewById<ImageView>(R.id.toolbarLogout)
        toolbarLogout.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileActivity] Click LOG OUT EVENT button")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadAllUsers() {
        firestore.collection("usuarios")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val userName = document.getString("user") ?: "Unknown"
                        addUserToList(userName, document.id)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("AddFriendsActivity", "Error loading users", exception)
            }
    }

    private fun addUserToList(userName: String, userId: String) {
        val userContainer = findViewById<LinearLayout>(R.id.usersContainer)

        // TextView para mostrar el nombre del usuario
        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = userName
            textSize = 20f
            setPadding(16, 16, 16, 16)
        }

        // Esto no acaba de funcionar bien del todoo
        val addButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "+"
            setOnClickListener {
                addFriend(userId)
            }
        }

        // Crear un nuevo LinearLayout para contener el TextView y el Button
        val userLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            addView(textView)
            addView(addButton)
        }

        userContainer.addView(userLayout)
    }

    private fun addFriend(userId: String) {
        // Falta por implementar esto que no tenemos bien aún para la parte social
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            firestore.collection("usuarios").document(currentUser.uid)
                .collection("friends").document(userId).set(mapOf("added" to true))
                .addOnSuccessListener {
                    Log.d("AddFriendsActivity", "Friend added successfully: $userId")
                }
                .addOnFailureListener {
                    Log.e("AddFriendsActivity", "Failed to add friend", it)
                }
        }
    }



}