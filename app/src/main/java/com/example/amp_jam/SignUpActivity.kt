
package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
class SignUpActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        // Obtener referencia al botón de "Crear cuenta"
        val button = findViewById<Button>(R.id.button7)

        // Configurar el Listener para manejar el clic del botón
        button.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[SignUpActivity] Click ADD EVENT button")
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

    }


}