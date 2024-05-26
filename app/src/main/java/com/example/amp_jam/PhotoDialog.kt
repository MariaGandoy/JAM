package com.example.amp_jam;

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Double.parseDouble
import java.lang.Float.parseFloat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PhotoDialog :  ComponentActivity() {

        private lateinit var auth: FirebaseAuth
        private var currentUser: FirebaseUser? = null
        private lateinit var database: FirebaseFirestore

        private lateinit var groupsView: RecyclerView
        private val itemList: MutableList<DocumentSnapshot> = mutableListOf()

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(savedInstanceState: Bundle?) {
                // TODO Auto-generated method stub
                super.onCreate(savedInstanceState)

                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.photo_event)
                dialog.setCancelable(true)
                dialog.setCanceledOnTouchOutside(true)

                auth = FirebaseAuth.getInstance()
                currentUser = auth.currentUser
                database = FirebaseFirestore.getInstance()

                loadGroups()
                setupGroupsView(dialog)
                setUpCreatePost(dialog)
                setUpExitButton(dialog)

                dialog.show()
                dialog.setOnCancelListener { finish() }
        }

        private fun setupGroupsView(dialog: Dialog) {
                groupsView = dialog.findViewById<RecyclerView>(R.id.groupsView)
                groupsView.layoutManager = LinearLayoutManager(this)

                groupsView.adapter = GroupsAdapter(itemList, "name")
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun setUpCreatePost(dialog: Dialog) {
                val confirm = dialog.findViewById<Button>(R.id.confirm)
                confirm.setOnClickListener {

                        var bundle :Bundle ?=intent.extras
                        var file = bundle!!.getString("photo")

                        val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.fromFile(
                                File(file)
                        ))

                        val recyclerView = dialog.findViewById<RecyclerView>(R.id.groupsView)
                        val selectedGroups = (recyclerView.adapter as? GroupsAdapter)?.selectedGroups?.keys?.toList() ?: listOf()

                        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                        val creationTime = LocalDateTime.now().format(formatter)

                        val eventType = "PHOTO"

                        createPost(Post(null, null, eventType, null, imageBitmap, null, null, selectedGroups, creationTime))
                }
        }

        private fun setUpExitButton(dialog: Dialog) {
                val confirm = dialog.findViewById<ImageButton>(R.id.exitButton)
                confirm.setOnClickListener {
                        finish()
                }

                val cancel = dialog.findViewById<Button>(R.id.cancel)
                cancel.setOnClickListener {
                        finish()
                }
        }

        private fun getCoords(): LatLng? {
                var center : LatLng?
                center = LatLng(0.0, 0.0)
                database.collection("usuarios").document(currentUser!!.uid).get()
                        .addOnSuccessListener {
                                        document ->
                                if (document != null){
                                        val doc = document.data?.entries?.toTypedArray()?.get(1).toString()
                                        val latitude = doc.split("latitude=").get(1).split(",").get(0)
                                        val longitude = doc.split("longitude=").get(1).split("}").get(0)
                                        center = LatLng(parseDouble(latitude), parseDouble(longitude))
                                }
                        }
                        .addOnFailureListener { exception ->
                                Log.w("JAM_PhotoDialog", "Error getting documents: ", exception)
                        }

                return center
        }

        private fun createPost(postData: Post) {
           this?.let {
                var center : LatLng?

                   val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                   if (ActivityCompat.checkSelfPermission(
                                   this,
                                   Manifest.permission.ACCESS_FINE_LOCATION
                           ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                   this,
                                   Manifest.permission.ACCESS_COARSE_LOCATION
                           ) != PackageManager.PERMISSION_GRANTED
                   ) {
                           Toast.makeText(this, "No se ha podido compartir la foto", Toast.LENGTH_LONG).show()
                           return

                   } else {
                           val location =
                                   locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                           if (location != null) {
                                   center = LatLng(location.latitude, location.longitude)
                                   persistPost(postData, center)

                           } else {
                                   Toast.makeText(this, "No se ha podido compartir la foto", Toast.LENGTH_LONG).show()
                           }
                   }
           }
        }



        private fun persistPost(data: Post, center: LatLng?) {
                Log.i("JAM_locati","Center: " + center.toString())

                // Create a reference to the user's posts collection
                val userPostsCollection = database.collection("usuarios").document(currentUser!!.uid)
                        .collection("posts")


                // Generate a new document ID for the post
                val newPostDocument = userPostsCollection.document()

                // Prepare the data for the post
                val postData = hashMapOf(
                        "fecha" to data.date,
                        "titulo" to data.title,
                        "tipo" to data.type,
                        "song" to data.song,
                        "lugar" to center,
                        "share" to data.shareWith
                        )

                Log.e("JAM_photos", "data.photo: " + data.photo)

                // Store post image if not null
                if (data.photo != null) {
                        val imageName = "${currentUser!!.uid}_${System.currentTimeMillis()}.jpg"

                        persisBitmap(data.photo as Bitmap, imageName) { imageUrl ->
                                // Set the post data in the document after saving the preference to the photo
                                postData["photo"] = imageUrl
                                newPostDocument.set(postData)
                        }
                } else {
                        // Set the post data in the document
                        newPostDocument.set(postData)
                }

                Thread.sleep(500)
                Toast.makeText(this, "Se ha compartido la foto", Toast.LENGTH_LONG).show()
                Thread.sleep(100)
                startActivity(Intent(this, EnterActivity::class.java))
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

        private fun loadGroups() {
                val userUid = FirebaseAuth.getInstance().currentUser?.uid
                if (userUid != null) {
                        itemList.clear()
                        database.collection("usuarios").document(userUid).collection("groups")
                                .get()
                                .addOnSuccessListener { documents ->
                                        if (documents.isEmpty) {
                                                Log.d("GroupCreateDialog", "Groups List: []")
                                        } else {
                                                for (groupDocument in documents) {
                                                        itemList.add(groupDocument)
                                                        groupsView.adapter?.notifyDataSetChanged()
                                                }
                                        }
                                }
                                .addOnFailureListener { exception ->
                                        Log.d("GroupCreateDialog", "Error loading friends list", exception)
                                }
                } else {
                        Log.d("GroupCreateDialog", "User ID is null, unable to load friends")
                }
        }


}