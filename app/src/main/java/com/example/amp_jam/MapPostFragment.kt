package com.example.amp_jam

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class MapPostFragment: DialogFragment() {

    interface MapPostDialogListener {
        fun onPostSubmitted(data: Post)
    }

    var listener: MapPostDialogListener? = null
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.map_post, null)


            val viewPager = view.findViewById<ViewPager>(R.id.viewPager)
            val adapter = MyPagerAdapter(childFragmentManager) // Pass the dialog to the adapter
            viewPager.adapter = adapter

            val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
            tabLayout.setupWithViewPager(viewPager)

            // Retrieve current user
            auth = FirebaseAuth.getInstance()
            currentUser = auth.currentUser

            // Set toolbar
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setNavigationOnClickListener  {
                dismiss()
            }

            builder.setView(view)
                .setPositiveButton("AÑADIR") { _, _ ->
                    val currentPage = viewPager.currentItem
                    val currentFragment = adapter.instantiateItem(viewPager, currentPage) as? Fragment
                    currentFragment?.view?.let { currentPageView ->

                        when (currentPage) {
                            0 -> setEventData(currentPageView)
                            1 -> setPhotoData(currentPageView)
                            2 -> setSongData(currentPageView)
                        }
                    }
                }
                .setNegativeButton("CANCELAR") { _, _ ->
                    Log.d("JAM_NAVIGATION", "[MapPost] Cancel post")
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    private fun setEventData(view: View) {
        Log.d("JAM_NAVIGATION", "[MapPost] Create EVENT")

        val eventName = view.findViewById<EditText>(R.id.eventName).text.toString()
        val eventDate = view.findViewById<EditText>(R.id.eventDate).text.toString()

        // Validación básica de nombre y evento
        if (eventName.isBlank() || eventDate.isBlank()) {
            Toast.makeText(requireContext(), "Los campos de nombre y fecha no pueden estar vacíos.", Toast.LENGTH_LONG).show()
            return
        }

        val filesImage = view.findViewById<ImageButton>(R.id.addFromFiles)
        val filesBitmap = (filesImage.drawable as? BitmapDrawable)?.bitmap

        val cameraImage = view.findViewById<ImageButton>(R.id.addFromCamera)
        val cameraBitmap = (cameraImage.drawable as? BitmapDrawable)?.bitmap

        val photoBitmap = when {
            filesBitmap != null -> filesBitmap
            cameraBitmap != null -> cameraBitmap
            else -> null
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.groupsView)
        val selectedGroups = (recyclerView.adapter as? GroupsAdapter)?.selectedGroups?.keys?.toList() ?: listOf()

        val eventType = "EVENT"

        submitPost(Post(eventName, eventDate, eventType, null, photoBitmap, null, null, selectedGroups))
    }

    private fun setPhotoData(view: View) {
        Log.d("JAM_NAVIGATION", "[MapPost] Create PHOTO")

        val filesImage = view.findViewById<ImageButton>(R.id.addFromFiles)
        val filesBitmap = (filesImage.drawable as? BitmapDrawable)?.bitmap

        val cameraImage = view.findViewById<ImageButton>(R.id.addFromCamera)
        val cameraBitmap = (cameraImage.drawable as? BitmapDrawable)?.bitmap

        val photoBitmap = when {
            filesBitmap != null -> filesBitmap
            cameraBitmap != null -> cameraBitmap
            else -> null
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.groupsView)
        val selectedGroups = (recyclerView.adapter as? GroupsAdapter)?.selectedGroups?.keys?.toList() ?: listOf()

        val eventType = "PHOTO"

        submitPost(Post(null, null, eventType, null, photoBitmap, null, null, selectedGroups))
    }

    private fun setSongData(view: View) {
        Log.d("JAM_NAVIGATION", "[MapPost] Create SONG")

        val postSong = view.findViewById<EditText>(R.id.songName).text.toString()
        val eventType = "SONG"

        val recyclerView = view.findViewById<RecyclerView>(R.id.groupsView)
        val selectedGroups = (recyclerView.adapter as? GroupsAdapter)?.selectedGroups?.keys?.toList() ?: listOf()

        submitPost(Post( null, null, eventType, null, null, postSong, null, selectedGroups))
    }

    private fun submitPost(data: Post) {
        listener?.onPostSubmitted(data)
        dismiss()
    }
}