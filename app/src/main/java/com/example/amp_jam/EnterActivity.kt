package com.example.amp_jam
import android.os.Bundle
import android.util.Log
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
        Log.d("Holaquetal", "llego hasta aquí al menos")


        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        Log.d("Holaquetal", "2")

        navView.setBackgroundColor(ContextCompat.getColor(this, R.color.darkGreen))

        Log.d("Holaquetal", "3")

        navView.setupWithNavController(navController)
        Log.d("Holaquetal", "4")
    }
}