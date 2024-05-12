package com.example.amp_jam

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendsActivity : ComponentActivity() {

    private lateinit var firestore: FirebaseFirestore
    private var currentUserUid: String? = null
    private var sentFriendUserIds: MutableSet<String> = mutableSetOf()
    private var friendUserIds: MutableSet<String> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friends)

        setUpBackArrow()

        firestore = FirebaseFirestore.getInstance()
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        loadSentFriendRequests {
            loadFriends {
                loadAllUsers()
            }
        }

    }

    private fun setUpBackArrow() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener  {
            finish()
        }
    }

    private fun loadFriends(onComplete: () -> Unit) {
        currentUserUid?.let { uid ->
            firestore.collection("usuarios").document(uid).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    friendUserIds.clear()
                    for (document in documents) {
                        friendUserIds.add(document.id)
                    }
                    onComplete()
                }
                .addOnFailureListener {
                    Log.e("AddFriendsActivity", "Error loading friends", it)
                    onComplete()
                }
        } ?: onComplete()
    }


    private fun loadSentFriendRequests(onComplete: () -> Unit) {
        currentUserUid?.let { uid ->
            firestore.collection("usuarios").document(uid).collection("send_friend")
                .get()
                .addOnSuccessListener { documents ->
                    sentFriendUserIds.clear()
                    for (document in documents) {
                        sentFriendUserIds.add(document.id)
                    }
                    onComplete()
                }
                .addOnFailureListener {
                    Log.e("AddFriendsActivity", "Error loading sent friend requests", it)
                    onComplete() // Continuar aunque haya un error.
                }
        } ?: onComplete() // Ejecutar onComplete si currentUserUid es nulo.
    }


    private fun loadAllUsers() {
        firestore.collection("usuarios")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    findViewById<LinearLayout>(R.id.usersContainer).removeAllViews()
                    for (document in documents) {
                        val userId = document.id
                        if (userId != currentUserUid && !sentFriendUserIds.contains(userId) && !friendUserIds.contains(userId)) {
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

        var textView= TextView(this)
        if (userName.length > 20) {
            // TextView para mostrar el nombre del usuario
            textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, // Ancho como 0, pero con peso
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.7f // Peso menor para dar más espacio al botón
                )
                text = userName + "\n"
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            textView.setTextColor(getResources().getColor(R.color.darkGreen))
        }
        else {
            // TextView para mostrar el nombre del usuario
            textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, // Ancho como 0, pero con peso
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.7f // Peso menor para dar más espacio al botón
                )
                text = userName + "\n"
                textSize = 20f
                setPadding(16, 16, 16, 16)
            }
            textView.setTextColor(getResources().getColor(R.color.darkGreen))
        }

        // Button para agregar a amigos
        val addButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0, // Ancho como 0, pero con peso
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f // Peso para asegurar que el botón tenga espacio visible
            )
            text = "+"
            background = ContextCompat.getDrawable(context, R.drawable.custom_round_button_background)
            setOnClickListener {
                addFriend(userId)

            }
        }
        addButton.setTextColor(getResources().getColor(R.color.ivory))

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
            // Implementar adiciones de amigo como antes, pero ahora refrescar la lista
            firestore.collection("usuarios").document(currentUser.uid)
                .collection("send_friend").document(friendUserId).set(mapOf("added" to true))
                .addOnSuccessListener {
                    Log.d("AddFriendsActivity", "Friend added to send_friend successfully: $friendUserId")
                    loadSentFriendRequests {
                        loadAllUsers() // Recargar usuarios después de añadir a un amigo
                    }
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
