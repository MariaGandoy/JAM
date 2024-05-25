package com.example.amp_jam

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlin.math.sqrt

class ShakerService(private val context: Context) : SensorEventListener {

    private var mSensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var mAccelerometer: Sensor? = null
    private var resume = false

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null


    fun startReading() {
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        job = coroutineScope.launch {
            while (isActive) {
                if (resume) {
                    val event = withContext(Dispatchers.Default) {
                        async { mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
                    }
                    onSensorChanged(event.await() as SensorEvent)
                }
                delay(1000) // Adjust delay as needed
            }
        }
    }

    fun stopReading() {
        mSensorManager.unregisterListener(this)
        job?.cancel()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not implemented
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Fetching x,y,z values
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        lastAcceleration = currentAcceleration

        // Getting current accelerations
        // with the help of fetched x,y,z values
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta: Float = currentAcceleration - lastAcceleration
        acceleration = acceleration * 0.9f + delta

        // Display a Toast message if
        // acceleration value is over 17
        if (acceleration > 17) {
            createAlert()
        }
    }

    private fun createAlert() {
        val database = FirebaseFirestore.getInstance()

        // Create a reference to the user's posts collection
        val userPostsCollection = database.collection("usuarios").document(currentUser!!.uid)
            .collection("posts")

        // Generate a new document ID for the post
        val newPostDocument = userPostsCollection.document()

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)

                // Prepare the data for the post
                val postData = hashMapOf(
                    "fecha" to null,
                    "titulo" to null,
                    "tipo" to "ALERT",
                    "song" to null,
                    "lugar" to latLng,
                    "share" to emptyList<String>(),
                )

                newPostDocument.set(postData)
                Toast.makeText(this.context, "Has creado una alerta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onResume() {
        mSensorManager?.registerListener(this, mSensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun onPause() {
        mSensorManager!!.unregisterListener(this)
    }

    fun onDestroy() {
        coroutineScope.cancel()
    }
}