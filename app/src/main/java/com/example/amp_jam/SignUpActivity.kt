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

class SignUpActivity : ComponentActivity() {

    // Referencia a Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val signUpButton = findViewById<Button>(R.id.button7)

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            createAccount(email, password)
        }
    }

    private fun createAccount(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(baseContext, "El correo electrónico y la contraseña no pueden estar vacíos.", Toast.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro exitoso
                    Log.d("FirebaseAuth", "createUserWithEmail:success")
                    val user = auth.currentUser
                    startActivity(Intent(this, EnterActivity::class.java))
                } else {
                    // Si falla el registro, muestra un mensaje al usuario
                    Log.w("FirebaseAuth", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "El registro falló.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
