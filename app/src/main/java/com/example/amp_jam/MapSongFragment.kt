package com.example.amp_jam

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup


/**
 * A simple [Fragment] subclass.
 * Use the [MapEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapSongFragment() : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.map_song, container, false)

        setUpSongTextListener(view)
        setUpRadioGroupListener(view)

        return view
    }

    private fun setUpSongTextListener(view: View) {
        val textInputSong = view.findViewById<EditText>(R.id.songName)
        textInputSong.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("JAM_NAVIGATION", "[MapSong] Song text changed to: $s")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setUpRadioGroupListener(view: View) {
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = view.findViewById<RadioButton>(checkedId)
            Log.d("JAM_NAVIGATION", "[MapPost] Notify radio group selection changed to: $checkedId")
        }
    }
}