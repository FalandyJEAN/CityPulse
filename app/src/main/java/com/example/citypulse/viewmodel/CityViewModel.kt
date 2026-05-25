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
    private val _searchQuery = MutableStateFlow("")

    val places: StateFlow<List<Place>> = combine(
        repository.observeAllPlaces(),
        _category,
        _searchQuery
    ) { all, cat, query ->
        all
            .filter { cat.isNullOrBlank() || it.category.equals(cat, ignoreCase = true) }
            .filter {
                query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.address?.contains(query, ignoreCase = true) == true
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<List<Place>> = repository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repository.seedIfEmpty(DEFAULT_PLACES) }
    }

    fun filtrerParCategorie(categorie: String?) {
        _category.value = categorie
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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

    companion object {
        val DEFAULT_PLACES = listOf(
            Place("1", "Le Gourmet", 48.8566, 2.3522, "Restaurants", "12 Rue de Rivoli", null),
            Place("2", "Parc Central", 48.8584, 2.2945, "Parcs", "Avenue Gustave Eiffel", null),
            Place("3", "Musée d'Art", 48.8606, 2.3376, "Musées", "Palais du Louvre", null),
            Place("4", "Café de Flore", 48.8542, 2.3331, "Restaurants", "172 Bd Saint-Germain", null),
            Place("5", "Galerie Lafayette", 48.8736, 2.3320, "Commerces", "40 Bd Haussmann", null)
        )
    }
}
