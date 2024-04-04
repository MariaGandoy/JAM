package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {
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
            Log.d("PROBANDOKLK", "llamo")
            startActivity(intent)
        }

    }
}
