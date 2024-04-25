package com.example.amp_jam
import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng


object SharedPreferencesHelper {
    private val PREF_NAME = "MyAppPrefs"

    private val KEY_DEFAULT_EMAIL = "defaultEmail"
    private val KEY_LAST_CORDS = "lastCords"
    private val KEY_LAST_ZOOM = "mapZoom"

    private lateinit var sharedPreferences: SharedPreferences;

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    fun setDefaultEmail(user: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_DEFAULT_EMAIL, user)
        editor.apply()
    }

    fun getDefaultEmail(): String? {
        return sharedPreferences.getString(KEY_DEFAULT_EMAIL, null)
    }

    fun setLastCords(cords: LatLng) {
        val cordsString = cords.latitude.toString() + "," + cords.longitude.toString()
        val editor = sharedPreferences.edit()
        editor.putString(KEY_LAST_CORDS, cordsString)
        editor.apply()
    }

    fun getLastCords(): LatLng? {
        val locationArray: List<String> =
            sharedPreferences.getString(KEY_LAST_CORDS, null)?.split(",") ?: listOf()

        if (locationArray.isEmpty()) return null

        val retrievedLat = locationArray[0].toDoubleOrNull()
        val retrievedLong = locationArray[1].toDoubleOrNull()

        if (retrievedLat == null || retrievedLong == null) return null

        return LatLng(retrievedLat, retrievedLong)
    }

    fun setMapZoom(zoom: Float) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_LAST_ZOOM, zoom.toString())
        editor.apply()
    }

    fun getMapZoom(): Float? {
        return sharedPreferences.getString(KEY_LAST_ZOOM, null)?.toFloat()
    }

    fun clearMapConfiguration() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_LAST_CORDS)
        editor.remove(KEY_LAST_ZOOM)
        editor.apply()
    }

    fun clearSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}