package com.pavlovalexey.pleinair.event.ui.newEvent

import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CitySelectionField(
    city: String,
    onCityChange: (String) -> Unit,
    citiesList: List<String>,
    onCitySelected: (String) -> Unit // Новый параметр
) {
    var expanded by remember { mutableStateOf(false) }
    var filteredCities by remember { mutableStateOf(citiesList) }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = city,
                onValueChange = { query ->
                    onCityChange(query)
                    filteredCities = if (query.isNotEmpty()) {
                        citiesList.filter { it.startsWith(query, ignoreCase = true) }
                    } else {
                        citiesList
                    }
                    expanded = filteredCities.isNotEmpty()
                },
                label = { Text("Выберите город") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.surface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        expanded = false
                        onCitySelected(city) // Вызываем обратный вызов при нажатии "Done"
                    }
                ),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredCities.forEach { selectedCity ->
                    DropdownMenuItem(onClick = {
                        onCityChange(selectedCity)
                        expanded = false
                        onCitySelected(selectedCity) // Вызываем обратный вызов при выборе из списка
                    }) {
                        Text(text = selectedCity)
                    }
                }
            }
        }
    }
}