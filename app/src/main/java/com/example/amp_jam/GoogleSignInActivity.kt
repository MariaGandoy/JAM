package com.example.amp_jam

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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
        auth = FirebaseAuth.getInstance()

        signIn()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        Log.d(TAG, "CURRENT USER: " + currentUser.toString())
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                if (task != null){
                    Log.d(TAG, "task " + task.toString())
                }
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    saveUserSession()
                    updateDB(user)
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun signIn() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val name = user.displayName
            val email = user.email
            Log.d(TAG, "updateUIGoogle: User is logged in")
            Log.d(TAG, "Nombre: $name")
            Log.d(TAG, "Correo electrónico: $email")
            startActivity(Intent(this, EnterActivity::class.java))
        } else {
            Log.d(TAG, "updateUIGoogle: No user is logged in")
        }
    }

    private fun updateDB(user: FirebaseUser?) {
        val db = FirebaseFirestore.getInstance()

        user?.let {
            val userExists = db.collection("usuarios").document(user.uid)
            userExists.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d(TAG, "User already exists in the database")
                    } else {
                        val userMap = hashMapOf(
                            "name" to user.displayName,
                            "email" to user.email,
                            "photo" to user.photoUrl
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

    private fun saveUserSession() {
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_id", FirebaseAuth.getInstance().currentUser?.uid)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}
