package com.example.amp_jam
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.util.Log

class LocationBroadcastReceiver(private val listener: LocationListener) : BroadcastReceiver() {

    interface LocationListener {
        fun onLocationReceived(location: Location)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (intent.action == LocationService.LOCATION_UPDATED) {
                val location = intent.getParcelableExtra<Location>(LocationService.LOCATION_DATA)
                if (location != null) {
                    listener.onLocationReceived(location)
                }
            }
        }
    }

    companion object {
        fun getIntentFilter(): IntentFilter {
            return IntentFilter(LocationService.LOCATION_UPDATED)
        }
    }
}