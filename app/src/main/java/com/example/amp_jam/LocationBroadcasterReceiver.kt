package com.example.amp_jam

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.util.Log

class LocationBroadcastReceiver(private val listener: LocationListener) : BroadcastReceiver() {

    interface LocationListener {
        fun onLocationReceived(mapData: HashMap<Any, Any>)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (intent.action == LocationService.MAP_UPDATED) {
                val mapData = intent.getSerializableExtra(LocationService.MAP_DATA) as? HashMap<Any, Any>
                mapData?.let {
                    listener.onLocationReceived(it)
                }
            }
        }
    }

    companion object {
        fun getIntentFilter(): IntentFilter {
            return IntentFilter(LocationService.MAP_UPDATED)
        }
    }
}