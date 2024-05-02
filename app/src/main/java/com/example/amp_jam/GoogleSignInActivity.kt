package com.example.amp_jam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class GoogleSignInActivity : Activity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = Firebase.auth

        signIn()

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        Log.d(TAG, "CURRENT USER: " + currentUser.toString())
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                if (task != null){
                    Log.d(TAG, "task " + task.toString())
                }
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateDB(user)
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun signIn() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // Start the sign-in intent after sign-out is completed
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Si el usuario ha iniciado sesión correctamente, se podría redirigir a otra Activity
            val name = user.displayName // Nombre del usuario
            val email = user.email // Correo electrónico del usuario
            Log.d(TAG, "updateUIGoogle: User is logged in")
            Log.d(TAG, "Nombre: $name")
            Log.d(TAG, "Correo electrónico: $email")
            startActivity(Intent(this, EnterActivity::class.java))
        } else {
            // Mantener al usuario en la LoginActivity o mostrar algún mensaje según sea necesario
            Log.d(TAG, "updateUIGoogle: No user is logged in")
        }
    }

    private fun updateDB(user: FirebaseUser?) {
        var db = FirebaseFirestore.getInstance()

        user?.let {
            val userExists = db.collection("usuarios").document(user.uid)
            userExists.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d(TAG, "User already exists in the database")
                    } else {
                        // User does not exist, add user information to the database
                        val userMap = hashMapOf(
                            "name" to user.displayName,
                            "email" to user.email
                        )

                        userExists.set(userMap)
                            .addOnSuccessListener {
                                Log.d(TAG, "User added to Firestore with ID: ${user.uid}")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding user to Firestore", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error checking user existence in Firestore", e)
                }
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}