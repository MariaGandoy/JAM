package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.amp_jam.ui.theme.AMP_JAMTheme

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
            startActivity(intent)
        }
    }
}