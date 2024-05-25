package com.example.amp_jam.ui.theme.friends

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.amp_jam.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsRequestFragment : ComponentActivity() {

    private lateinit var firestore: FirebaseFirestore
    private var currentUserUid: String? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friend_request)

        setUpBackArrow()
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        firestore = FirebaseFirestore.getInstance()
        if (currentUserUid != null) {
            refreshFriendRequests()
        }
    }

    private fun setUpBackArrow() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener  {
            finish()
        }
    }

    // Función para cargar las listas
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun refreshFriendRequests() {
        loadFriendRequests("receive_friend", R.id.receivedRequestsContainer)
        loadFriendRequests("send_friend", R.id.sentRequestsContainer)
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadFriendRequests(collectionPath: String, containerId: Int) {
        val container = findViewById<LinearLayout>(containerId)
        container.removeAllViews() // Limpiar vistas antiguas antes de cargar nuevas IMPORTANTE
        firestore.collection("usuarios").document(currentUserUid!!)
            .collection(collectionPath).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userId = document.id
                    fetchUserDetailsAndAddToView(container, userId, collectionPath == "receive_friend")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FriendsRequestFragment", "Error loading friend requests", e)
            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fetchUserDetailsAndAddToView(container: LinearLayout, userId: String, isReceived: Boolean) {
        firestore.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: "unknown"
                val profilePhotoURL = document.getString("photo")
                    ?: Uri.parse("android.resource://" + getPackageName() +"/"+R.drawable.sample_user).toString()

                addUserToScrollView(container, userName, profilePhotoURL, userId, isReceived)
            }
            .addOnFailureListener { e ->
                Log.e("FriendsRequestFragment", "Error loading user details", e)
                addUserToScrollView(container, "Error loading name", "nophoto", userId, isReceived)
            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addUserToScrollView(container: LinearLayout, userName: String, profilePhotoURL: String, userId: String, isReceived: Boolean) {

        // Layout para la imagen de perfil
        val profilePhotoLayout = LinearLayout(this).apply {
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
            .load(profilePhotoURL)
            .into(profilePhotoView)

        // TextView para el nombre de usuario
        val userNameView = TextView(this).apply {
            text = userName + " "
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
        }
        userNameView.setTextColor(ContextCompat.getColor(this, R.color.black))

        // Agregar la imagen de perfil y el nombre de usuario al layout de la imagen de perfil
        profilePhotoLayout.addView(profilePhotoView)
        profilePhotoLayout.addView(userNameView)

        // Layout para los botones
        if (isReceived) {
            //Boton de aceptar
            val acceptButton = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100)
                text = "✔"
                background = ContextCompat.getDrawable(context, R.drawable.custom_round_button_background)
                setPadding(20, 0, 20, 0)
                setOnClickListener { manageFriendRequest(userId, true) }
            }
            acceptButton.setTextColor(ContextCompat.getColor(this, R.color.ivory))

            val textView2 = TextView(this).apply {
                text = "  "
                textSize = 20f
                setPadding(16, 0, 0, 16)
            }

            //Boton de rechazar
            val rejectButton = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100)
                text = "✘"
                background = ContextCompat.getDrawable(context, R.drawable.custom_button_background_2)
                setPadding(20, 0, 20, 0)
                setOnClickListener { manageFriendRequest(userId, false) }
            }
            rejectButton.setTextColor(ContextCompat.getColor(this, R.color.ivory))

            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, // Ancho como 0, pero con peso
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.7f // Peso menor para dar más espacio al botón
                )
                text = "  "
                textSize = 20f
                setPadding(16, 16, 16, 16)
            }

            profilePhotoLayout.addView(textView)

            // Agregar los botones al layout
            profilePhotoLayout.addView(acceptButton)
            profilePhotoLayout.addView(textView2)
            profilePhotoLayout.addView(rejectButton)
        } else {
            //Rechazar si se envió por error
            val cancelButton = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100)
                text = "✘"
                background = ContextCompat.getDrawable(context, R.drawable.custom_button_background_2)
                setPadding(20, 0, 20, 0)
                setOnClickListener { cancelFriendRequest(userId) }
            }
            cancelButton.setTextColor(ContextCompat.getColor(this, R.color.ivory))

            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, // Ancho como 0, pero con peso
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.7f // Peso menor para dar más espacio al botón
                )
                text = "  "
                textSize = 20f
                setPadding(16, 16, 16, 16)
            }
            profilePhotoLayout.addView(textView)

            // Agregar el botón al layout
            profilePhotoLayout.addView(cancelButton)
        }




        val userLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 50 // Establece el margen inferior
            }
            addView(profilePhotoLayout)
        }

        // Agregar el layout de usuario al contenedor principal
        container.addView(userLayout)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun manageFriendRequest(userId: String, accept: Boolean) {
        val currentUserRef = firestore.collection("usuarios").document(currentUserUid!!)
        val otherUserRef = firestore.collection("usuarios").document(userId)

        firestore.runTransaction { transaction ->
            // Eliminar de las listas "receive_friend"
            transaction.delete(currentUserRef.collection("receive_friend").document(userId))
            transaction.delete(otherUserRef.collection("receive_friend").document(userId))
            // Eliminar de las listas "send_friend"
            transaction.delete(currentUserRef.collection("send_friend").document(currentUserUid!!))
            transaction.delete(otherUserRef.collection("send_friend").document(currentUserUid!!))

            if (accept) {
                // Añadir al usuario que acepta en la lista de amigos del que envió la solicitud
                transaction.set(otherUserRef.collection("friends").document(currentUserUid!!), mapOf("status" to "friend"))
                // Añadir al usuario que envió la solicitud en la lista de amigos del que acepta
                transaction.set(currentUserRef.collection("friends").document(userId), mapOf("status" to "friend"))
            }
            null
        }.addOnSuccessListener {
            refreshFriendRequests()
            Log.d("FriendsRequestFragment", "Friend request managed successfully.")
        }.addOnFailureListener { e ->
            Log.e("FriendsRequestFragment", "Error managing friend request", e)
        }
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    private fun cancelFriendRequest(userId: String) {
        val currentUserRef = firestore.collection("usuarios").document(currentUserUid!!)
        val otherUserRef = firestore.collection("usuarios").document(userId)

        firestore.runTransaction { transaction ->
            // Eliminar al usuario de la lista de solicitudes enviadas del usuario actual
            transaction.delete(currentUserRef.collection("send_friend").document(userId))
            // Eliminar al usuario actual de la lista de solicitudes recibidas del otro usuario
            transaction.delete(otherUserRef.collection("receive_friend").document(currentUserUid!!))

            null
        }.addOnSuccessListener {
            refreshFriendRequests()
            Log.d("FriendsRequestFragment", "Friend request cancelled successfully.")
        }.addOnFailureListener { e ->
            Log.e("FriendsRequestFragment", "Error cancelling friend request", e)
        }
    }


}
