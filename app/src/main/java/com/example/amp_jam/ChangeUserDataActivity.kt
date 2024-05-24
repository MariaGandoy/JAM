package com.example.amp_jam

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeUserDataActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var saveButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_data)

        nameEditText = findViewById(R.id.name)
        lastNameEditText = findViewById(R.id.lastName)
        saveButton = findViewById(R.id.button5)

        loadUserData()

        saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun loadUserData() {
        currentUser?.uid?.let { uid ->
            db.collection("usuarios").document(uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    nameEditText.setText(document.getString("name"))
                    lastNameEditText.setText(document.getString("lastName"))
                } else {
                    Toast.makeText(this, "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar los datos: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserData() {
        val userMap: Map<String, Any> = hashMapOf(
            "name" to nameEditText.text.toString(),
            "lastName" to lastNameEditText.text.toString()
        )

        currentUser?.uid?.let { uid ->
            db.collection("usuarios").document(uid).update(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Datos guardados correctamente.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar los datos: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }


}
