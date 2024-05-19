package com.example.amp_jam

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var posts: MutableList<Post>  = ArrayList()
    private lateinit var context: Context
    private lateinit var navController: NavController

    fun RecyclerAdapter(posts : MutableList<Post>, context: Context, navController: NavController) {
        this.posts = posts
        this.context = context
        this.navController = navController
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = posts[position]
        holder.bind(item, context, navController)
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
        private val postSong = view.findViewById(R.id.postSong) as ImageView
        private val userAvatar = view.findViewById(R.id.userAvatar) as ImageView
        private val postOptions = view.findViewById<ImageView>(R.id.postOptions)

        fun bind(post:Post, context: Context, navController: NavController){
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

            postOptions.setOnClickListener {
                showPopupMenu(post, navController, context, it)
            }
        }

        private fun showPopupMenu(post:Post, navController: NavController, context: Context, anchor: View) {
            val popupMenu = PopupMenu(context, anchor)
            popupMenu.menuInflater.inflate(R.menu.map_post_options, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.seePostMap -> {
                        // Redirect to post in map
                        post.location?.let { location ->
                            SharedPreferencesHelper.setLastCords(context, location);
                            SharedPreferencesHelper.setMapZoom(context,17f);
                            navController.navigate(R.id.navigation_map);
                        }

                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun createEventPost(post:Post, context: Context) {
            // Set event data
            postText.text = Html.fromHtml("<b>${post.user?.name}</b> creo el evento <b>${post.title}</b> programado para el <b>${post.date}</b>")
            postTime.visibility = View.GONE // TODO: change for a mark of when the post was created

            // Set post user profile pic
            Glide.with(itemView)
                .load(post.user?.photo)
                .placeholder(R.drawable.sample_user)
                .into(userAvatar)

            // Set post image
            if (post.photo != null) {
                postImage.visibility = View.VISIBLE
                Glide.with(context)
                    .load(post.photo)
                    .placeholder(R.drawable.sample_photo)
                    .into(postImage)
            } else {
                postImage.visibility = View.GONE
            }
        }

        private fun createPhotoPost(post:Post, context: Context) {
            // Set photo data
            postText.text = Html.fromHtml("<b>${post.user?.name}</b> compartió una foto")
            postTime.visibility = View.GONE // TODO: change for a mark of when the post was created

            // Set post user profile pic
            Glide.with(itemView)
                .load(post.user?.photo)
                .placeholder(R.drawable.sample_user)
                .into(userAvatar)

            // Set post image
            if (post.photo != null) {
                Glide.with(context)
                    .load(post.photo)
                    .placeholder(R.drawable.sample_photo)
                    .into(postImage)
            }
        }

        private fun createSongPost(post:Post, context: Context) {
            // Set song data
            postText.text = Html.fromHtml("<b>${post.user?.name}</b> compartió una canción")
            postTime.visibility = View.GONE // TODO: change for a mark of when the post was created

            // Song link
            postSong.visibility = View.VISIBLE
            postSong.setOnClickListener {
                val songLink = post.song as String // Tu enlace de Spotify aquí

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(songLink))
                context.startActivity(intent)
            }

            // Set post user profile pic
            Glide.with(itemView)
                .load(post.user?.photo)
                .placeholder(R.drawable.sample_user)
                .into(userAvatar)

            postImage.visibility = View.GONE
        }

        private fun createAlertPost(post:Post, context: Context) {
            // Set song data
            postText.text = Html.fromHtml("<b>${post.user?.name}</b> creó una <b>alerta</b>")
            postTime.visibility = View.GONE // TODO: change for a mark of when the post was created

            // Set post user profile pic
            Glide.with(itemView)
                .load(post.user?.photo)
                .placeholder(R.drawable.sample_user)
                .into(userAvatar)

            postImage.visibility = View.GONE
        }
    }

}