package com.example.citypulse.data

import android.util.Log
import com.example.citypulse.data.local.PlaceDao
import com.example.citypulse.data.remote.Photo
import com.example.citypulse.data.remote.PlaceResult
import com.example.citypulse.data.remote.RetrofitClient
import com.example.citypulse.model.Place
import kotlinx.coroutines.flow.Flow

class PlaceRepository(private val placeDao: PlaceDao) {

    sealed class FetchResult {
        data class Success(val count: Int) : FetchResult()
        data class Empty(val status: String) : FetchResult()
        data class ApiError(val status: String, val message: String?) : FetchResult()
        data class NetworkError(val cause: Throwable) : FetchResult()
        object MissingKey : FetchResult()
    }

    fun observeAllPlaces(): Flow<List<Place>> = placeDao.getAllPlaces()

    fun observeFavorites(): Flow<List<Place>> = placeDao.getFavoritePlaces()

    suspend fun getPlaceById(id: String): Place? = placeDao.getPlaceById(id)

    suspend fun savePlaces(places: List<Place>) = placeDao.insertPlaces(places)

    suspend fun toggleFavorite(place: Place) {
        placeDao.updatePlace(place.copy(isFavorite = !place.isFavorite))
    }

    suspend fun removeFavorite(place: Place) {
        placeDao.updatePlace(place.copy(isFavorite = false))
    }

    suspend fun updateUserNote(place: Place, note: String) {
        placeDao.updatePlace(place.copy(userNote = note))
    }

    suspend fun seedIfEmpty(seed: List<Place>) {
        if (placeDao.getPlaceById(seed.firstOrNull()?.id ?: return) == null) {
            placeDao.insertPlaces(seed)
        }
    }

    suspend fun fetchAndStoreNearbyPlaces(
        lat: Double,
        lon: Double,
        apiKey: String
    ): FetchResult {
        if (apiKey.isBlank()) {
            Log.w(TAG, "GOOGLE_PLACES_API_KEY manquante dans local.properties")
            return FetchResult.MissingKey
        }
        return try {
            val response = RetrofitClient.placesApi.getNearbyPlaces(
                location = "$lat,$lon",
                apiKey = apiKey
            )
            when {
                response.status == "OK" && response.results.isNotEmpty() -> {
                    val places = response.results.map { it.toPlace(apiKey) }
                    placeDao.clearNonFavorites()
                    placeDao.insertPlaces(places)
                    FetchResult.Success(places.size)
                }
                response.status == "ZERO_RESULTS" || response.results.isEmpty() ->
                    FetchResult.Empty(response.status)
                else -> {
                    Log.w(TAG, "Google Places API status=${response.status}")
                    FetchResult.ApiError(response.status, null)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Erreur réseau Google Places", t)
            FetchResult.NetworkError(t)
        }
    }

    private fun PlaceResult.toPlace(apiKey: String) = Place(
        id = placeId,
        name = name,
        latitude = geometry.location.lat,
        longitude = geometry.location.lng,
        category = mapCategory(types),
        address = vicinity,
        photoUrl = photos?.firstOrNull()?.buildPhotoUrl(apiKey)
    )

    private fun Photo.buildPhotoUrl(apiKey: String) =
        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=$photoReference&key=$apiKey"

    private fun mapCategory(types: List<String>): String = when {
        types.any { it in listOf("restaurant", "cafe", "bar", "food", "meal_delivery", "meal_takeaway", "bakery") } -> "Restaurants"
        types.any { it in listOf("park", "natural_feature", "campground", "stadium") } -> "Parcs"
        types.any { it in listOf("museum", "art_gallery", "tourist_attraction", "church", "place_of_worship") } -> "Musées"
        types.any { it in listOf("store", "shopping_mall", "supermarket", "clothing_store", "department_store", "pharmacy") } -> "Commerces"
        else -> "Autres"
    }

    companion object {
        private const val TAG = "PlaceRepository"
    }
}
