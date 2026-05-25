package com.example.citypulse.data.remote

import com.google.gson.annotations.SerializedName

data class NearbySearchResponse(
    val results: List<PlaceResult>,
    val status: String
)

data class PlaceResult(
    @SerializedName("place_id") val placeId: String,
    val name: String,
    val geometry: Geometry,
    val vicinity: String?,
    val types: List<String>,
    val photos: List<Photo>?
)

data class Geometry(val location: LatLngRemote)

data class LatLngRemote(val lat: Double, val lng: Double)

data class Photo(
    @SerializedName("photo_reference") val photoReference: String
)
