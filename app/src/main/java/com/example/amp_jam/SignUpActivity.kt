
package com.example.amp_jam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignUpActivity : ComponentActivity() {

    lateinit var auth: FirebaseAuth
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        // Obtener referencia al botón de "Crear cuenta"
        val button = findViewById<Button>(R.id.button7)

        auth = Firebase.auth

        // Configurar el Listener para manejar el clic del botón
        button.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[SignUpActivity] Click SIGN UP button")

            //Hay que pillar el email y la password
            auth.createUserWithEmailAndPassword(R.id.editTextTextEmailAddress.toString(), R.id.editTextTextPassword.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "createUserWithEmail:success")
                        val user = auth.currentUser
                        //updateUI(user)
                        val intent = Intent(this, EnterActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            R.id.editTextTextEmailAddress.toString()+" Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        //updateUI(null)
                    }
                }
        }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            //reload()
        }
    }


}