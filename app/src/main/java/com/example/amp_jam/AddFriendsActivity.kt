package com.example.amp_jam

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendsActivity : ComponentActivity() {

    private lateinit var firestore: FirebaseFirestore
    private var currentUserUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friends)

        firestore = FirebaseFirestore.getInstance()
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        loadAllUsers()
    }

    private fun loadAllUsers() {
        firestore.collection("usuarios")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val userId = document.id
                        // Solo mostrar usuarios que no son el usuario actual
                        if (userId != currentUserUid) {
                            val userName = document.getString("user") ?: "Unknown"
                            addUserToList(userName, userId)
                        }
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
                0, // Ancho como 0, pero con peso
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.7f // Peso menor para dar más espacio al botón
            )
            text = userName
            textSize = 20f
            setPadding(16, 16, 16, 16)
        }

        // Button para agregar a amigos
        val addButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0, // Ancho como 0, pero con peso
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.3f // Peso para asegurar que el botón tenga espacio visible
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


    private fun addFriend(friendUserId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && friendUserId != currentUser.uid) {
            // Añadir a send_friend del usuario actual
            firestore.collection("usuarios").document(currentUser.uid)
                .collection("send_friend").document(friendUserId).set(mapOf("added" to true))
                .addOnSuccessListener {
                    Log.d("AddFriendsActivity", "Friend added to send_friend successfully: $friendUserId")
                }
                .addOnFailureListener {
                    Log.e("AddFriendsActivity", "Failed to add to send_friend", it)
                }
            // Añadir a receive_friend del otro usuario
            firestore.collection("usuarios").document(friendUserId)
                .collection("receive_friend").document(currentUser.uid).set(mapOf("added" to true))
                .addOnSuccessListener {
                    Log.d("AddFriendsActivity", "Friend added to receive_friend successfully: ${currentUser.uid}")
                }
                .addOnFailureListener {
                    Log.e("AddFriendsActivity", "Failed to add to receive_friend", it)
                }
        }
    }
}
