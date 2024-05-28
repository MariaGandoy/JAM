package com.example.amp_jam.ui.theme.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.amp_jam.ChangeUserDataActivity
import com.example.amp_jam.GroupCreateDialog
import com.example.amp_jam.ListOfFriendsActivity
import com.example.amp_jam.Post
import com.example.amp_jam.R
import com.example.amp_jam.RecyclerAdapter
import com.example.amp_jam.SettingsActivity
import com.example.amp_jam.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


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

    val mAdapter: RecyclerAdapter = RecyclerAdapter()
    lateinit var mRecyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var customMessage: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setUpSettings(view)
        loadUserProfileData(view)
        setupAddGroupsButton(view)
        setupEditProfileButton(view)

        val settingsButton = view.findViewById<ImageButton>(R.id.configurations)
        settingsButton.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun setUpSettings(view: View) {
        val settingsButton = view.findViewById<ImageButton>(R.id.configurations)
        settingsButton.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        view?.let { loadUserProfileData(it) }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.cancel()
    }

    private fun loadUserProfileData(view: View) {
        val currentUser = auth.currentUser
        val userNameTextView = view.findViewById<TextView>(R.id.textView3)
        val lastNameTextView = view.findViewById<TextView>(R.id.textView4)
        val profileImageView = view.findViewById<ImageView>(R.id.imageView3)
        val friendsCountTextView = view.findViewById<TextView>(R.id.textView5)
        progressBar = view.findViewById<ProgressBar>(androidx.appcompat.R.id.progress_circular)
        customMessage = view.findViewById<TextView>(R.id.customMessage)

        if (currentUser != null) {
            // Load user profile picture and name
            firestore.collection("usuarios").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (isAdded) {
                        val name = document.getString("name") ?: "@usuario"
                        val lastName = document.getString("lastName") ?: "Apellidos"
                        val photo = document.getString("photo")

                        userNameTextView.text = name
                        lastNameTextView.text = lastName
                        Glide.with(requireContext())
                            .load(photo)
                            .placeholder(R.drawable.sample_user)
                            .into(profileImageView)
                    }
                }
                .addOnFailureListener { exception ->
                    if (isAdded) {
                        Log.d("ProfileFragment", "get failed with ", exception)
                        userNameTextView.text = "@usuario"
                        lastNameTextView.text = "Apellidos"
                    }
                }

            // Load posts
            lifecycleScope.launch {
                val user = getUser(currentUser.uid, firestore)
                setUpRecyclerView(view, user)
            }

            // Load friends count
            firestore.collection("usuarios").document(currentUser.uid).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    if (isAdded) {
                        friendsCountTextView.text = "${documents.size()} amigos"
                        friendsCountTextView.setOnClickListener {
                            val intent = Intent(activity, ListOfFriendsActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        Log.d("ProfileFragment", "Error loading friends count", it)
                        friendsCountTextView.text = "0 amigos"
                    }
                }
        } else {
            if (isAdded) {
                Toast.makeText(this.context, "isAdded", Toast.LENGTH_SHORT).show()
                userNameTextView.text = "@usuario"
                lastNameTextView.text = "Apellidos"
                friendsCountTextView.text = "0 amigos"
                customMessage.visibility = View.VISIBLE
                customMessage.text = "No tienes actividad reciente"
            }
        }
    }

    private fun setupEditProfileButton(view: View) {
        val editProfileButton = view.findViewById<Button>(R.id.editProfile)
        editProfileButton.setOnClickListener {
            val intent = Intent(activity, ChangeUserDataActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpRecyclerView(view: View, user: User?) {
        mRecyclerView = view.findViewById<RecyclerView>(R.id.postsList)
        // mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        progressBar.visibility = View.VISIBLE
        customMessage.visibility = View.GONE

        // Fetch posts asynchronously and set up the adapter when posts are available
        getPosts(user) { posts ->
            posts.sortWith(compareByDescending(nullsFirst()) { it.timestamp as Comparable<Any>? })
            mAdapter.RecyclerAdapter(posts, requireContext(), findNavController())
            mRecyclerView.adapter = mAdapter
            progressBar.visibility = View.GONE
        }
    }

    private fun getPosts(user: User?, callback: (MutableList<Post>) -> Unit) {
        var posts: MutableList<Post> = ArrayList()

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            firestore.collection("usuarios").document(currentUser.uid).collection("posts")
                .get()
                .addOnSuccessListener { documents ->
                    if (isAdded) {
                        if (!documents.isEmpty) {
                            for (postDocument in documents) {
                                val postData = postDocument.data

                                // Get post location data
                                val lugarPost = postData["lugar"] as HashMap<*, *>
                                val latitude = lugarPost["latitude"] as Double
                                val longitude = lugarPost["longitude"] as Double

                                posts.add(Post(postData["titulo"], postData["fecha"], postData["tipo"], user, postData["photo"], postData["song"], LatLng(latitude, longitude), postData["shareWith"], postData["timestamp"]))
                            }
                        } else {
                            customMessage.visibility = View.VISIBLE
                            customMessage.text = "No tienes actividad reciente"
                        }

                        callback(posts)
                    }
                }
                .addOnFailureListener { exception ->
                    if (isAdded) {
                        progressBar.visibility = View.GONE
                        customMessage.text = "Error al cargar los posts"
                        Log.d("ProfileFragment", "Error loading posts", exception)
                    }
                }
        }
    }

    private fun setupAddGroupsButton(view: View) {
        val addGroupsButton = view.findViewById<Button>(R.id.addGroups)
        addGroupsButton.setOnClickListener {
            val groupCreateDialog = GroupCreateDialog()
            groupCreateDialog.show(requireFragmentManager(), "GroupCreateDialog")
        }
    }

    private suspend fun getUser(userId: String, database: FirebaseFirestore): User? {
        return try {
            val document = database.collection("usuarios").document(userId).get().await()
            val name = document.getString("name")
            val photo = document.getString("photo")
            val email = document.getString("email")

            return User(name, userId, email, photo)
        } catch (e: Exception) {
            null
        }
    }

}
