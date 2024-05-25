package com.example.amp_jam

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ChangeUserDataActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var saveButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_data)
        setUpChangePicture()
        setUpBackArrow()

        nameEditText = findViewById(R.id.name)
        lastNameEditText = findViewById(R.id.lastName)
        saveButton = findViewById(R.id.button5)

        loadUserData()

        saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun setUpBackArrow() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener  {
            finish()
        }
    }

    private fun setUpChangePicture() {
        val changePictureBtn = findViewById<ImageButton>(R.id.changePicture)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                changePictureBtn?.setImageBitmap(imageBitmap)
            }
        }

        changePictureBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
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
        val filesImage = findViewById<ImageButton>(R.id.changePicture)
        val filesBitmap = (filesImage.drawable as? BitmapDrawable)?.bitmap

        if (filesBitmap != null) {
            val imageName = "${currentUser!!.uid}_${System.currentTimeMillis()}.jpg"

            persisBitmap(filesBitmap, imageName) { imageUrl ->
                val userMap: Map<String, Any> = hashMapOf(
                    "name" to nameEditText.text.toString(),
                    "lastName" to lastNameEditText.text.toString(),
                    "photo" to imageUrl.toString()
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
        } else {
            val userMap: Map<String, Any> = hashMapOf(
                "name" to nameEditText.text.toString(),
                "lastName" to lastNameEditText.text.toString(),
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

    private fun persisBitmap(bitmap: Bitmap, imageName: String, callback: (Uri?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child(imageName)

        val byteArray = bitmapToByteArray(bitmap)
        val uploadTask = imageRef.putBytes(byteArray)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri) // Use the image name instead of the URL
            }.addOnFailureListener {
                callback(null)
                Log.e("FirebaseStorage", "Failed to get download URL")
            }
        }.addOnFailureListener {
            callback(null)
            Log.e("FirebaseStorage", "Failed to upload image")
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }


}
