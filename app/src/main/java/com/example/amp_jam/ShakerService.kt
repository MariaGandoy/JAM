package com.example.amp_jam

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
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

    fun startReading() {
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)

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
        // acceleration value is over 12
        if (acceleration > 12) {

            //Esto cambiarlo por
            Toast.makeText(this.context, "Shake event detected", Toast.LENGTH_SHORT).show()
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