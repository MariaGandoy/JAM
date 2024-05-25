package com.example.amp_jam.ui.theme.friends


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.amp_jam.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class AddFriendsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private var currentUserUid: String? = null
    private var sentFriendUserIds: MutableSet<String> = mutableSetOf()
    private var friendUserIds: MutableSet<String> = mutableSetOf()
    private var receivedFriendUserIds: MutableSet<String> = mutableSetOf()
    private var lastVisible: DocumentSnapshot? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_friends, container, false)
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid


        val searchEditText = view.findViewById<EditText>(R.id.textInputLayout2)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                lastVisible = null
                loadAllUsers(view, s.toString().trim())
            }
        })

        setUpNotification(view)

        loadSentFriendRequests {
            loadReceivedFriendRequests {
                loadFriends {
                    loadAllUsers(view)
                    setupScrollListener(view)
                }
            }
        }

        return view
    }

    private fun setupScrollListener(view: View) {
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView3)
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val viewHeight = scrollView.height
            val contentHeight = scrollView.getChildAt(0).height
            if (!isFetching && scrollView.scrollY >= (contentHeight - viewHeight)) {
                loadAllUsers(view)
            }
        }
    }

    private fun setUpNotification(view: View) {
        val settingsButton = view.findViewById<Button>(R.id.btn_notis)

        settingsButton.setOnClickListener {
            val intent = Intent(activity, FriendsRequestFragment::class.java)
            startActivity(intent)
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

    private var isFetching = false

    private fun loadAllUsers(view: View, filter: String? = "") {
        if (isFetching) return
        isFetching = true

        var query = firestore.collection("usuarios").limit(14)
        if (lastVisible != null && filter.isNullOrEmpty()) {
            query = query.startAfter(lastVisible!!)
        } else {
            lastVisible = null
            view.findViewById<LinearLayout>(R.id.usersContainer).removeAllViews()
        }

        query.get()
            .addOnSuccessListener { documents ->
                if (isAdded) {
                    if (!documents.isEmpty) {
                        lastVisible = documents.documents[documents.size() - 1]

                        for (document in documents) {
                            val userId = document.id
                            val userName = (document.getString("name") ?: "Unknown").trim()
                            val profilePhotoURL = document.getString("photo")
                                ?: Uri.parse("android.resource://"
                                        + getActivity()?.getPackageName() +"/"+R.drawable.sample_user).toString()


                            if (userId != currentUserUid && !sentFriendUserIds.contains(userId) && !receivedFriendUserIds.contains(userId)
                                && !friendUserIds.contains(userId) && (filter.isNullOrEmpty() || userName.toLowerCase().contains(filter.trim().toLowerCase()))
                            ) {

                                if (filter == null || userName.contains(
                                        filter,
                                        ignoreCase = true
                                    )
                                ) {
                                    addUserToList(view, userName, profilePhotoURL, userId)
                                }
                            }
                        }
                    }
                    isFetching = false
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded) {
                    Log.d("AddFriendsFragment", "Error loading users", exception)
                    isFetching = false
                }
            }
    }


    private fun addUserToList(view: View, userName: String, photo: String, userId: String) {
        val userContainer = view.findViewById<LinearLayout>(R.id.usersContainer)

        // ImageView para la imagen de perfil
        val profilePhotoView = ImageView(context).apply {
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
            .load(photo)
            .into(profilePhotoView)

        val textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0, // Ancho como 0, pero con peso
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.7f // Peso menor para dar más espacio al botón
            )
            text = userName + "\n"
            textSize = if (userName.length > 25) 16f else 20f
            setPadding(16, 50, 16, 0)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        // Button para agregar a amigos
        val addButton = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100)
            text = "+"
            background =
                ContextCompat.getDrawable(context, R.drawable.custom_round_button_background)
            setOnClickListener {
                if (isAdded) {
                    addFriend(userId)
                }
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

            addView(profilePhotoView)
            addView(textView)
            addView(addButton)
        }

        userContainer.addView(userLayout)
    }

    private fun addFriend(friendUserId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && friendUserId != currentUser.uid && isAdded) {
            firestore.collection("usuarios").document(currentUser.uid)
                .collection("send_friend").document(friendUserId).set(mapOf("added" to true))
                .addOnSuccessListener {
                    Log.d(
                        "AddFriendsFragment",
                        "Friend added to send_friend successfully: $friendUserId"
                    )
                    if (isAdded) {
                        loadSentFriendRequests {
                            loadAllUsers(requireView()) // Recargar usuarios después de añadir a un amigo
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    if (isAdded) {
                        Log.e("AddFriendsFragment", "Failed to add to send_friend", exception)
                    }
                }

            firestore.collection("usuarios").document(friendUserId)
                .collection("receive_friend").document(currentUser.uid).set(mapOf("added" to true))
                .addOnSuccessListener {
                    if (isAdded) {
                        Log.d(
                            "AddFriendsFragment",
                            "Friend added to receive_friend successfully: ${currentUser.uid}"
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    if (isAdded) {
                        Log.e("AddFriendsFragment", "Failed to add to receive_friend", exception)
                    }
                }
        }
    }
}
