package com.example.amp_jam.ui.theme.timeline

import android.content.Intent
import android.os.Bundle
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
        val currentUser = auth.currentUser

        // Fetch posts asynchronously and set up the adapter when posts are available
        if (currentUser != null) {
            getPosts(currentUser) { posts ->
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

    /* Get posts from coroutine */
    private fun getPosts(currentUser: FirebaseUser, callback: (MutableList<Post>) -> Unit) {
        val database = FirebaseFirestore.getInstance()
        val posts = mutableListOf<Post>()

        lifecycleScope.launch {
            // Get my posts
            val userPosts = getUserPosts(currentUser.uid, database)
            if (userPosts != null) {
                posts.addAll(userPosts)
            }

            // Get friends posts
            val friends = getFriends(currentUser.uid, database)
            val friendPosts = mutableListOf<Post>()
            for (friendId in friends) {
                val postsForFriend = getUserPosts(friendId, database)
                if (postsForFriend != null) {
                    friendPosts.addAll(postsForFriend)
                }
            }

            posts.addAll(friendPosts)
            callback(posts)
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
                    .get()
                    .await()

            for (postDocument in postsSnapshot) {
                val postData = postDocument.data

                // Get post location data
                val lugarPost = postData["lugar"] as HashMap<*, *>
                val latitude = lugarPost["latitude"] as Double
                val longitude = lugarPost["longitude"] as Double

                val user = getUser(userId, database)
                if (user != null) {
                    posts.add(Post(postData["titulo"], postData["fecha"], postData["tipo"], user, postData["foto"], postData["song"], LatLng(latitude, longitude)))
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

