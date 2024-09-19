package com.pavlovalexey.pleinair.event.ui.newEvent

import android.app.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
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
import com.pavlovalexey.pleinair.utils.uiComponents.CustomButtonOne
import java.time.LocalDate

@Composable
fun EventCreationContent(
    modifier: Modifier = Modifier, // Added parameter
    uiState: NewEventUiState,
    onUiStateChange: (NewEventUiState) -> Unit,
    onCreateEvent: () -> Unit,
    onChooseLocation: () -> Unit
) {

    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                onUiStateChange(uiState.copy(date = selectedDate))
                showDatePicker = false
            },
            LocalDate.now().year,
            LocalDate.now().monthValue - 1,
            LocalDate.now().dayOfMonth
        )
        datePickerDialog.show()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        OutlinedTextField(   // City Input
            value = uiState.city,
            onValueChange = { onUiStateChange(uiState.copy(city = it)) },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomButtonOne( // Location Input
            onClick = onChooseLocation,
            text = stringResource(R.string.location),
            iconResId = R.drawable.location_on_50dp,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.latitude != null && uiState.longitude != null) {
            Text(
                text = "Selected Location: Lat ${uiState.latitude}, Lng ${uiState.longitude}",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date Input
        OutlinedTextField(
            value = uiState.date,
            onValueChange = { /* Do nothing */ },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                }
            },
            readOnly = true // To prevent manual input
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Time Input
        OutlinedTextField(
            value = uiState.time,
            onValueChange = { onUiStateChange(uiState.copy(time = it)) },
            label = { Text("Time") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { /* Open time picker */ }) {
                    Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
                }
            },
            readOnly = true // To prevent manual input
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Description Input
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { onUiStateChange(uiState.copy(description = it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))


        CustomButtonOne( // Create Event Button
            onClick = onCreateEvent,
            text = stringResource(R.string.create),
            iconResId = R.drawable.add_circle_50dp,
            modifier = Modifier.align(Alignment.End)
        )
    }
}