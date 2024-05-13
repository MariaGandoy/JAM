package com.example.amp_jam

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListOfFriendsActivity : ComponentActivity() {

    private lateinit var firestore: FirebaseFirestore
    private var currentUserUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friends)

        firestore = FirebaseFirestore.getInstance()
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        setUpBackArrow()

        if (currentUserUid != null) {
            loadFriendsList()
        } else {
            Log.e("ListOfFriendsActivity", "User ID is null.")
        }
    }

    private fun setUpBackArrow() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Amigos"
        toolbar.setNavigationOnClickListener  {
            finish()
        }
    }

    private fun loadFriendsList() {
        val userContainer = findViewById<LinearLayout>(R.id.usersContainer)
        userContainer.removeAllViews()
        currentUserUid?.let { uid ->
            firestore.collection("usuarios").document(uid).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.d("ListOfFriendsActivity", "No tienes amigos :(")
                    } else {
                        val friendsList = documents.map { it.id }
                        loadFriendDetails(friendsList)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ListOfFriendsActivity", "Error loading friend IDs", exception)
                }
        }
    }



    private fun loadFriendDetails(friendsList: List<String>) {
        friendsList.forEach { friendId ->
            firestore.collection("usuarios").document(friendId).get()
                .addOnSuccessListener { friendDoc ->
                    val friendName = friendDoc.getString("name") ?: "Unknown"
                    runOnUiThread {
                        addFriendToList(friendName, friendId)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ListOfFriendsActivity", "Error loading friend details", exception)
                }
        }
    }


    private fun addFriendToList(friendName: String, friendId: String) {
        val userContainer = findViewById<LinearLayout>(R.id.usersContainer)
        val userLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.7f
            )
            text = friendName
            textSize = 20f
            setPadding(16, 16, 16, 16)
        }
        textView.setTextColor(getResources().getColor(R.color.darkGreen))

        val removeButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.3f
            )
            text = "✘"
            background = ContextCompat.getDrawable(context, R.drawable.custom_button_background)
            setOnClickListener {
                removeFriend(friendId)
            }
        }
        removeButton.setTextColor(getResources().getColor(R.color.ivory))

        userLayout.addView(textView)
        userLayout.addView(removeButton)
        userContainer.addView(userLayout)
    }


    private fun removeFriend(friendId: String) {
        val currentUserRef = firestore.collection("usuarios").document(currentUserUid!!)
        val friendUserRef = firestore.collection("usuarios").document(friendId)

        firestore.runTransaction { transaction ->
            // Eliminar al amigo de la colección de amigos del usuario actual
            transaction.delete(currentUserRef.collection("friends").document(friendId))
            // Eliminar al usuario actual de la colección de amigos del amigo
            transaction.delete(friendUserRef.collection("friends").document(currentUserUid!!))
            null
        }.addOnSuccessListener {
            Log.d("ListOfFriendsActivity", "Friendship with $friendId removed successfully.")
            loadFriendsList() // Recargar
        }.addOnFailureListener { e ->
            Log.e("ListOfFriendsActivity", "Error removing friendship", e)
        }
    }


}
