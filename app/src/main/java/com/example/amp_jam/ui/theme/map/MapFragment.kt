package com.example.amp_jam.ui.theme.map

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
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
import com.example.amp_jam.SharedPreferencesHelper
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
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

    private lateinit var autocompleteFragment: AutocompleteSupportFragment


    private var LOCATION_SERVICE_ACTIVE = false

    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver

    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

        Places.initialize(requireContext(), getString(R.string.google_map_api_key))
        autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocompleteUbication) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object: PlaceSelectionListener{
            override fun onError(place: Status) {
                Log.d("JAM_NAVIGATION", "[MapFragment] Error searching location")
            }

            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng!!

                zoomOnMap(latLng)
            }
        })

        setupAddEventButton(view)
        createMapFragment()
        checkLocationPermissions()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el cliente de ubicación fusionada
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (LOCATION_SERVICE_ACTIVE) {
            registerLocationReceiver()
            // Obtener la ubicación actual y hacer zoom en ella
            getCurrentLocationAndZoom()
        }

    }

    private fun getCurrentLocationAndZoom() {
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
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(location.latitude, location.longitude)

                    // Zoom en última configuración o en usuario por defecto
                    val lastCords = SharedPreferencesHelper.getLastCords()
                    val zoom = SharedPreferencesHelper.getMapZoom()
                    if (lastCords != null && zoom != null) {
                        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(lastCords, zoom))
                    } else {
                        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.25f))
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MapFragment", "Error getting location: ${e.message}")
            }
    }
    override fun onDestroyView() {
        // Guardar última configuración del mapa
        val center = mGoogleMap?.cameraPosition?.target
        val zoom = mGoogleMap?.cameraPosition?.zoom
        if (center != null && zoom != null) {
            SharedPreferencesHelper.setLastCords(center)
            SharedPreferencesHelper.setMapZoom(zoom)
        }

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

        // Verificar si la ubicación está habilitada
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // La ubicación no está habilitada, solicitar al usuario que la habilite
            AlertDialog.Builder(requireContext())
                .setTitle("Activar Ubicación")
                .setMessage("La ubicación está desactivada. ¿Desea activarla ahora?")
                .setPositiveButton("Sí") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cerrar") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        } else {
            // La ubicación está habilitada, activar el botón de "Mi Ubicación"
            if (ActivityCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mGoogleMap!!.isMyLocationEnabled = true

            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            Log.d("Localizacion", location.toString())
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                zoomOnMap(latLng)
            }

        }
    }

    override fun onLocationReceived(location: Location) {
        // Update the map with the received location data
        val myLocation = LatLng(location.latitude, location.longitude)

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

    private fun zoomOnMap(latLng: LatLng) {
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
        mGoogleMap?.animateCamera(newLatLngZoom)
    }

    /* Handle events creation and data base dialog: */
    override fun onEventSubmitted(postData: Post) {
        val center = mGoogleMap?.cameraPosition?.target

        // Create event marker (quitar cuando se lea de firebase y leer tb nuestro datos de base de datos ¿?)
        val markerOptions = MarkerOptions()
            .apply {
                center?.let { position(it) }
                title(postData.title.toString())
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
