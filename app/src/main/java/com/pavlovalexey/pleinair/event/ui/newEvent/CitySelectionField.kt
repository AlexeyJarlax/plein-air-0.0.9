package com.pavlovalexey.pleinair.event.ui.newEvent

import android.util.Log
import android.widget.Toast
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CitySelectionField(
    city: String,
    onCityChange: (String) -> Unit,
    citiesList: List<String>,
    onCitySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var filteredCities by remember { mutableStateOf(citiesList) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var isCitySelected by remember { mutableStateOf(false) }

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
                    isCitySelected = false // Сброс флага выбора, так как пользователь изменяет текст
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
                        // Если пользователь выбрал город из списка
                        if (isCitySelected && selectedCity != null) {
                            onCitySelected(selectedCity!!)
                        } else if (filteredCities.contains(city)) {
                            // Если город найден в фильтрованном списке
                            onCitySelected(city)
                        }
                    }
                ),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredCities.forEach { cityItem ->
                    DropdownMenuItem(onClick = {
                        selectedCity = cityItem
                        isCitySelected = true // Устанавливаем флаг, что город был выбран из списка
                        onCityChange(cityItem)
                        onCitySelected(cityItem) // Вызываем выбор города
                        expanded = false
                    }) {
                        Text(text = cityItem)
                    }
                }
            }
        }
    }
}