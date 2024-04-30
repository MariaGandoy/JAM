package com.example.amp_jam

import com.google.android.gms.maps.model.LatLng

data class Post(
    var title: Any?,
    var date: Any?,
    var type: Any?,
    var user: Any?,
    var photo: String?,
    var song: String?,
    var location: LatLng?) {}

