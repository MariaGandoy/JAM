package com.example.amp_jam.ui.theme.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
                val profilePhoto = document.getBlob("")
                addUserToScrollView(container, userName, userId, isReceived)
            }
            .addOnFailureListener { e ->
                Log.e("FriendsRequestFragment", "Error loading user details", e)
                addUserToScrollView(container, "Error loading name", userId, isReceived)
            }
    }

    private fun addUserToScrollView(container: LinearLayout, userName: String, userId: String, isReceived: Boolean) {
        val userLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val userNameView = TextView(context).apply {
            text = userName
            textSize = 20f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
        }
        userNameView.setTextColor(getResources().getColor(R.color.darkGreen))

        userLayout.addView(userNameView)

        if (isReceived) {
            //Boton de aceptar
            val acceptButton = Button(context).apply {
                text = "✔"
                background = ContextCompat.getDrawable(context, R.drawable.custom_button_background)
                setOnClickListener { manageFriendRequest(userId, true) }
            }
            acceptButton.setTextColor(getResources().getColor(R.color.ivory))

            //Boton de rechazar
            val rejectButton = Button(context).apply {
                text = "✘"
                background = ContextCompat.getDrawable(context, R.drawable.custom_button_background)
                setOnClickListener { manageFriendRequest(userId, false) }
            }
            rejectButton.setTextColor(getResources().getColor(R.color.ivory))

            userLayout.addView(acceptButton)
            userLayout.addView(rejectButton)
        } else {
            //Rechazar si se envió por error
            val cancelButton = Button(context).apply {
                text = "✘"
                background = ContextCompat.getDrawable(context, R.drawable.custom_button_background)
                setOnClickListener { cancelFriendRequest(userId) }
            }
            cancelButton.setTextColor(getResources().getColor(R.color.ivory))


            userLayout.addView(cancelButton)
        }

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
