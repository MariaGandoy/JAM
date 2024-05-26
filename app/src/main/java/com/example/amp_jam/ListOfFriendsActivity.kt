package com.example.amp_jam

import android.net.Uri
import android.os.Bundle
import android.service.voice.VoiceInteractionSession.VisibleActivityCallback
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
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

        val searchEditText = findViewById<EditText>(R.id.textInputLayout2)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (currentUserUid != null) {
                    loadFriendsList(s.toString())
                }
            }
        })

        if (currentUserUid != null) {
            loadFriendsList("")
        } else {
            Log.e("ListOfFriendsActivity", "User ID is null.")
        }
    }



    private fun loadFriendsList(filter: String = "") {
        val userContainer = findViewById<LinearLayout>(R.id.usersContainer)
        val progressBar = findViewById<ProgressBar>(R.id.progress_circular)
        progressBar.visibility = View.VISIBLE

        userContainer.removeAllViews()
        currentUserUid?.let { uid ->
            firestore.collection("usuarios").document(uid).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.d("ListOfFriendsActivity", "No tienes amigos :(")
                    } else {
                        val friendsList = documents.map { it.id }
                        loadFriendDetails(friendsList, filter)
                        progressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    progressBar.visibility = View.GONE
                    Log.e("ListOfFriendsActivity", "Error loading friend IDs", exception)
                }
        }
    }

    private fun loadFriendDetails(friendsList: List<String>, filter: String) {
        friendsList.forEach { friendId ->
            firestore.collection("usuarios").document(friendId).get()
                .addOnSuccessListener { friendDoc ->
                    val friendName = friendDoc.getString("name") ?: "Unknown"
                    val profilePhotoURL = friendDoc.getString("photo") ?: Uri.parse("android.resource://" + packageName + "/" + R.drawable.sample_user).toString()

                    if (filter.isEmpty() || friendName.contains(filter, ignoreCase = true)) {
                        runOnUiThread {
                            addFriendToList(friendName, friendId, profilePhotoURL)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ListOfFriendsActivity", "Error loading friend details", exception)
                }
        }
    }


    private fun addFriendToList(friendName: String, friendId: String, friendPhoto: String) {
        val userContainer = findViewById<LinearLayout>(R.id.usersContainer)

        val userLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // ImageView para la imagen de perfil
        val profilePhotoView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                125, // Tamaño deseado de la imagen de perfil
                125
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginEnd = 10 // Ajusta el margen entre la imagen y el nombre de usuario
            }
            setPadding(20, 0, 10, 0)
        }
        Glide.with(this)
            .load(friendPhoto)
            .into(profilePhotoView)


        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.7f
            )
            text = friendName + "\n"
            textSize = if (friendName.length > 25) 16f else 20f
            setPadding(16, 16, 16, 16)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        val removeButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100)
            text = "x"
            background =
                ContextCompat.getDrawable(context, R.drawable.custom_round_button_background)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            setOnClickListener {
                removeFriend(friendId)
            }
        }
        removeButton.setTextColor(getResources().getColor(R.color.ivory))

        userLayout.addView(profilePhotoView)
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
