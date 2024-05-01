package com.example.amp_jam.ui.theme.friends


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.amp_jam.R

class FriendsRequestFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.friend_request, container, false)

        Log.d("Debugeandoklk", "llego Friends")
        return view
    }



}
