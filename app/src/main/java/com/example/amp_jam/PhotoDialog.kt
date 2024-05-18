package com.example.amp_jam;

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.snapshots
import java.io.File
import java.lang.Double.parseDouble
import java.lang.Float.parseFloat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PhotoDialog :  ComponentActivity() {

        private var mGoogleMap: GoogleMap? = null
        private lateinit var auth: FirebaseAuth
        private var currentUser: FirebaseUser? = null

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(savedInstanceState: Bundle?) {
                // TODO Auto-generated method stub
                super.onCreate(savedInstanceState)

                //Log.d("DEBUG", "showing dialog!");
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.photo_event)
                dialog.setCancelable(true)
                dialog.setCanceledOnTouchOutside(true)

                auth = FirebaseAuth.getInstance()
                currentUser = auth.currentUser

                setUpRadioGroupListener(dialog)
                setUpCreatePost(dialog)
                setUpNameEventListener(dialog)
                setUpExitButton(dialog)

                dialog.show()
                //
                dialog.setOnCancelListener { finish() }
        }

        private fun setUpRadioGroupListener(dialog: Dialog) {
                val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)
                radioGroup.setOnCheckedChangeListener { group, checkedId ->
                        val radioButton = dialog.findViewById<RadioButton>(checkedId)
                        Log.d("JAM_NAVIGATION", "[MapPost] Notify radio group selection changed to: $checkedId")
                }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun setUpCreatePost(dialog: Dialog) {
                val confirm = dialog.findViewById<Button>(R.id.confirm)
                confirm.setOnClickListener {

                        var bundle :Bundle ?=intent.extras
                        var file = bundle!!.getString("photo")

                        val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.fromFile(
                                File(file)
                        )).toString()

                        val eventName = dialog.findViewById<EditText>(R.id.eventName).text.toString()

                        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                        val eventDate = LocalDateTime.now().format(formatter)

                        // Validación básica de nombre
                        if (eventName.isBlank()) {
                                Toast.makeText(this, "El campo de nombre no puede estar vacío.", Toast.LENGTH_LONG).show()                        }

                        val eventType = "PHOTO"

                        //Cambiar foto de null
                        createPost(Post(eventName, eventDate, eventType, null, imageBitmap, null, null))

                        Thread.sleep(500)
                        Toast.makeText(this, "Se ha creado el evento", Toast.LENGTH_LONG).show()
                        Thread.sleep(100)
                        startActivity(Intent(this, EnterActivity::class.java))
                }
        }

        private fun setUpExitButton(dialog: Dialog) {
                val confirm = dialog.findViewById<ImageButton>(R.id.exitButton)
                confirm.setOnClickListener {
                        finish()
                }
        }

        private fun setUpNameEventListener(dialog: Dialog) {
                val textInputName = dialog.findViewById<EditText>(R.id.eventName)

                textInputName.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                Log.d("JAM_NAVIGATION", "[MapEvent] Event name text changed to: $s")
                        }

                        override fun afterTextChanged(s: Editable?) {}
                })
        }

        private fun createPost(postData: Post) {
           this?.let {

                val database = FirebaseFirestore.getInstance()
                var center : LatLng?
                center = mGoogleMap?.cameraPosition?.target
                database.collection("usuarios").document(currentUser!!.uid).get()
                        .addOnSuccessListener {
                                        document ->
                                if (document != null){
                                        //save document to val usuari
                                        val doc = document.data?.entries?.toTypedArray()?.get(1).toString()
                                        val latitude = doc.split("latitude=").get(1).split(",").get(0)
                                        val longitude = doc.split("longitude=").get(1).split("}").get(0)
                                        center = LatLng(parseDouble(latitude), parseDouble(longitude))
                                        //the log show the correct nickname
                                        Log.i("JAM_locati","lat: " + latitude + " - long: " + longitude)
                                }
                        }
                        .addOnFailureListener { exception ->
                                Log.w("JAM_PhotoDialog", "Error getting documents: ", exception)
                        }

                Thread.sleep(100)

                Log.i("JAM_locati","Center: " + center.toString())


                val markerOptions = MarkerOptions()
                   .apply {
                           center?.let { position(it) }
                           title(postData.title.toString())
                           snippet("Fecha: " + postData.date)
                           when (postData.type) {
                                        "EVENT" -> {
                                                val scaledBitmap =Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                                                resources,R.drawable.event_marker), 150, 150, false)
                                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                                        }

                                        "PHOTO" -> {
                                                val scaledBitmap =Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                                                resources,R.drawable.photo_marker), 150, 150, false)
                                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                                        }

                                        "SONG" -> {
                                                val scaledBitmap =Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                                                resources,R.drawable.song_marker), 150, 150, false)
                                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                                        }
                                }
                        }

                mGoogleMap?.addMarker(markerOptions)
                center.let { nonNullCenter ->
                        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(nonNullCenter!!, 20f))
                }

                Log.e("JAM_PhotoDialog", "what is $center")
                // Persist to firebase
                persistPost(postData, center)

           }
        }



        private fun persistPost(data: Post, center: LatLng?) {
                val database = FirebaseFirestore.getInstance()

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
                        "user" to data.user,
                        "foto" to data.photo,
                        "song" to data.song,
                        "lugar" to center
                )

                // Set the post data in the document
                newPostDocument.set(postData)
        }

}