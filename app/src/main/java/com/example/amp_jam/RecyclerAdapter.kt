package com.example.amp_jam

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.BitmapDescriptorFactory

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
        private val postImage = view.findViewById(R.id.postImage) as ImageView
        private val userAvatar = view.findViewById(R.id.userAvatar) as ImageView

        fun bind(post:Post, context: Context){
            when (post.type) {
                "EVENT" -> {
                    createEventPost(post, context)
                }
                "PHOTO" -> {
                    createPhotoPost(post, context)
                }
                "SONG" -> {
                    createSongPost(post, context)
                }
                "ALERT" -> {
                    createAlertPost(post, context)
                }
            }
        }

        private fun createEventPost(post:Post, context: Context) {
            // Set event data
            postText.text = Html.fromHtml("<b>${post.user}</b> creo el evento <b>${post.title}</b> programado para el <b>${post.date}</b>")
            postTime.visibility = View.GONE // TODO: change for a mark of when the post was created

            // Set post user profile pic (TODO: change for user pic)
            val drawableName = "sample_user"
            val resourceId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
            userAvatar.setImageResource(resourceId)

            // Set post image (TODO: change for shared pic)
            if (post.photo != null) {
                postImage.visibility = View.VISIBLE
                postImage.setImageResource(resourceId)
            } else {
                postImage.visibility = View.GONE
            }
        }

        private fun createPhotoPost(post:Post, context: Context) {
            // Set photo data
            postText.text = Html.fromHtml("<b>${post.user}</b> compartió una foto")
            postTime.visibility = View.GONE // TODO: change for a mark of when the post was created

            // Set post user profile pic (TODO: change for user pic)
            val drawableName = "sample_user"
            val resourceId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
            userAvatar.setImageResource(resourceId)

            // Set post image (TODO: change for shared pic)
            if (post.photo != null) {
                postImage.visibility = View.VISIBLE
                postImage.setImageResource(resourceId)
            } else {
                postImage.visibility = View.GONE
            }
        }

        private fun createSongPost(post:Post, context: Context) {
            // Set song data
            val text = "<b>${post.user}</b> compartió la canción <a href='${post.song}'>${post.song}</a>"
            postText.text = Html.fromHtml(text)
            postText.movementMethod = LinkMovementMethod.getInstance()
            postTime.visibility = View.GONE // TODO: change for a mark of when the post was created

            // Set post user profile pic (TODO: change for user pic)
            val drawableName = "sample_user"
            val resourceId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
            userAvatar.setImageResource(resourceId)

            postImage.visibility = View.GONE
        }

        private fun createAlertPost(post:Post, context: Context) {
            // Set song data
            postText.text = Html.fromHtml("<b>${post.user}</b> creó una <b>alerta</b>")
            postTime.visibility = View.GONE // TODO: change for a mark of when the post was created

            val resourceId = context.resources.getIdentifier("warning", "drawable", context.packageName)
            userAvatar.setImageResource(resourceId)

            postImage.visibility = View.GONE
        }
    }

}