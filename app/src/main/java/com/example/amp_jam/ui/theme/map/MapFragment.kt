package com.example.amp_jam.ui.theme.map

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.amp_jam.LocationBroadcastReceiver
import com.example.amp_jam.LocationService
import com.example.amp_jam.MapPostFragment
import com.example.amp_jam.Post
import com.example.amp_jam.R
import com.example.amp_jam.ShakerService
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
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(), OnMapReadyCallback, LocationBroadcastReceiver.LocationListener, MapPostFragment.MapPostDialogListener {

    private var FINE_PERMISSION_CODE = 1

    private var mGoogleMap: GoogleMap? = null

    private var mapMarkers: MutableList<Marker> = ArrayList()

    private var visibleMarkers = mutableListOf("FRIEND", "EVENT", "PHOTO", "SONG", "ALERT")

    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    private var LOCATION_SERVICE_ACTIVE = false

    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var auth: FirebaseAuth

    private var currentUser: FirebaseUser? = null

    private lateinit var shakerService: ShakerService

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map, container, false)
        progressBar = view.findViewById<ProgressBar>(R.id.progress_circular)
        if (mapMarkers.isEmpty()) progressBar.visibility = View.VISIBLE

        // Retrieve current user
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        createMapFragment()
        checkLocationPermissions()
        setUpLegend(view)
        setUpSearchLocation(view)
        setupAddEventButton(view)
        setUpCustomCenterButton(view)

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
        shakerService = ShakerService(this.context as Context)

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

                    mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.25f))

                    // Zoom en última configuración o en usuario por defecto
                    val lastCords = SharedPreferencesHelper.getLastCords(this.context as Context)
                    val zoom = SharedPreferencesHelper.getMapZoom(this.context as Context)
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
            SharedPreferencesHelper.setLastCords(this.context as Context, center)
            SharedPreferencesHelper.setMapZoom(this.context as Context, zoom)
        }

        super.onDestroyView()
        unregisterLocationReceiver()
    }


    private fun setupAddEventButton(view: View) {
        val addBtn = view.findViewById<ImageButton>(R.id.addEventButton)
        addBtn.setOnClickListener {
            val postDialog = MapPostFragment()
            postDialog.listener = this
            postDialog.show(requireFragmentManager(), "PostDialog")
        }
    }

    private fun setUpLegend(view: View) {
        val legendButton = view.findViewById<ImageButton>(R.id.legendButton)
        val legendContainer = view.findViewById<FrameLayout>(R.id.legendContainer)

        legendButton.setOnClickListener {
            // Toggle visibility of the legend container
            legendContainer.visibility = if (legendContainer.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }

            // Handle each checkbox
            if (legendContainer.visibility == View.VISIBLE) {
                setupMarkerChangeListener("EVENT", view.findViewById(R.id.checkboxEvents))
                setupMarkerChangeListener("FRIEND", view.findViewById(R.id.checboxAmigos))
                setupMarkerChangeListener("SONG", view.findViewById(R.id.checboxSongs))
                setupMarkerChangeListener("ALERT", view.findViewById(R.id.checboxAlerts))
                setupMarkerChangeListener("PHOTO", view.findViewById(R.id.checkboxPhotos))
            }
        }
    }

    private fun setupMarkerChangeListener(markerType: String, checkBox: CheckBox) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                visibleMarkers.add(markerType)
            } else {
                visibleMarkers.remove(markerType)
            }
            updateMarkersVisibility()
        }
    }

    private fun updateMarkersVisibility() {
        for (marker in mapMarkers) {
            marker.isVisible = visibleMarkers.contains(marker.getTag())
        }
    }

    private fun setUpSearchLocation(view: View) {
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
    }

    private fun setUpCustomCenterButton(view: View) {
        val centerButton = view.findViewById<ImageButton>(R.id.centerButton)

        centerButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                centerButton.isEnabled = false
            } else {
                centerButton.isEnabled = true
                val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }
            }
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
            mGoogleMap!!.uiSettings.isMyLocationButtonEnabled = false

            // User location
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                zoomOnMap(latLng)
            }

            // Last saved location
            val lastCords = SharedPreferencesHelper.getLastCords(this.context as Context)
            val zoom = SharedPreferencesHelper.getMapZoom(this.context as Context)

            if (lastCords != null && zoom != null) {
                mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(lastCords, zoom))
            } else if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                zoomOnMap(latLng)
            }
        }
    }

    override fun onLocationReceived(mapData: HashMap<Any, Any>) {
        if (progressBar.visibility == View.VISIBLE) {
            progressBar.visibility = View.GONE
        }

        // Accessing the location
        val location: Location? = mapData["currentLocation"] as? Location

        if (location !== null) {
            val myLocation = LatLng(location.latitude, location.longitude)

            CoroutineScope(Dispatchers.IO).launch {
                persistUbication(myLocation)
            }
        }

        // Accessing map data (friends and posts)
        val posts: MutableList<Post>? = mapData["posts"] as? MutableList<Post>
        val friends: ArrayList<HashMap<String, Any?>>? = mapData["friends"] as? ArrayList<HashMap<String, Any?>>

        if (posts !== null && friends !== null) updateMapView(posts, friends)
    }

    private fun updateMapView(posts: MutableList<Post>, friends: ArrayList<HashMap<String, Any?>>) {
        mGoogleMap?.clear()
        mapMarkers.clear()

        // Update posts from firebase
        posts.forEach { post ->
            val shareWith = post.shareWith as? List<String> ?: emptyList()

            if (post.user!!.id == currentUser!!.uid || shareWith.isEmpty() || shareWith.contains(currentUser!!.uid)) {
                val latitude = post.location?.latitude as Double
                val longitude = post.location?.longitude as Double

                val postMarkerOptions = MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .apply {
                        when (post.type) {
                            "EVENT" -> {
                                val scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.event_marker), 150, 150, false)
                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                                title("Evento " + post.title.toString() + " creado para el " + post.date)
                                snippet(post.timestamp.toString())
                            }
                            "PHOTO" -> {
                                val scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.photo_marker), 150, 150, false)
                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                                title("Foto " + post.photo.toString() )
                                snippet(post.timestamp.toString())
                            }
                            "SONG" -> {
                                val scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.song_marker), 150, 150, false)
                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                                title("Canción " + post.song.toString())
                                snippet(post.timestamp.toString())
                            }
                            "ALERT" -> {
                                val scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.alert_marker), 150, 150, false)
                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                                title("Alerta creada por " + (post.user?.name ?: "ti"))
                                snippet(post.timestamp.toString())
                            }
                        }
                    }

                val marker = mGoogleMap?.addMarker(postMarkerOptions) as Marker
                marker.setTag(post.type)
                mapMarkers.add(marker)
            }
        }

        // Update friends from firebase
        friends.forEach { friend ->
            val lugar = friend["lugar"] as? HashMap<*, *>

            if (lugar != null) {
                val latitude = lugar["latitude"] as Double
                val longitude = lugar["longitude"] as Double
                val photoUrl = friend["photo"] as String? // Usar safe cast para manejar nulos

                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(photoUrl)
                        .override(100, 100) // Redimensionar la imagen al tamaño deseado
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resource)

                                val userMarkerOptions = MarkerOptions()
                                    .position(LatLng(latitude, longitude))
                                    .title(friend["user"] as String)
                                    .icon(bitmapDescriptor)

                                val marker = mGoogleMap?.addMarker(userMarkerOptions) as Marker
                                marker.setTag("FRIEND")
                                mapMarkers.add(marker)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                } else {
                    val sampleBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_user)
                    val resizedBitmap = Bitmap.createScaledBitmap(sampleBitmap, 100, 100, false)
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedBitmap)

                    val userMarkerOptions = MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .title(friend["user"] as String)
                        .icon(bitmapDescriptor)

                    val marker = mGoogleMap?.addMarker(userMarkerOptions) as Marker
                    marker.setTag("FRIEND")
                    mapMarkers.add(marker)
                }
            }
        }

        updateMarkersVisibility()
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

    /* Handle posts creation: */
    override fun onPostSubmitted(postData: Post) {
        view?.let {
            val mapOverlay = it.findViewById<View>(R.id.mapOverlay)
            val instructionText = it.findViewById<TextView>(R.id.instructionsMarker)
            val instructionDelete = it.findViewById<TextView>(R.id.mockupDelete)

            mapOverlay.visibility = View.VISIBLE
            instructionText.visibility = View.VISIBLE
            instructionDelete.visibility = View.VISIBLE

            instructionDelete.setOnClickListener {
                mapOverlay.visibility = View.GONE
                instructionText.visibility = View.GONE
                instructionDelete.visibility = View.GONE

                mGoogleMap?.setOnMapLongClickListener(null)
            }

            mGoogleMap?.setOnMapLongClickListener { postPosition ->
                // Create event marker (quitar cuando se lea de firebase y leer tb nuestro datos de base de datos ¿?)
                val markerOptions = MarkerOptions()
                    .apply {
                        position(postPosition)
                        title(postData.title.toString())
                        snippet("Fecha: " + postData.date)
                        when (postData.type) {
                            "EVENT" -> {
                                val scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.event_marker), 150, 150, false)
                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                            }
                            "PHOTO" -> {
                                val scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.photo_marker), 150, 150, false)
                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                            }
                            "SONG" -> {
                                val scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.song_marker), 150, 150, false)
                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                            }
                            "ALERT" -> {
                                val scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.alert_marker), 150, 150, false)
                                icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                            }
                        }
                    }

                mGoogleMap?.addMarker(markerOptions)

                mapOverlay.visibility = View.GONE
                instructionText.visibility = View.GONE
                instructionDelete.visibility = View.GONE

                // Persist to firebase
                CoroutineScope(Dispatchers.IO).launch {
                    persistPost(postData, postPosition)
                }
            }
        }
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
            "song" to data.song,
            "lugar" to center,
            "share" to data.shareWith,
            "timestamp" to data.timestamp
        )

        // Store post image if not null
        if (data.photo != null) {
            val imageName = "${currentUser!!.uid}_${System.currentTimeMillis()}.jpg"

            persisBitmap(data.photo as Bitmap, imageName) { imageUrl ->
                // Set the post data in the document after saving the preference to the photo
                postData["photo"] = imageUrl
                newPostDocument.set(postData)
            }
        } else {
            // Set the post data in the document
            newPostDocument.set(postData)
        }
    }

    private fun persistUbication(location: LatLng?) {
        val database = FirebaseFirestore.getInstance()

        var userData = hashMapOf<String, Any?>(
            "lugar" to location
        )

        // Update the document in Firestore if share location is true in settings
        if (currentUser != null && SharedPreferencesHelper.getShareLocation(requireContext())) {
            database.collection("usuarios").document(currentUser!!.uid)
                .update(userData)
                .addOnFailureListener { e ->
                    Log.d("Firestore", "Error updating user data")
                }
        }

    }

    private fun persisBitmap(bitmap: Bitmap, imageName: String, callback: (Uri?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child(imageName)

        val byteArray = bitmapToByteArray(bitmap)
        val uploadTask = imageRef.putBytes(byteArray)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri) // Use the image name instead of the URL
            }.addOnFailureListener {
                callback(null)
                Log.e("FirebaseStorage", "Failed to get download URL")
            }
        }.addOnFailureListener {
            callback(null)
            Log.e("FirebaseStorage", "Failed to upload image")
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    override fun onResume() {
        super.onResume()
        shakerService.onResume()
        shakerService.startReading()
    }
    override fun onPause() {
        super.onPause()
        shakerService.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        shakerService.onDestroy()
    }
}
