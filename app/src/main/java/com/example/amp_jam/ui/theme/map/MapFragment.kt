package com.example.amp_jam.ui.theme.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.amp_jam.MapEventFragment
import com.example.amp_jam.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

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

    // TODO: move ubication related actions to a "LocationService"
    private var FINE_PERMISSION_CODE = 1
    private var mGoogleMap: GoogleMap? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map, container, false)

        setupAddEventButton(view)
        setUpUbicationListener(view)
        createMapFragment()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLastLocation()

        return view
    }

    private fun setupAddEventButton(view: View) {
        val addBtn = view.findViewById<ImageButton>(R.id.addEventButton)
        addBtn.setOnClickListener {
            val eventDialog = MapEventFragment()

            eventDialog.show(requireFragmentManager(), "MapEvent")
        }
    }

    private fun setUpUbicationListener(view: View) {
        val searchUbication = view.findViewById<SearchView>(R.id.searchUbication)
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
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_PERMISSION_CODE
            )
            return
        }

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location

                    val myLocation = LatLng(currentLocation.latitude, currentLocation.longitude)
                    val markerOptions = MarkerOptions()
                        .position(myLocation)
                        .title("My position")
                    mGoogleMap?.addMarker(markerOptions)
                    mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10f))
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Log.d("JAM_NAVIGATION", "[MapFragment] Need permission")
            }
        }
    }
}
