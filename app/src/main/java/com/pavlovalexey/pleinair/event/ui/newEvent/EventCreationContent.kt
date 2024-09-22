package com.pavlovalexey.pleinair.event.ui.newEvent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.event.model.NewEventUiState
import com.pavlovalexey.pleinair.event.ui.eventLocation.getAddressFromLatLng
import com.pavlovalexey.pleinair.utils.uiComponents.CustomButtonOne
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun EventCreationContent(
    modifier: Modifier = Modifier,
    uiState: NewEventUiState,
    cities: List<String>,
    onUiStateChange: (NewEventUiState) -> Unit,
    onCreateEvent: () -> Unit,
    onChooseLocation: () -> Unit,
    onCitySelected: (String) -> Unit // Новый параметр
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var address by remember(uiState.latitude, uiState.longitude) {
        mutableStateOf<String?>(null)
    }
    val isButtonLocationVisible = false

    LaunchedEffect(key1 = uiState.latitude, key2 = uiState.longitude) {
        if (uiState.latitude != null && uiState.longitude != null) {
            address = getAddressFromLatLng(context, uiState.latitude, uiState.longitude)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        CitySelectionField(
            city = uiState.city,
            onCityChange = { query ->
                onUiStateChange(uiState.copy(city = query))
            },
            citiesList = cities,
            onCitySelected = { selectedCity ->
                onUiStateChange(uiState.copy(city = selectedCity))
                onCitySelected(selectedCity)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (showDatePicker) {
            val currentDate = LocalDate.now()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${month + 1}/$year"
                    onUiStateChange(uiState.copy(date = selectedDate))
                    showDatePicker = false
                },
                currentDate.year,
                currentDate.monthValue - 1,
                currentDate.dayOfMonth
            ).show()
        }

        if (showTimePicker) {
            val currentTime = LocalTime.now()
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    onUiStateChange(uiState.copy(time = selectedTime))
                    showTimePicker = false
                },
                currentTime.hour,
                currentTime.minute,
                true
            ).show()
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            CustomButtonOne(
                onClick = onChooseLocation,
                text = stringResource(R.string.location),
                iconResId = R.drawable.location_on_50dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            if (uiState.latitude != null && uiState.longitude != null) {
                Text(
                    text = address ?: "Getting address...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.date,
                onValueChange = { /* Do nothing */ },
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                    }
                },
                readOnly = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.time,
                onValueChange = { /* Do nothing */ },
                label = { Text("Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
                    }
                },
                readOnly = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { onUiStateChange(uiState.copy(description = it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButtonOne(
                onClick = onCreateEvent,
                text = stringResource(R.string.create),
                iconResId = R.drawable.add_circle_50dp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}