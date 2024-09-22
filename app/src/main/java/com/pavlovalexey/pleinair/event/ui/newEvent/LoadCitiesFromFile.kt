package com.pavlovalexey.pleinair.event.ui.newEvent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.pavlovalexey.pleinair.R
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun loadCitiesFromFile(): List<String> {
    val context = LocalContext.current
    val cities = remember {
        val inputStream = context.resources.openRawResource(R.raw.cities)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val citiesList = mutableListOf<String>()
        reader.useLines { lines ->
            lines.forEach { line ->
                val cityName = line.split(",")[0] // Извлекаем только название города
                citiesList.add(cityName)
            }
        }
        citiesList
    }
    return cities
}