package com.example.citypulse.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.citypulse.data.PlaceRepository
import com.example.citypulse.data.local.AppDatabase

class CityViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val repository = PlaceRepository(
        AppDatabase.getDatabase(context.applicationContext).placeDao()
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CityViewModel::class.java)) {
            return CityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
