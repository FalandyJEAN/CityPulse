package com.example.citypulse.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesApi {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int = 2000,
        @Query("key") apiKey: String
    ): NearbySearchResponse
}
