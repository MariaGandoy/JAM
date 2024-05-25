package com.example.amp_jam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : ComponentActivity() {

    // Referencia a Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        signOut()
        FirebaseApp.initializeApp(this)
        SharedPreferencesHelper.clearMapConfiguration(this)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton = findViewById<Button>(R.id.button)
        val googleButton = findViewById<ImageButton>(R.id.googleButton)

        val defaultEmail = SharedPreferencesHelper.getDefaultEmail(this)
        if (defaultEmail != null) emailEditText.setText(defaultEmail)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signIn(email, password)
        }

        googleButton.setOnClickListener {
            val intent = Intent(this, GoogleSignInActivity::class.java)
            startActivity(intent)
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
                    SharedPreferencesHelper.setDefaultEmail(this, email)
                    saveUserSession()
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

    private fun signOut() {
        Firebase.auth.signOut()
        clearUserSession()
    }

    private fun saveUserSession() {
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_id", FirebaseAuth.getInstance().currentUser?.uid)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    private fun clearUserSession() {
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("user_id")
            putBoolean("isLoggedIn", false)
            apply()
        }
    }
}
