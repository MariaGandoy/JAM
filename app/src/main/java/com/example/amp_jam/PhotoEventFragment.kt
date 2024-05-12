package com.example.amp_jam;

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class PhotoEventFragment : DialogFragment() {

        interface PhotoEventDialogListener {
                fun onEventSubmitted(data: Post)
        }

        fun showDialog() {
                getActivity()?.getSupportFragmentManager()
                        ?.let { PhotoEventFragment().show(it, "myCustomDialog") };
        }


        var listener: PhotoEventDialogListener? = null

        private lateinit var auth: FirebaseAuth

        private var currentUser: FirebaseUser? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                return activity?.let {
                        val builder = AlertDialog.Builder(it)
                        val inflater = requireActivity().layoutInflater;
                        val view = inflater.inflate(R.layout.photo_event, null)

                        // Retrieve current user
                        auth = FirebaseAuth.getInstance()
                        currentUser = auth.currentUser

                        setUpExitButton(view)
                        setUpNameEventListener(view)
                        setUpRadioGroupListener(view)
                        setupPhotoButtons(view)

                        builder.setView(view)
                                .setPositiveButton("AÑADIR") { dialog, id ->
                                        Log.d("JAM_NAVIGATION", "[MapEvent] Add event")
                                        val eventData = setEventData(view)
                                        submitEvent(eventData)
                                }
                                .setNegativeButton("CANCELAR") { dialog, id ->
                                        Log.d("JAM_NAVIGATION", "[MapEvent] Cancel event")
                                }
                        builder.create()
                } ?: throw IllegalStateException("Activity cannot be null")
        }

        private fun setUpExitButton(view: View) {
                val exitBtn = view.findViewById<ImageButton>(R.id.exitButton)
                exitBtn.setOnClickListener {
                        Log.d("JAM_NAVIGATION", "[MapEvent] Click EXIT button")
                }
        }

        private fun setUpNameEventListener(view: View) {
                val textInputName = view.findViewById<EditText>(R.id.eventName)
                textInputName.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                Log.d("JAM_NAVIGATION", "[MapEvent] Event name text changed to: $s")
                        }

                        override fun afterTextChanged(s: Editable?) {}
                })
        }

        private fun setUpRadioGroupListener(view: View) {
                val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
                radioGroup.setOnCheckedChangeListener { group, checkedId ->
                        val radioButton = view.findViewById<RadioButton>(checkedId)
                        Log.d("JAM_NAVIGATION", "[MapEvent] Notify radio group selection changed to: $checkedId")
                }
        }

        private fun setupPhotoButtons(view: View) {
                val folderBtn = view.findViewById<ImageButton>(R.id.addFromCamera)
                folderBtn.setOnClickListener {
                        Log.d("JAM_NAVIGATION", "[MapEvent] Click ADD FROM CAMERA button")
                }

                val pictureBtn = view.findViewById<ImageButton>(R.id.addFromFiles)
                pictureBtn.setOnClickListener {
                        Log.d("JAM_NAVIGATION", "[MapEvent] Click ADD FROM FILES button")
                }
        }

        private fun setEventData(view: View): Post {
                val eventName = view.findViewById<EditText>(R.id.eventName).text.toString()
                val eventDate = view.findViewById<EditText>(R.id.eventDate).text.toString()
                val eventType = "EVENT"
                val userEmail = currentUser?.email ?: ""
                val photo= null //Añadir la foto que se acaba de sacar

                return Post(eventName, eventDate, eventType, userEmail, null, null, null)
        }

        private fun submitEvent(data: Post) {
                listener?.onEventSubmitted(data)
                dismiss()
        }
}