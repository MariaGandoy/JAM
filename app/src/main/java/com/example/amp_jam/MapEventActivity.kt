package com.example.amp_jam

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.ComponentActivity
import java.util.Calendar

class MapEventActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_event)

        setupAddEventButton()
        setUpNameEventListener()
        setUpDatePickerDialog()
        setUpRadioGroupListener()

        val navigationMenu = NavigationMenu()
        navigationMenu.setupBottomMenu(this)
    }

    private fun setupAddEventButton() {
        val addBtn = findViewById<Button>(R.id.idBtnAdd)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[MapEvent] Click ADD EVENT button")
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpNameEventListener() {
        val textInputName = findViewById<EditText>(R.id.eventName)
        textInputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("JAM_NAVIGATION", "[MapEvent] Event name text changed to: $s")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setUpDatePickerDialog() {
        val textInputDate = findViewById<EditText>(R.id.eventDate)
        textInputDate.setOnClickListener{
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, yearValue, monthValue, dayValue ->
                    textInputDate.setText("$dayValue/$monthValue/$yearValue")
                    Log.d("JAM_NAVIGATION", "[MapEvent] Event date changed to: $dayValue/$monthValue/$yearValue")
                },
                year, month, day
            )

            datePickerDialog.show()
        }
    }

    private fun setUpRadioGroupListener() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            Log.d("JAM_NAVIGATION", "[MapEvent] Notify radio group selection changed to: $checkedId")
        }
    }

}