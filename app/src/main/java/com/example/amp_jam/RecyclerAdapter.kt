package com.example.amp_jam

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var posts: MutableList<Post>  = ArrayList()
    private lateinit var context: Context

    fun RecyclerAdapter(posts : MutableList<Post>, context: Context) {
        this.posts = posts
        this.context = context
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = posts[position]
        holder.bind(item, context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.timeline_posts, parent, false))
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val postText = view.findViewById(R.id.postText) as TextView
        private val postTime = view.findViewById(R.id.postTime) as TextView
        private val userAvatar = view.findViewById(R.id.userAvatar) as ImageView

        fun bind(post:Post, context: Context){
            postText.text = post.text
            postTime.text = post.time

            val drawableName = "sample_user"
            val resourceId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
            userAvatar.setImageResource(resourceId)
        }
    }
}