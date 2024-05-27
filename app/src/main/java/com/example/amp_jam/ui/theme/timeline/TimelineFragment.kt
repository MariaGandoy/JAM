package com.example.amp_jam.ui.theme.timeline

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amp_jam.Post
import com.example.amp_jam.R
import com.example.amp_jam.RecyclerAdapter
import com.example.amp_jam.SettingsActivity
import com.example.amp_jam.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query

/**
 * A simple [Fragment] subclass.
 * Use the [TimelineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimelineFragment : Fragment() {
    lateinit var mRecyclerView: RecyclerView

    val mAdapter: RecyclerAdapter = RecyclerAdapter()

    lateinit var progressBar: ProgressBar

    lateinit var customMessage: TextView

    private lateinit var currentUser: FirebaseUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.timeline, container, false)

        setUpSettings(view)
        setUpRecyclerView(view)

        return view
    }

    private fun setUpSettings(view: View) {
        val settingsButton = view.findViewById<ImageButton>(R.id.configurations)
        settingsButton.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpRecyclerView(view: View) {
        mRecyclerView = view.findViewById<RecyclerView>(R.id.postsList)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        progressBar = view.findViewById<ProgressBar>(androidx.appcompat.R.id.progress_circular)
        customMessage = view.findViewById<TextView>(R.id.customMessage)
        progressBar.visibility = View.VISIBLE

        val auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        // Fetch posts asynchronously and set up the adapter when posts are available
        if (currentUser != null) {
            getPosts(currentUser) { posts ->
                if (isAdded) { // Check if Fragment is still added to its activity
                    posts.sortWith(compareByDescending(nullsFirst()) { it.timestamp as Comparable<Any>? })
                    mAdapter.RecyclerAdapter(posts, requireContext(), findNavController())
                    mRecyclerView.adapter = mAdapter
                    progressBar.visibility = View.GONE

                    // Check if posts are available
                    if (posts.isEmpty()) {
                        customMessage.visibility = View.VISIBLE
                        customMessage.text = "No hay ningún post"
                    }
                }
            }
        }
    }

    /* Get posts from coroutine */
    private fun getPosts(currentUser: FirebaseUser, callback: (MutableList<Post>) -> Unit) {
        val database = FirebaseFirestore.getInstance()

        lifecycleScope.launch {
            val posts = mutableListOf<Post>()

            // Get my posts
            val userPosts = getUserPosts(currentUser.uid, database)
            if (userPosts != null) {
                posts.addAll(userPosts)
            }

            // Get friends posts
            val friends = getFriends(currentUser.uid, database)
            for (friendId in friends) {
                val postsForFriend = getUserPosts(friendId, database)
                if (postsForFriend != null) {
                    posts.addAll(postsForFriend)
                }
            }

            if (isAdded) { // Check if Fragment is still added to its activity before updating UI
                callback(posts)
            }
        }
    }



    private suspend fun getUserPosts(userId: String, database: FirebaseFirestore): MutableList<Post>? {
        return try {
            val posts = mutableListOf<Post>()
            val postsSnapshot =
                database
                    .collection("usuarios")
                    .document(userId)
                    .collection("posts")
                    .orderBy("fecha", Query.Direction.ASCENDING)
                    .get()
                    .await()

            val user = getUser(userId, database)
            for (postDocument in postsSnapshot) {
                val postData = postDocument.data

                // Get post location data
                val lugarPost = postData["lugar"] as HashMap<*, *>
                val latitude = lugarPost["latitude"] as Double
                val longitude = lugarPost["longitude"] as Double

                // Get post restrictions
                val shareWith = postData["share"] as? List<String> ?: emptyList()

                if (user != null) {
                    if (userId == currentUser.uid || shareWith.isEmpty() || shareWith.contains(currentUser.uid)) {
                        posts.add(Post(postData["titulo"], postData["fecha"], postData["tipo"], user, postData["photo"], postData["song"], LatLng(latitude, longitude), postData["shareWith"], postData["timestamp"]))
                    }
                }
            }

            posts
        } catch (e: Exception) {
            null
        }
    }

        private suspend fun getFriends(userId: String, database: FirebaseFirestore): List<String> {
        return try {
            val friends = mutableListOf<String>()
            val friendsSnapshot = database
                .collection("usuarios")
                .document(userId)
                .collection("friends")
                .get()
                .await()

            for (friendDocument in friendsSnapshot) {
                friends.add(friendDocument.id)
            }

            return friends
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getUser(userId: String, database: FirebaseFirestore): User? {
        return try {
            val document = database
                .collection("usuarios")
                .document(userId)
                .get()
                .await()

            val name = document.getString("name")
            val photo = document.getString("photo")
            val email = document.getString("email")

            return User(name, userId, email, photo)
        } catch (e: Exception) {
            null
        }
    }
}

