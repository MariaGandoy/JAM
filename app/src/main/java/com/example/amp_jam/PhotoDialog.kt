package com.example.amp_jam;

import android.app.Dialog
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

class PhotoDialog :  ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                // TODO Auto-generated method stub
                super.onCreate(savedInstanceState)

                //Log.d("DEBUG", "showing dialog!");
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.photo_event)
                dialog.setCancelable(true)
                dialog.setCanceledOnTouchOutside(true)

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

        private fun setUpCreatePost(dialog: Dialog) {
                val confirm = dialog.findViewById<Button>(R.id.confirm)
                confirm.setOnClickListener {
                        //var bundle :Bundle ?=intent.extras
                        //var photo = bundle!!.getString("photo")

                        //Toast.makeText(this, "Leída foto", Toast.LENGTH_SHORT).show()

                        finish()
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

}