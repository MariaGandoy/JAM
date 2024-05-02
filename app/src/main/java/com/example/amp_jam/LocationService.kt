package com.example.amp_jam

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LocationService : Service() {

    // Obtain latest location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Called when FusedLocationProviderClient has a new Location
    private lateinit var locationCallback: LocationCallback

    // Store current location (TODO: persist in firebase)
    private var currentLocation: Location? = null

    companion object {
        const val MAP_DATA = "com.example.amp_jam.MAP_DATA"
        const val MAP_UPDATED = "com.example.amp_jam.action.MAP_UPDATED"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startMapUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMapUpdates()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startMapUpdates() {
        // Create a LocationRequest (configuration parameters for LocationCallback)
        val locationRequest = LocationRequest.create().apply {
            // Sets the desired interval for active location updates.
            interval = 15000 // 15 secs (TODO: cambiar llegado el momento, para pruebas dejar número alto para evitar muchas peticiones)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Initialize the LocationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                currentLocation = locationResult.lastLocation

                GlobalScope.launch {
                    val friendsData = getFriendsData()
                    // Get posts
                    val posts: MutableList<Post> = ArrayList()
                    posts.addAll(friendsData.posts)
                    posts.addAll(getUserPosts(null))

                    // Get friends location
                    val friends = friendsData.friends

                    var mapData = hashMapOf(
                        "currentLocation" to currentLocation,
                        "posts" to posts,
                        "friends" to friends
                    )

                    val intent = Intent(MAP_UPDATED)
                    intent.putExtra(MAP_DATA, mapData)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
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

    private fun stopMapUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    data class FriendsPostsData(
        val posts: MutableList<Post>,
        val friends: ArrayList<HashMap<String, Any?>>
    )

    private suspend fun getFriendsData(): FriendsPostsData {
        val database = FirebaseFirestore.getInstance()

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val posts: MutableList<Post> = ArrayList()
        val friends: ArrayList<HashMap<String, Any?>> = ArrayList()

        // Get friends posts Firestore
        if (currentUser != null) {
            try {
                val documents = database.collection("usuarios")
                    .document(currentUser.uid)
                    .collection("friends")
                    .get()
                    .await()

                for (document in documents) {
                    val friendPosts = getUserPosts(document.id)
                    friends.add(getUserUbication(document.id))
                    posts.addAll(friendPosts)
                }
            } catch (exception: Exception) {
                // Handle any errors that may occur
            }
        }

        return FriendsPostsData(posts, friends)
    }

    private suspend fun getUserPosts(id: String?): MutableList<Post> {
        // Determine the userId to use
        val userId = id ?: FirebaseAuth.getInstance().currentUser?.uid

        val database = FirebaseFirestore.getInstance()
        val posts:MutableList<Post> = ArrayList()

        if (userId != null) {
            try {
                val postsResult = database.collection("usuarios")
                    .document(userId).collection("posts")
                    .get()
                    .await()

                for (postDocument in postsResult) {
                    val postData = postDocument.data

                    // Get post location data
                    val lugarPost = postData["lugar"] as HashMap<*, *>
                    val latitude = lugarPost["latitude"] as Double
                    val longitude = lugarPost["longitude"] as Double

                    posts.add(Post(postData["titulo"], postData["fecha"], postData["tipo"], postData["user"], null, null, LatLng(latitude, longitude)))
                }
            } catch (exception: Exception) {
                // Handle any errors that may occur
            }
        }

        return posts
    }

    private suspend fun getUserUbication(id: String): HashMap<String, Any?>  {
        val database = FirebaseFirestore.getInstance()
        var userUbication = hashMapOf<String, Any?>()

        try {
            val userResult = database.collection("usuarios")
                .document(id)
                .get()
                .await()

            userUbication = hashMapOf(
                "id" to id,
                "lugar" to userResult["lugar"],
                "user" to userResult["user"]
            )
        } catch (exception: Exception) {
            // Handle any errors that may occur
        }

        return userUbication
    }
}