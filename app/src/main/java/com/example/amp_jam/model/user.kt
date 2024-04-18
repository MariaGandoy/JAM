package com.example.amp_jam.model

data class User(
    val amigos: List<String> = listOf(),
    val ciudad: GeoPoint = GeoPoint(0.0, 0.0),
    val mail: String = "",
    val nombre: String = ""
)

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)
