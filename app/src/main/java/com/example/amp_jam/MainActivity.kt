package com.example.amp_jam

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.Objects
import kotlin.math.sqrt

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

    private fun checkUserSession() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Si hay un usuario iniciado sesión, dirigirlo a la pantalla principal
            Log.d("FirebaseAuth", "checkUserSession: User is logged in")
            try {
                startActivity(Intent(this, EnterActivity::class.java))
                Log.d("Holaquetal", "checkUserSession: User is logged in")
                finish() // Finalizar LoginActivity para evitar volver atrás
                Log.d("Holaquetal", "pasofinish")
            } catch (e: Exception) {
                Log.e("Vererror", "Error launching EnterActivity", e)
            }
        } else {
            // Si no hay ningún usuario iniciado sesión, permitir que el usuario inicie sesión normalmente
            Log.d("FirebaseAuth", "checkUserSession: No user is logged in")
        }
    }






}