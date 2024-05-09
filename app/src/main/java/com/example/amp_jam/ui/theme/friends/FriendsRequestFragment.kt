package com.example.amp_jam.ui.theme.friends

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.amp_jam.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsRequestFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private var currentUserUid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.friend_request, container, false)
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        firestore = FirebaseFirestore.getInstance()

        if (currentUserUid != null) {
            refreshFriendRequests(view)
        }

        return view
    }

    // Función para cargar las listas
    private fun refreshFriendRequests(view: View) {
        loadFriendRequests(view, "receive_friend", R.id.receivedRequestsContainer)
        loadFriendRequests(view, "send_friend", R.id.sentRequestsContainer)
    }


    private fun loadFriendRequests(view: View, collectionPath: String, containerId: Int) {
        val container = view.findViewById<LinearLayout>(containerId)
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

    private fun fetchUserDetailsAndAddToView(container: LinearLayout, userId: String, isReceived: Boolean) {
        firestore.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: "Unknown"
                val profilePhotoURL = document.getString("photo") ?: "nophoto"

                addUserToScrollView(container, userName, profilePhotoURL, userId, isReceived)
            }
            .addOnFailureListener { e ->
                Log.e("FriendsRequestFragment", "Error loading user details", e)
                addUserToScrollView(container, "Error loading name", "nophoto", userId, isReceived)
            }
    }

    private fun addUserToScrollView(container: LinearLayout, userName: String, profilePhotoURL: String, userId: String, isReceived: Boolean) {
        val userLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 50 // Establece el margen inferior
            }
        }

        // Layout para la imagen de perfil
        val profilePhotoLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // ImageView para la imagen de perfil
        val profilePhotoView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                250, // Tamaño deseado de la imagen de perfil
                250
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginEnd = 30 // Ajusta el margen entre la imagen y el nombre de usuario
            }
        }
        Glide.with(requireContext())
            .load(profilePhotoURL)
            .into(profilePhotoView)

        // TextView para el nombre de usuario
        val userNameView = TextView(context).apply {
            text = userName
            textSize = 20f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        userNameView.setTextColor(getResources().getColor(R.color.darkGreen))

        // Agregar la imagen de perfil y el nombre de usuario al layout de la imagen de perfil
        profilePhotoLayout.addView(profilePhotoView)
        profilePhotoLayout.addView(userNameView)

        // Agregar el layout de la imagen de perfil al layout de usuario
        userLayout.addView(profilePhotoLayout)

        // Layout para los botones
        val buttonLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
            }
        }

        if (isReceived) {
            //Boton de aceptar
            val acceptButton = Button(context).apply {
                text = "ACEPTAR"
                background = ContextCompat.getDrawable(context, R.drawable.custom_button_background_2)
                setOnClickListener { manageFriendRequest(userId, true) }
            }
            acceptButton.setTextColor(getResources().getColor(R.color.ivory))

            //Boton de rechazar
            val rejectButton = Button(context).apply {
                text = "RECHAZAR"
                background = ContextCompat.getDrawable(context, R.drawable.custom_button_background_2)
                setOnClickListener { manageFriendRequest(userId, false) }
            }
            rejectButton.setTextColor(getResources().getColor(R.color.ivory))

            // Agregar los botones al layout
            buttonLayout.addView(acceptButton)
            buttonLayout.addView(rejectButton)
        } else {
            //Rechazar si se envió por error
            val cancelButton = Button(context).apply {
                text = "CANCELAR"
                background = ContextCompat.getDrawable(context, R.drawable.custom_button_background_2)
                setOnClickListener { cancelFriendRequest(userId) }
            }
            cancelButton.setTextColor(getResources().getColor(R.color.ivory))

            // Agregar el botón al layout
            buttonLayout.addView(cancelButton)
        }

        // Agregar el layout de los botones al layout de usuario
        userLayout.addView(buttonLayout)

        // Agregar el layout de usuario al contenedor principal
        container.addView(userLayout)
    }


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
            refreshFriendRequests(requireView())
            Log.d("FriendsRequestFragment", "Friend request managed successfully.")
        }.addOnFailureListener { e ->
            Log.e("FriendsRequestFragment", "Error managing friend request", e)
        }
    }



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
            refreshFriendRequests(requireView())
            Log.d("FriendsRequestFragment", "Friend request cancelled successfully.")
        }.addOnFailureListener { e ->
            Log.e("FriendsRequestFragment", "Error cancelling friend request", e)
        }
    }


}
