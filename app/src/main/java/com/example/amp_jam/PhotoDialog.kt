package com.example.amp_jam;

import android.app.Dialog
import android.os.Build
import android.os.Bundle
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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PhotoDialog :  ComponentActivity() {

        interface MapPostDialogListener {
                fun onPostSubmitted(data: Post)
        }

        var listener: MapPostDialogListener? = null
        private lateinit var auth: FirebaseAuth
        private var currentUser: FirebaseUser? = null

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
                        var photo = bundle!!.getString("photo")

                        val eventName = dialog.findViewById<EditText>(R.id.eventName).text.toString()

                        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                        val eventDate = LocalDateTime.now().format(formatter)

                        // Validación básica de nombre
                        if (eventName.isBlank()) {
                                Toast.makeText(this, "El campos de nombre no puede estar vacío.", Toast.LENGTH_LONG).show()                        }

                        val eventType = "EVENT"
                        val userEmail = currentUser?.email ?: ""

                        submitPost(Post(eventName, eventDate, eventType, userEmail, photo, null, null))
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

        private fun submitPost(data: Post) {
                listener?.onPostSubmitted(data)
                finish()
        }

}