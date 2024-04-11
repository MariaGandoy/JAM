package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : ComponentActivity() {

    // Referencia a Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        FirebaseApp.initializeApp(this)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Obtener referencias a los EditText y al botón de "Entrar"
        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton = findViewById<Button>(R.id.button)

        // Configurar el Listener para manejar el clic del botón de "Entrar"
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signIn(email, password)
        }
    }

    private fun signIn(email: String, password: String) {
        // Validación básica de email y contraseña
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(baseContext, "Los campos de correo electrónico y contraseña no pueden estar vacíos.", Toast.LENGTH_LONG).show()
            return
        }

        // Intento de inicio de sesión con Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    Log.d("FirebaseAuth", "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // Error al iniciar sesión
                    Log.w("FirebaseAuth", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "La autenticación falló.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Si el usuario ha iniciado sesión correctamente, se podría redirigir a otra Activity
            Log.d("FirebaseAuth", "updateUI: User is logged in")
            startActivity(Intent(this, EnterActivity::class.java))
        } else {
            // Mantener al usuario en la LoginActivity o mostrar algún mensaje según sea necesario
            Log.d("FirebaseAuth", "updateUI: No user is logged in")
        }
    }

    // Si se quiere implementar el registro de usuarios nuevos, se puede agregar una función similar aquí
    //o podemos acceder al signup de la otra actividad
    // y llamarla desde un botón de "Crear cuenta", por ejemplo.
}
