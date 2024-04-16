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
        posts.add(Post("Jorge ha compartido una imagen desde la Torre de Hércules", "Hace 5 seg", "EVENT", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg"))
        posts.add(Post("Jorge Copy ha compartido una imagen desde la Torre de Hércules", "Hace 5 seg","EVENT", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg"))
        posts.add(Post("María ha creado el evento Cumpleaños en Pub Rochester", "Hace 10 seg", "EVENT","Matthew Michael Murdock", "https://cursokotlin.com/wp-content/uploads/2017/07/daredevil.jpg"))
        posts.add(Post("María Copy ha creado el evento Cumpleaños en Pub Rochester", "Hace 10 seg", "EVENT","Matthew Michael Murdock", "https://cursokotlin.com/wp-content/uploads/2017/07/daredevil.jpg"))
        posts.add(Post("Joel está en Juan Flórez", "Hace 3 mins", "James Howlett", "EVENT","https://cursokotlin.com/wp-content/uploads/2017/07/logan.jpeg"))
        posts.add(Post("Joel Copy está en Juan Flórez", "Hace 3 mins", "EVENT","James Howlett", "https://cursokotlin.com/wp-content/uploads/2017/07/logan.jpeg"))
        posts.add(Post("Juan está en María Pita", "Hace 1 h", "EVENT","Bruce Wayne", "https://cursokotlin.com/wp-content/uploads/2017/07/batman.jpg"))
        posts.add(Post("Juan Copy está en María Pita", "Hace 1 h", "EVENT","Bruce Wayne", "https://cursokotlin.com/wp-content/uploads/2017/07/batman.jpg"))
        posts.add(Post("Hilda ha compartido una foto", "Hace 2 h", "EVENT","Thor Odinson", "https://cursokotlin.com/wp-content/uploads/2017/07/thor.jpg"))
        posts.add(Post("Hilda Copy ha compartido una foto", "Hace 2 h", "EVENT","Thor Odinson", "https://cursokotlin.com/wp-content/uploads/2017/07/thor.jpg"))
        return posts
    }
}