package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Obtener referencia al botón de "Crear cuenta"
        val button = findViewById<Button>(R.id.button2)

        // Configurar el Listener para manejar el clic del botón
        button.setOnClickListener {
            // Iniciar otra actividad al hacer click
            val intent = Intent(this, LoginActivity::class.java)
            Log.d("JAM_NAVIGATION", "[MapEvent] Click ADD EVENT button")
            startActivity(intent)
        }

        val button3 = findViewById<Button>(R.id.button3)

        // Configurar el Listener para manejar el clic del botón
        button3.setOnClickListener {
            // Iniciar otra actividad al hacer click
            Log.d("JAM_NAVIGATION", "[MainActivity] Click ADD EVENT button")
            val intent = Intent(this, SignUpActivity::class.java)

            startActivity(intent)
        }
    }

}