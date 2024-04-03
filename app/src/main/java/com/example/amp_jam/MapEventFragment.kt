package com.example.amp_jam

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class MapEventFragment: DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAddEventButton(view)
        setUpNameEventListener(view)
        setUpDatePickerDialog(view)
        setUpRadioGroupListener(view)
    }

    private fun setupAddEventButton(view: View) {
        val addBtn = view.findViewById<Button>(R.id.idBtnAdd)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[MapEvent] Click ADD EVENT button")
            dismiss()
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

}