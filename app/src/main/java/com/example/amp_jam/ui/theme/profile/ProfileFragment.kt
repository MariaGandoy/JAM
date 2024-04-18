package com.example.amp_jam.ui.theme.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.amp_jam.AddFriendsActivity
import com.example.amp_jam.MainActivity
import com.example.amp_jam.Post
import com.example.amp_jam.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupLogOut(view)
        setupAddFriends(view)
        loadUserProfileData(view)

        return view
    }


    private fun setupLogOut(view: View) {
        val toolbarLogout = view.findViewById<ImageView>(R.id.toolbarLogout)
        toolbarLogout.setOnClickListener {
            Log.d("ProfileFragment", Firebase.auth.currentUser.toString())
            Firebase.auth.signOut()

            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()

            Log.d("ProfileFragment", Firebase.auth.currentUser.toString())
        }
    }


    private fun setupAddFriends(view: View) {
        val addBtn = view.findViewById<Button>(R.id.button5)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileFragment] Click ADD FRIENDS MENU EVENT button")
            val intent = Intent(activity, AddFriendsActivity::class.java)
            startActivity(intent)

        }
    }

    private fun loadUserProfileData(view: View) {
        val currentUser = auth.currentUser
        val userNameTextView = view.findViewById<TextView>(R.id.textView3)
        val profileImageView = view.findViewById<ImageView>(R.id.imageView3)
        val postsTextView = view.findViewById<TextView>(R.id.postsTextView)

        if (currentUser != null) {
            // Load user profile picture and name
            currentUser.photoUrl?.let {
                Glide.with(requireContext())
                    .load(it)
                    .placeholder(R.drawable.logo)
                    .into(profileImageView)
            } ?: profileImageView.setImageResource(R.drawable.logo)

            // Load user name
            firestore.collection("usuarios").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    userNameTextView.text = name ?: "@usuario10"
                }
                .addOnFailureListener { exception ->
                    Log.d("ProfileFragment", "get failed with ", exception)
                    userNameTextView.text = "@usuario10"
                }

            // Load posts
            firestore.collection("usuarios").document(currentUser.uid).collection("posts")
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val stringBuilder = StringBuilder()
                        for (document in documents) {
                            val title = document.getString("titulo") ?: "Sin título"
                            val date = document.getString("fecha") ?: "Fecha desconocida"
                            val tipo = document.getString("tipo") ?: "Tipo no especificado"
                            val user = document.getString("user") ?: "Usuario desconocido"
                            stringBuilder.append("Título: $title\nFecha: $date\nTipo: $tipo\nUsuario: $user\n\n")
                        }
                        postsTextView.text = stringBuilder.toString()
                    } else {
                        postsTextView.text = "No tienes actividad reciente"
                    }
                }
                .addOnFailureListener {
                    postsTextView.text = "Error al cargar los posts"
                    Log.d("ProfileFragment", "Error loading posts", it)
                }
        } else {
            userNameTextView.text = "@usuario10"
            postsTextView.text = "No tienes actividad reciente"
        }
    }






}
