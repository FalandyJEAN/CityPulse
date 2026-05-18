package com.example.citypulse.data

import com.example.citypulse.data.local.PlaceDao
import com.example.citypulse.model.Place
import kotlinx.coroutines.flow.Flow

class PlaceRepository(private val placeDao: PlaceDao) {

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
}
