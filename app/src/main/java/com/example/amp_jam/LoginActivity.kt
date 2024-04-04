package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    // Instancia de FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        // Obtener referencia al botón de "Crear cuenta"
        val button = findViewById<Button>(R.id.button)

        // Configurar el Listener para manejar el clic del botón
        button.setOnClickListener {
            // Iniciar otra actividad al hacer click
            Log.d("JAM_NAVIGATION", "[LoginActivity] Click ADD EVENT button");
            val intent = Intent(this, EnterActivity::class.java)
            startActivity(intent)
        }

    }
}
