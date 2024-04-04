package com.example.amp_jam.ui.theme.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.amp_jam.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile, container, false)

        setupBackButton(view)
        setupLogOut(view)
        setupAddFriends(view)

        return view
    }

    private fun setupBackButton(view: View) {
        val toolbarBack = view.findViewById<ImageView>(R.id.toolbarBack)
        toolbarBack.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileFragment] Click BACK EVENT button")
        }
    }

    private fun setupLogOut(view: View) {
        val toolbarLogout = view.findViewById<ImageView>(R.id.toolbarLogout)
        toolbarLogout.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileFragment] Click LOG OUT EVENT button")
        }
    }

    private fun setupAddFriends(view: View) {
        val addBtn = view.findViewById<Button>(R.id.button5)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileFragment] Click ADD FRIENDS MENU EVENT button")

        }
    }
}
