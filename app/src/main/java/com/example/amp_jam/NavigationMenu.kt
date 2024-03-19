package com.example.amp_jam

import android.app.Activity
import android.content.Intent
import android.widget.ImageButton

class NavigationMenu {
    fun setupBottomMenu(activity: Activity) {
        val timelineBtn = activity.findViewById<ImageButton>(R.id.menuTimeline)
        timelineBtn.setOnClickListener {
            val intent = Intent(activity, TimelineActivity::class.java)
            activity.startActivity(intent)
        }

        val mapBtn = activity.findViewById<ImageButton>(R.id.menuMap)
        mapBtn.setOnClickListener {
            val intent = Intent(activity, MapActivity::class.java)
            activity.startActivity(intent)
        }

        val cameraBtn = activity.findViewById<ImageButton>(R.id.menuCamera)
        cameraBtn.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)
            activity.startActivity(intent)
        }

        val profileBtn = activity.findViewById<ImageButton>(R.id.menuProfile)
        profileBtn.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            activity.startActivity(intent)
        }
    }
}