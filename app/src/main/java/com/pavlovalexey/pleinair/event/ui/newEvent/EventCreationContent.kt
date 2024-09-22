package com.pavlovalexey.pleinair.event.ui.newEvent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun EventCreationContent(
    modifier: Modifier = Modifier,
    uiState: NewEventUiState,
    cities: List<String>,
    onUiStateChange: (NewEventUiState) -> Unit,
    onCreateEvent: () -> Unit,
    onChooseLocation: () -> Unit,
    onCitySelected: (String) -> Unit
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var address by remember(uiState.latitude, uiState.longitude) {
        mutableStateOf<String?>(null)
    }
    val isButtonLocationVisible = false
    val currentDateTime = LocalDateTime.now()
    val maxDateTime = currentDateTime.plusHours(100)

    LaunchedEffect(key1 = uiState.latitude, key2 = uiState.longitude) {
        if (uiState.latitude != null && uiState.longitude != null) {
            address = getAddressFromLatLng(context, uiState.latitude, uiState.longitude)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    if (selectedDate.isBefore(currentDateTime.toLocalDate()) || selectedDate.isAfter(maxDateTime.toLocalDate())) {
                        Toast.makeText(context, "+100 часов от текущего времени - допустимые время и дата}", Toast.LENGTH_LONG).show()
                        Toast.makeText(context, "Максимальная дата: ${maxDateTime.toLocalDate()}", Toast.LENGTH_LONG).show()
                    } else {
                        onUiStateChange(uiState.copy(date = selectedDate.toString()))
                    }
                    showDatePicker = false
                },
                currentDate.year,
                currentDate.monthValue - 1,
                currentDate.dayOfMonth
            ).apply {
                datePicker.minDate = currentDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
//                datePicker.maxDate = maxDateTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
            }.show()
        }

        if (showTimePicker) {
            val currentTime = LocalTime.now()
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val selectedTime = LocalTime.of(hourOfDay, minute)
                    val selectedDateTime = LocalDateTime.of(uiState.date.toLocalDate(), selectedTime)
                    if (selectedDateTime.isBefore(currentDateTime) || selectedDateTime.isAfter(maxDateTime)) {
                        Toast.makeText(context, "+100 часов от текущего времени - допустимые время и дата}", Toast.LENGTH_LONG).show()
                        Toast.makeText(context, "Максимальное время: ${maxDateTime}", Toast.LENGTH_LONG).show()
                    } else {
                        onUiStateChange(uiState.copy(time = selectedTime.toString()))
                    }
                    showTimePicker = false
                },
                currentTime.hour,
                currentTime.minute,
                true
            ).show()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isButtonLocationVisible) {
                CustomButtonOne(
                    onClick = onChooseLocation,
                    text = stringResource(R.string.location),
                    iconResId = R.drawable.location_on_50dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
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
                value = uiState.date ?: "",
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
                value = uiState.time ?: "",
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
                onClick = {
                    val selectedDateTime = LocalDateTime.of(uiState.date.toLocalDate(), uiState.time.toLocalTime())
                    if (selectedDateTime.isBefore(currentDateTime) || selectedDateTime.isAfter(maxDateTime)) {
                        Toast.makeText(context, "+100 часов от текущего времени - допустимые время и дата}", Toast.LENGTH_LONG).show()
                        Toast.makeText(context, "Максимальное время: ${maxDateTime}", Toast.LENGTH_LONG).show()
                    } else {
                        onCreateEvent()
                    }
                },
                text = stringResource(R.string.create),
                iconResId = R.drawable.add_circle_50dp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

fun String.toLocalDate(): LocalDate {
    return LocalDate.parse(this)
}

fun String.toLocalTime(): LocalTime {
    return LocalTime.parse(this)
}