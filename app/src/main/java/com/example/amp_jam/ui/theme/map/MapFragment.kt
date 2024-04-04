package com.example.amp_jam.ui.theme.map

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.amp_jam.MapEventFragment
import com.example.amp_jam.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    private var mGoogleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map, container, false)

        setupAddEventButton(view)
        setUpUbicationListener(view)
        createMapFragment()

        return view
    }

    private fun setupAddEventButton(view: View) {
        val addBtn = view.findViewById<Button>(R.id.addEventButton)
        addBtn.setOnClickListener {
            val eventDialog = MapEventFragment()

            eventDialog.show(requireFragmentManager(), "MapEvent")
        }
    }

    private fun setUpUbicationListener(view: View) {
        val textInputUbication = view.findViewById<EditText>(R.id.textInputUbication)
        textInputUbication.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("JAM_NAVIGATION", "[MapFragment] Ubication text changed to: $s")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun createMapFragment() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
    }
}
