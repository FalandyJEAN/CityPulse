package com.example.citypulse.utils

import android.content.Context
import android.content.Intent

object ShareUtils {
    fun partagerLieu(context: Context, nom: String, lat: Double, lon: Double) {
        val message = "Regarde ce lieu sur CityPulse !\n\n" +
                "Nom : $nom\n" +
                "Coordonnées : $lat, $lon\n" +
                "Lien Maps : https://www.google.com/maps/search/?api=1&query=$lat,$lon"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Partager via"))
    }
}
