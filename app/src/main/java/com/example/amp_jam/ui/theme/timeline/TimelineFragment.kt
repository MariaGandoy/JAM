package com.example.amp_jam.ui.theme.timeline

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amp_jam.Post
import com.example.amp_jam.R
import com.example.amp_jam.RecyclerAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        setUpRecyclerView(view)
        Log.d("Debugeandoklk", "llego Timeline")
        return view
    }

    private fun setUpRecyclerView(view: View) {
        mRecyclerView = view.findViewById<RecyclerView>(R.id.postsList)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        progressBar = view.findViewById<ProgressBar>(androidx.appcompat.R.id.progress_circular)
        customMessage = view.findViewById<TextView>(R.id.customMessage)
        progressBar.visibility = View.VISIBLE
        customMessage.visibility = View.GONE

        // Fetch posts asynchronously and set up the adapter when posts are available
        getPosts { posts ->
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

    private fun getPosts(callback: (MutableList<Post>) -> Unit) {
        var posts:MutableList<Post> = ArrayList()
        val database = FirebaseFirestore.getInstance()

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            database.collection("usuarios")
                .document(currentUser.uid).collection("posts")
                .get()
                .addOnSuccessListener { myResult ->
                    for (postDocument in myResult) {
                        val postData = postDocument.data

                        // Get post location data
                        val lugarPost = postData["lugar"] as HashMap<*,*>
                        val latitude = lugarPost["latitude"] as Double
                        val longitude = lugarPost["longitude"] as Double

                        posts.add(Post(postData["titulo"], postData["fecha"], postData["tipo"], postData["user"], null, null, LatLng(latitude, longitude)))
                    }

                    database.collection("usuarios")
                        .document(currentUser.uid)
                        .collection("friends")
                        .get()
                        .addOnSuccessListener { usuariosResult ->
                            val fetchCount = usuariosResult.size()
                            if (fetchCount == 0) callback(posts)
                            var fetchedCount = 0

                            for (usuarioDocument in usuariosResult) {
                                database.collection("usuarios")
                                    .document(usuarioDocument.id)
                                    .collection("posts")
                                    .get()
                                    .addOnSuccessListener { postsResult ->
                                        for (postDocument in postsResult) {
                                            val postData = postDocument.data

                                            // Get post location data
                                            val lugarPost = postData["lugar"] as HashMap<*,*>
                                            val latitude = lugarPost["latitude"] as Double
                                            val longitude = lugarPost["longitude"] as Double

                                            posts.add(Post(postData["titulo"], postData["fecha"], postData["tipo"], postData["user"], null, null, LatLng(latitude, longitude)))
                                        }

                                        fetchedCount++
                                        // Check if all posts have been fetched
                                        if (fetchedCount == fetchCount) {
                                            // Invoke the callback with the fetched posts
                                            callback(posts)
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        // Handle any errors that may occur
                                        progressBar.visibility = View.GONE
                                        customMessage.text = "Error cargando posts"
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors that may occur
                            progressBar.visibility = View.GONE
                            customMessage.text = "Error cargando posts"
                        }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors that may occur
                    progressBar.visibility = View.GONE
                    customMessage.text = "Error cargando posts"
                }
        }
    }
}

