package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TimelineActivity : ComponentActivity() {
    lateinit var mRecyclerView : RecyclerView
    val mAdapter : RecyclerAdapter = RecyclerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timeline)

        setUpRecyclerView()

        setUpSeeMapButton()

        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }

    private fun setUpSeeMapButton() {
        val addBtn = findViewById<Button>(R.id.verMapaButton)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[TimelineActivity] Click SEE MAP button")
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpRecyclerView() {
        mRecyclerView = findViewById<RecyclerView>(R.id.postsList)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter.RecyclerAdapter(getPosts(), this)
        mRecyclerView.adapter = mAdapter
    }

    private fun getPosts(): MutableList<Post>{
        var posts:MutableList<Post> = ArrayList()
        posts.add(Post("Jorge ha compartido una imagen desde la Torre de Hércules", "Hace 5 seg", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg"))
        posts.add(Post("Jorge Copy ha compartido una imagen desde la Torre de Hércules", "Hace 5 seg", "Peter Parker", "https://cursokotlin.com/wp-content/uploads/2017/07/spiderman.jpg"))
        posts.add(Post("María ha creado el evento Cumpleaños en Pub Rochester", "Hace 10 seg", "Matthew Michael Murdock", "https://cursokotlin.com/wp-content/uploads/2017/07/daredevil.jpg"))
        posts.add(Post("María Copy ha creado el evento Cumpleaños en Pub Rochester", "Hace 10 seg", "Matthew Michael Murdock", "https://cursokotlin.com/wp-content/uploads/2017/07/daredevil.jpg"))
        posts.add(Post("Joel está en Juan Flórez", "Hace 3 mins", "James Howlett", "https://cursokotlin.com/wp-content/uploads/2017/07/logan.jpeg"))
        posts.add(Post("Joel Copy está en Juan Flórez", "Hace 3 mins", "James Howlett", "https://cursokotlin.com/wp-content/uploads/2017/07/logan.jpeg"))
        posts.add(Post("Juan está en María Pita", "Hace 1 h", "Bruce Wayne", "https://cursokotlin.com/wp-content/uploads/2017/07/batman.jpg"))
        posts.add(Post("Juan Copy está en María Pita", "Hace 1 h", "Bruce Wayne", "https://cursokotlin.com/wp-content/uploads/2017/07/batman.jpg"))
        posts.add(Post("Hilda ha compartido una foto", "Hace 2 h", "Thor Odinson", "https://cursokotlin.com/wp-content/uploads/2017/07/thor.jpg"))
        posts.add(Post("Hilda Copy ha compartido una foto", "Hace 2 h", "Thor Odinson", "https://cursokotlin.com/wp-content/uploads/2017/07/thor.jpg"))
        return posts
    }
}