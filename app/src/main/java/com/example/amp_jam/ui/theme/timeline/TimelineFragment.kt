package com.example.amp_jam.ui.theme.timeline

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amp_jam.Post
import com.example.amp_jam.R
import com.example.amp_jam.RecyclerAdapter
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

        return view
    }

    private fun setUpRecyclerView(view: View) {
        mRecyclerView = view.findViewById<RecyclerView>(R.id.postsList)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch posts asynchronously and set up the adapter when posts are available
        getPosts { posts ->
            mAdapter.RecyclerAdapter(posts, requireContext())
            mRecyclerView.adapter = mAdapter
        }
    }

    private fun getPosts(callback: (MutableList<Post>) -> Unit) {
        var posts:MutableList<Post> = ArrayList()
        val database = FirebaseFirestore.getInstance()

        // Some test data
        posts.add(Post("Cumpleaños", "04/07/24", "EVENT", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg", ""))
        posts.add(Post("", "04/07/24", "PHOTO", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg", ""))
        posts.add(Post("", "04/07/24", "SONG", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg", "https://open.spotify.com/intl-es/track/59xD5osEFsaNt5PXfIKUnX?si=2476b039634943fe"))
        posts.add(Post("", "04/07/24", "ALERT", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg", ""))

        database.collection("usuarios").get()
            .addOnSuccessListener { usuariosResult ->
                val fetchCount = usuariosResult.size()
                var fetchedCount = 0

                for (usuarioDocument in usuariosResult) {
                    val usuarioId = usuarioDocument.id

                    database.collection("usuarios").document(usuarioId)
                        .collection("posts").get()
                        .addOnSuccessListener { postsResult ->
                            for (postDocument in postsResult) {
                                val postData = postDocument.data

                                posts.add(Post(postData["titulo"], postData["fecha"], postData["tipo"], postData["user"], null, null))
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