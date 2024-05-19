package com.example.amp_jam.ui.theme.friends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.amp_jam.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private var currentUserUid: String? = null
    private var sentFriendUserIds: MutableSet<String> = mutableSetOf()
    private var friendUserIds: MutableSet<String> = mutableSetOf()
    private var receivedFriendUserIds: MutableSet<String> = mutableSetOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_friends, container, false)


        firestore = FirebaseFirestore.getInstance()
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val searchEditText = view.findViewById<EditText>(R.id.textInputLayout2)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // He puesto esto porque si no lo pongo me da un error en el Object
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // He puesto esto porque si no lo pongo me da un error en el Object
            }

            override fun afterTextChanged(s: Editable?) {
                // Buscar
                loadAllUsers(view, s.toString())
            }
        })

        loadSentFriendRequests {
            loadReceivedFriendRequests {
                loadFriends {
                    loadAllUsers(view)
                }
            }
        }

        return view
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
                    Log.e("AddFriendsFragment", "Error loading friends", it)
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
                    Log.e("AddFriendsFragment", "Error loading sent friend requests", it)
                    onComplete() // Continuar aunque haya un error.
                }
        } ?: onComplete() // Ejecutar onComplete si currentUserUid es nulo.
    }

    private fun loadReceivedFriendRequests(onComplete: () -> Unit) {
        currentUserUid?.let { uid ->
            firestore.collection("usuarios").document(uid).collection("receive_friend")
                .get()
                .addOnSuccessListener { documents ->
                    receivedFriendUserIds.clear()
                    for (document in documents) {
                        receivedFriendUserIds.add(document.id)
                    }
                    onComplete()
                }
                .addOnFailureListener {
                    Log.e("AddFriendsFragment", "Error loading received friend requests", it)
                    onComplete()
                }
        } ?: onComplete()
    }

    private fun loadAllUsers(view: View, filter: String? = null) {
        firestore.collection("usuarios")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    view.findViewById<LinearLayout>(R.id.usersContainer).removeAllViews()
                    for (document in documents) {
                        val userId = document.id
                        val userName = document.getString("name") ?: "Unknown"

                        if (userId != currentUserUid && !sentFriendUserIds.contains(userId) && !receivedFriendUserIds.contains(userId) && !friendUserIds.contains(userId)) {
                            // Aplicar búsqueda (si busco algo claro)
                            if (filter == null || userName.contains(filter, ignoreCase = true)) {
                                addUserToList(view, userName, userId)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("AddFriendsFragment", "Error loading users", exception)
            }
    }


    private fun addUserToList(view: View, userName: String, userId: String) {
        val userContainer = view.findViewById<LinearLayout>(R.id.usersContainer)

        val textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0, // Ancho como 0, pero con peso
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.7f // Peso menor para dar más espacio al botón
            )
            text = userName + "\n"
            textSize = if (userName.length > 20) 16f else 20f
            setPadding(16, 16, 16, 16)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.darkGreen))
        }

        // Button para agregar a amigos
        val addButton = Button(context).apply {
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
        addButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.ivory))

        // Crear un nuevo LinearLayout para contener el TextView y el Button
        val userLayout = LinearLayout(context).apply {
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
                    Log.d("AddFriendsFragment", "Friend added to send_friend successfully: $friendUserId")
                    loadSentFriendRequests {
                        loadAllUsers(requireView()) // Recargar usuarios después de añadir a un amigo
                    }
                }
                .addOnFailureListener {
                    Log.e("AddFriendsFragment", "Failed to add to send_friend", it)
                }
            // Añadir a receive_friend del otro usuario
            firestore.collection("usuarios").document(friendUserId)
                .collection("receive_friend").document(currentUser.uid).set(mapOf("added" to true))
                .addOnSuccessListener {
                    Log.d("AddFriendsFragment", "Friend added to receive_friend successfully: ${currentUser.uid}")
                }
                .addOnFailureListener {
                    Log.e("AddFriendsFragment", "Failed to add to receive_friend", it)
                }
        }
    }
}