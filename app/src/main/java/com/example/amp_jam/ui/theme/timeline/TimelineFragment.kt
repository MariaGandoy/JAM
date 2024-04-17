package com.example.amp_jam.ui.theme.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amp_jam.Post
import com.example.amp_jam.R
import com.example.amp_jam.RecyclerAdapter

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
        mAdapter.RecyclerAdapter(getPosts(), requireContext())
        mRecyclerView.adapter = mAdapter
    }

    private fun getPosts(): MutableList<Post>{
        var posts:MutableList<Post> = ArrayList()
        posts.add(Post("Cumpleaños", "04/07/24", "EVENT", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg", ""))
        posts.add(Post("", "04/07/24", "PHOTO", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg", ""))
        posts.add(Post("", "04/07/24", "SONG", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg", "https://open.spotify.com/intl-es/track/59xD5osEFsaNt5PXfIKUnX?si=2476b039634943fe"))
        posts.add(Post("", "04/07/24", "ALERT", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg", ""))
        return posts
    }
}