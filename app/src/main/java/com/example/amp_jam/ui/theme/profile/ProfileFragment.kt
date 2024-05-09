package com.example.amp_jam.ui.theme.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.amp_jam.AddFriendsActivity
import com.example.amp_jam.ListOfFriendsActivity
import com.example.amp_jam.MainActivity
import com.example.amp_jam.Post
import com.example.amp_jam.R
import com.example.amp_jam.RecyclerAdapter
import com.google.android.gms.maps.model.LatLng
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
    private lateinit var locationSwitch: Switch
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

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

        setupLogOut(view)
        setupAddFriends(view)
        loadUserProfileData(view)

        Log.d("Debugeandoklk", "llego Profile")
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
        val friendsCountTextView = view.findViewById<TextView>(R.id.textView5)
        progressBar = view.findViewById<ProgressBar>(androidx.appcompat.R.id.progress_circular)
        customMessage = view.findViewById<TextView>(R.id.customMessage)

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
                    userNameTextView.text = name ?: "@usuario"
                }
                .addOnFailureListener { exception ->
                    Log.d("ProfileFragment", "get failed with ", exception)
                    userNameTextView.text = "@usuario"
                }

            // Load posts
            setUpRecyclerView(view)

            firestore.collection("usuarios").document(currentUser.uid).collection("friends")
                .get()
                .addOnSuccessListener { documents ->

                    friendsCountTextView.text = "${documents.size()} amigos"
                    friendsCountTextView.setOnClickListener {
                        val intent = Intent(activity, ListOfFriendsActivity::class.java)
                        startActivity(intent)
                    }

                }
                .addOnFailureListener {
                    Log.d("ProfileFragment", "Error loading friends count", it)
                    friendsCountTextView.text = "0 amigos" // Si no hay lsita o hay un error 0
                }
        } else {
            userNameTextView.text = "@usuario"
            friendsCountTextView.text = "0 amigos"
            customMessage.visibility = View.VISIBLE
            customMessage.text = "No tienes actividad reciente"
        }
    }

    private fun setUpRecyclerView(view: View) {
        mRecyclerView = view.findViewById<RecyclerView>(R.id.postsList)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        progressBar.visibility = View.VISIBLE
        customMessage.visibility = View.GONE

        // Fetch posts asynchronously and set up the adapter when posts are available
        getPosts { posts ->
            mAdapter.RecyclerAdapter(posts, requireContext(), findNavController())
            mRecyclerView.adapter = mAdapter
            progressBar.visibility = View.GONE
        }
    }

    private fun getPosts(callback: (MutableList<Post>) -> Unit) {
        var posts:MutableList<Post> = ArrayList()

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            firestore.collection("usuarios").document(currentUser.uid).collection("posts")
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (postDocument in documents) {
                            val postData = postDocument.data

                            // Get post location data
                            val lugarPost = postData["lugar"] as HashMap<*,*>
                            val latitude = lugarPost["latitude"] as Double
                            val longitude = lugarPost["longitude"] as Double

                            posts.add(Post(postData["titulo"], postData["fecha"], postData["tipo"], postData["user"], null, null, LatLng(latitude, longitude)))
                        }
                    } else {
                        customMessage.visibility = View.VISIBLE
                        customMessage.text = "No tienes actividad reciente"
                    }

                    callback(posts)
                }
                .addOnFailureListener {
                    // Handle any errors that may occur
                    progressBar.visibility = View.GONE
                    customMessage.text = "Error al cargar los posts"
                    Log.d("ProfileFragment", "Error loading posts", it)
                }
        }
    }

}
