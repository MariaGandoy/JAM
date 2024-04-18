package com.example.amp_jam.ui.theme.friends


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.amp_jam.MainActivity
import com.example.amp_jam.R

class AddFriendsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_friends, container, false)

        setupLogOut(view)
        setupAddFriend1(view)
        setupAddFriend2(view)
        setupAddFriend3(view)
        setupAddFriend4(view)
        setupAddFriend5(view)

        return view
    }

    private fun setupLogOut(view: View) {
        val toolbarLogout = view.findViewById<Button>(R.id.toolbarLogout)
        toolbarLogout.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileActivity] Click LOG OUT EVENT button")
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun setupAddFriend1(view: View) {
        val addBtn = view.findViewById<Button>(R.id.button5)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsFragment] Click ADD FRIEND 1 EVENT button")
        }
    }

    private fun setupAddFriend2(view: View) {
        val addBtn = view.findViewById<Button>(R.id.button16)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsFragment] Click ADD FRIEND 2 EVENT button")
        }
    }

    private fun setupAddFriend3(view: View) {
        val addBtn = view.findViewById<Button>(R.id.button17)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsFragment] Click ADD FRIEND 3 EVENT button")
        }
    }

    private fun setupAddFriend4(view: View) {
        val addBtn = view.findViewById<Button>(R.id.button18)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsFragment] Click ADD FRIEND 4 EVENT button")
        }
    }

    private fun setupAddFriend5(view: View) {
        val addBtn = view.findViewById<Button>(R.id.button19)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsFragment] Click ADD FRIEND 5 EVENT button")
        }
    }
}
