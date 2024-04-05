package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val db = Firebase.firestore

        // Create a new user with a first and last name


        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Obtener referencia al botón de "Crear cuenta"
        val button = findViewById<Button>(R.id.button2)

        // Configurar el Listener para manejar el clic del botón
        button.setOnClickListener {
            // Iniciar otra actividad al hacer click
            val intent = Intent(this, LoginActivity::class.java)
            Log.d("JAM_NAVIGATION", "[MapEvent] Click ADD EVENT button")

            val user = hashMapOf(
                "first" to "Ada",
                "last" to "Lovelace",
                "born" to 1815,
            )

            // Add a new document with a generated ID
            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("tag", "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("tag", "Error adding document", e)
                }



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