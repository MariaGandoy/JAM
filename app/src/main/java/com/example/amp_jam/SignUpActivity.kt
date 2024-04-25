package com.example.amp_jam


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
import com.google.firebase.firestore.FirebaseFirestore


class SignUpActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameEditText = findViewById<EditText>(R.id.editTextText)
        val lastNameEditText = findViewById<EditText>(R.id.editTextText2)
        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val signUpButton = findViewById<ImageButton>(R.id.button7)

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            createAccount(name, lastName, email, password)
        }
    }

    private fun createAccount(name: String, lastName: String, email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(baseContext, "El correo electrónico y la contraseña no pueden estar vacíos.", Toast.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "createUserWithEmail:success")
                    val user = auth.currentUser
                    user?.let {
                        val userMap = hashMapOf(
                            "name" to name,
                            "lastName" to lastName,
                            "email" to email
                        )
                        db.collection("users").document(user.uid).set(userMap)
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot added with ID: ${user.uid}")
                                startActivity(Intent(this, EnterActivity::class.java))
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error adding document", e)
                            }
                    }
                } else {
                    Log.w("FirebaseAuth", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "El registro falló.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
