package com.example.citypulse.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citypulse.data.PlaceRepository
import com.example.citypulse.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CityViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category

    val places: StateFlow<List<Place>> = combine(
        repository.observeAllPlaces(),
        _category
    ) { all, cat ->
        if (cat.isNullOrBlank()) all else all.filter { it.category.equals(cat, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<List<Place>> = repository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun seed(places: List<Place>) {
        viewModelScope.launch { repository.seedIfEmpty(places) }
    }

    fun filtrerParCategorie(categorie: String?) {
        _category.value = categorie
    }

    fun toggleFavorite(place: Place) {
        viewModelScope.launch { repository.toggleFavorite(place) }
    }

    fun removeFavorite(place: Place) {
        viewModelScope.launch { repository.removeFavorite(place) }
    }

    fun saveNote(place: Place, note: String) {
        viewModelScope.launch { repository.updateUserNote(place, note) }
    }

    suspend fun getPlace(id: String): Place? = repository.getPlaceById(id)

    fun calculerDistance(userLat: Double, userLon: Double, placeLat: Double, placeLon: Double): Float {
        val userLoc = Location("user").apply { latitude = userLat; longitude = userLon }
        val placeLoc = Location("place").apply { latitude = placeLat; longitude = placeLon }
        return userLoc.distanceTo(placeLoc)
    }
}
