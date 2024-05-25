package com.example.amp_jam

import com.google.android.gms.maps.model.LatLng

data class Post(
    var title: Any?,
    var date: Any?,
    var type: Any?,
    var user: User?,
    var photo: Any?,
    var song: Any?,
    var location: LatLng?,
    var shareWith: Any?) {}

