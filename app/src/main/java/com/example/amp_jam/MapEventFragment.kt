package com.example.amp_jam

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class MapEventFragment: DialogFragment() {

    data class EventData(
        val name: String,
        val date: String,
        val type: String,
    )

    interface MapEventDialogListener {
        fun onEventSubmitted(data: EventData)
    }

    var listener: MapEventDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val view = inflater.inflate(R.layout.map_event, null)

            setUpExitButton(view)
            setUpNameEventListener(view)
            setUpDatePickerDialog(view)
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

    private fun setUpDatePickerDialog(view: View) {
        val textInputDate = view.findViewById<EditText>(R.id.eventDate)
        textInputDate.setOnClickListener{
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, yearValue, monthValue, dayValue ->
                    textInputDate.setText("$dayValue/$monthValue/$yearValue")
                    Log.d(
                        "JAM_NAVIGATION",
                        "[MapEvent] Event date changed to: $dayValue/$monthValue/$yearValue"
                    )
                },
                year, month, day
            )

            datePickerDialog.show()
        }
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

    private fun setEventData(view: View): EventData {
        val eventName = view.findViewById<EditText>(R.id.eventName).text.toString()
        val eventDate = view.findViewById<EditText>(R.id.eventDate).text.toString()
        val eventType = "EVENT" // TODO: add SONG and PHOTO

        return EventData(eventName, eventDate, eventType)
    }

    private fun submitEvent(data: EventData) {
        listener?.onEventSubmitted(data)
        dismiss()
    }

}