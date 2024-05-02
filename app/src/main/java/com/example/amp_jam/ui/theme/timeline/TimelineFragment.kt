package com.example.amp_jam.ui.theme.timeline

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TimelineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimelineFragment : Fragment() {
    lateinit var mRecyclerView: RecyclerView
    val mAdapter: RecyclerAdapter = RecyclerAdapter()

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

        var progressBar = view.findViewById<ProgressBar>(androidx.appcompat.R.id.progress_circular)
        progressBar.visibility = View.VISIBLE

        // Fetch posts asynchronously and set up the adapter when posts are available
        getPosts { posts ->
            mAdapter.RecyclerAdapter(posts, requireContext(), findNavController())
            mRecyclerView.adapter = mAdapter
            progressBar.visibility = View.GONE
        }
    }

    private fun getPosts(callback: (MutableList<Post>) -> Unit) {
        var posts:MutableList<Post> = ArrayList()
        val database = FirebaseFirestore.getInstance()

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            database.collection("usuarios")
                .document(currentUser.uid)
                .collection("friends")
                .get()
                .addOnSuccessListener { usuariosResult ->
                    val fetchCount = usuariosResult.size()
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
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors that may occur
                }
        }
    }
}

