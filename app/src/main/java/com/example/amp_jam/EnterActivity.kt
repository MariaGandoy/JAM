package com.example.amp_jam
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.amp_jam.databinding.EnterActivityBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class EnterActivity : AppCompatActivity() {

    private lateinit var binding: EnterActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EnterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        navView.setBackgroundColor(ContextCompat.getColor(this, R.color.darkGreen))

        navView.setupWithNavController(navController)
    }
}