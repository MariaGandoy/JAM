package com.example.amp_jam

import android.Manifest
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


class LocationService : Service() {

    // Obtain latest location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Called when FusedLocationProviderClient has a new Location
    private lateinit var locationCallback: LocationCallback

    // Store current location (TODO: persist in firebase)
    private var currentLocation: Location? = null

    companion object {
        const val LOCATION_DATA = "com.example.amp_jam.LOCATION_DATA"
        const val LOCATION_UPDATED = "com.example.amp_jam.action.LOCATION_UPDATED"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startLocationUpdates() {
        // Create a LocationRequest (configuration parameters for LocationCallback)
        val locationRequest = LocationRequest.create().apply {
            // Sets the desired interval for active location updates.
            interval = 20000 // 20 secs (TODO: cambiar llegado el momento, para pruebas dejar número alto para evitar muchas peticiones)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        // Initialize the LocationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                currentLocation = locationResult.lastLocation

                // Notify that a new location was added
                val intent = Intent(LOCATION_UPDATED)
                intent.putExtra(LOCATION_DATA, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}