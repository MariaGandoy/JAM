package com.example.amp_jam.ui.theme.map

import android.Manifest
import android.content.Intent
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.amp_jam.LocationBroadcastReceiver
import com.example.amp_jam.LocationService
import com.example.amp_jam.MapEventFragment
import com.example.amp_jam.Post
import com.example.amp_jam.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(), OnMapReadyCallback, LocationBroadcastReceiver.LocationListener, MapEventFragment.MapEventDialogListener {

    private var FINE_PERMISSION_CODE = 1
    private var mGoogleMap: GoogleMap? = null
    private var previousLocation: Marker? = null


    private var LOCATION_SERVICE_ACTIVE = false

    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver

    private lateinit var auth: FirebaseAuth

    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map, container, false)

        // Retrieve current user
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        setupAddEventButton(view)
        setUpUbicationListener(view)
        createMapFragment()
        checkLocationPermissions()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(LOCATION_SERVICE_ACTIVE)
            registerLocationReceiver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterLocationReceiver()
    }


    private fun setupAddEventButton(view: View) {
        val addBtn = view.findViewById<ImageButton>(R.id.addEventButton)
        addBtn.setOnClickListener {
            val eventDialog = MapEventFragment()
            eventDialog.listener = this
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

    /* Check permissions: */
    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                FINE_PERMISSION_CODE
            )
        } else {
            // Permissions are already granted, start the LocationService
            startLocationService()
            registerLocationReceiver()
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
                startLocationService()
                registerLocationReceiver()
            } else {
                Log.d("JAM_NAVIGATION", "[MapFragment] Need permission")
                // TODO: Handle the case where permissions are not granted
            }
        }
    }

    /* LocationService related functions: */
    private fun createMapFragment() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun startLocationService() {
        LOCATION_SERVICE_ACTIVE = true
        val locationServiceIntent = Intent(requireContext(), LocationService::class.java)
        requireActivity().startService(locationServiceIntent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap!!.uiSettings.isZoomControlsEnabled = true
    }

    override fun onLocationReceived(location: Location) {
        // Update the map with the received location data
        val myLocation = LatLng(location.latitude, location.longitude)

        // Remove the previous marker if it exists
        previousLocation?.remove()

        // Add the new marker
        val markerOptions = MarkerOptions()
            .position(myLocation)
            .title("Mi posición en tiempo real")

        previousLocation = mGoogleMap?.addMarker(markerOptions)

        // Persist to firebase
        persistUbication(myLocation)
    }

    private fun registerLocationReceiver() {
        locationBroadcastReceiver = LocationBroadcastReceiver(this)

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationBroadcastReceiver,
            LocationBroadcastReceiver.getIntentFilter()
        )
    }

    private fun unregisterLocationReceiver() {
        if (::locationBroadcastReceiver.isInitialized) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        }
    }

    /* Handle events creation and data base dialog: */
    override fun onEventSubmitted(postData: Post) {
        val center = mGoogleMap?.cameraPosition?.target

        // Create event marker (quitar cuando se lea de firebase y leer tb nuestro datos de base de datos ¿?)
        val markerOptions = MarkerOptions()
            .apply {
                center?.let { position(it) }
                title(postData.title)
                snippet("Fecha: " + postData.date)
                when (postData.type) {
                    "EVENT" -> {
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    }
                    "PHOTO" -> {
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                    }
                    "SONG" -> {
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    }
                }
            }


        // TODO: remove this and read all events/posts from firebase to draw the map, even our own posts
        mGoogleMap?.addMarker(markerOptions)
        center?.let { nonNullCenter ->
            mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(nonNullCenter, 20f))
        }

        // Persist to firebase
        persistPost(postData, center)
    }

    private fun persistPost(data: Post, center: LatLng?) {
        val database = FirebaseFirestore.getInstance()

        // Create a reference to the user's posts collection
        val userPostsCollection = database.collection("usuarios").document(currentUser!!.uid)
            .collection("posts")

        // Generate a new document ID for the post
        val newPostDocument = userPostsCollection.document()

        // Prepare the data for the post
        val postData = hashMapOf(
            "fecha" to data.date,
            "titulo" to data.title,
            "tipo" to data.type,
            "user" to data.user,
            "lugar" to center
        )

        // Set the post data in the document
        newPostDocument.set(postData)
    }

    private fun persistUbication(location: LatLng?) {
        val database = FirebaseFirestore.getInstance()

        // TODO: Change to update location, not create new everytime
        var userData = hashMapOf(
            "user" to currentUser!!.email,
            "lugar" to location
        )

        if (currentUser != null) {
            database.collection("usuarios").document(currentUser!!.uid)
                .set(userData)
        }
    }
}
