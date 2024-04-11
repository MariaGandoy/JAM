package com.example.amp_jam

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.SearchView
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class MapActivity : FragmentActivity(), OnMapReadyCallback {
    private var mGoogleMap:GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map)

        setupAddEventButton()
        setUpUbicationListener()
        createMapFragment()
    }

    private fun setupAddEventButton() {
        val addBtn = findViewById<ImageButton>(R.id.addEventButton)
        addBtn.setOnClickListener {
            val eventDialog = MapEventFragment()

            eventDialog.show(supportFragmentManager, "MapEvent")
        }
    }

    private fun setUpUbicationListener() {
        val searchUbication = findViewById<SearchView>(R.id.searchUbication)
        searchUbication.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // This method is called when the user submits the query.
                // You can handle the submission here if needed.
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // This method is called when the text in the search field changes.
                // newText contains the new text entered by the user.
                Log.d("JAM_NAVIGATION", "[MapFragment] Ubication text changed to: $newText")
                return true
            }
        })
    }

    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
    }
}
