package com.example.amp_jam.ui.theme.camera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.amp_jam.CamaraActivity
import com.example.amp_jam.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.camera, container, false)

        setupCameraButtons(view)
        Log.d("Debugeandoklk", "llego cAMERA")
        return view
    }

    private fun setupCameraButtons(view: View) {
        val folderBtn = view.findViewById<ImageButton>(R.id.imageButton2)
        folderBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[Camera] Click FOLDER button")
        }

        val pictureBtn = view.findViewById<ImageButton>(R.id.imageButton4)
        pictureBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[Camera] Click TAKE PICTURE button")
            Toast.makeText(context, "Abrir camara", Toast.LENGTH_SHORT)
            val intent = Intent(activity, CamaraActivity::class.java)
            startActivity(intent)

        }

        val filtersBtn = view.findViewById<ImageButton>(R.id.imageButton3)
        filtersBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[Camera] Click FILTERS button")
        }

        val settingsBtn = view.findViewById<ImageButton>(R.id.imageButton5)
        settingsBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[Camera] Click SETTINGS button")
        }
    }
}