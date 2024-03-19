package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.ComponentActivity

class AddFriendsActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friends)

        setupBackButton()
        setupAddFriend1()
        setupAddFriend2()
        setupAddFriend3()
        setupAddFriend4()
        setupAddFriend5()
        
        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }

    private fun setupBackButton() {
        val addBtn = findViewById<Button>(R.id.button)
        addBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriend1() {
        val addBtn = findViewById<Button>(R.id.button5)
        addBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriend2() {
        val addBtn = findViewById<Button>(R.id.button16)
        addBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriend3() {
        val addBtn = findViewById<Button>(R.id.button17)
        addBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriend4() {
        val addBtn = findViewById<Button>(R.id.button18)
        addBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddFriend5() {
        val addBtn = findViewById<Button>(R.id.button19)
        addBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}