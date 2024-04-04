package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity

class AddFriendsActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friends)

        setupBackButton()
        setupLogOut()
        setupAddFriend1()
        setupAddFriend2()
        setupAddFriend3()
        setupAddFriend4()
        setupAddFriend5()
        
        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }

    private fun setupBackButton() {
        val toolbarBack = findViewById<ImageView>(R.id.toolbarBack)
        toolbarBack.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsActivity] Click BACK EVENT button")
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupLogOut() {
        val toolbarLogout = findViewById<ImageView>(R.id.toolbarLogout)
        toolbarLogout.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileActivity] Click LOG OUT EVENT button")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriend1() {
        val addBtn = findViewById<Button>(R.id.button5)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsActivity] Click ADD FRIEND 1 EVENT button")
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriend2() {
        val addBtn = findViewById<Button>(R.id.button16)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsActivity] Click ADD FRIEND 2 EVENT button")
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriend3() {
        val addBtn = findViewById<Button>(R.id.button17)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsActivity] Click ADD FRIEND 3 EVENT button")
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriend4() {
        val addBtn = findViewById<Button>(R.id.button18)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsActivity] Click ADD FRIEND 4 EVENT button")
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriend5() {
        val addBtn = findViewById<Button>(R.id.button19)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[AddFriendsActivity] Click ADD FRIEND 5 EVENT button")
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}