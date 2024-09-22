package com.pavlovalexey.pleinair.event.ui.newEvent

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.utils.uiComponents.CustomYesOrNoDialog

@Composable
fun NewEventScreen(
    navController: NavController,
    onEventLocation: () -> Unit,
) {
    val viewModel: NewEventViewModel = hiltViewModel()
    val context = LocalContext.current
    val creationStatus by viewModel.creationStatus.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val cities = loadCitiesFromFile()
    val existingEvent by viewModel.existingEvent.observeAsState()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val location =
        savedStateHandle?.getStateFlow<Pair<Double, Double>?>("location", null)?.collectAsState()
    var showDeleteEventDialog by remember { mutableStateOf(false) }

    if (location?.value != null) {
        val (lat, lng) = location.value!!
        viewModel.updateUiState(uiState.copy(latitude = lat, longitude = lng))
        savedStateHandle?.remove<Pair<Double, Double>>("location")
    }

    existingEvent?.let {
        showDeleteEventDialog = true
    }

    if (showDeleteEventDialog) {
        CustomYesOrNoDialog(
            title = stringResource(R.string.confirmation),
            text = stringResource(R.string.confirmation_del_event),
            onDismiss = {
                showDeleteEventDialog = false
                navController.popBackStack()
            },
            onConfirm = {
                viewModel.deleteExistingEventAndCreateNew()
                showDeleteEventDialog = false
            }
        )
    }

    LaunchedEffect(creationStatus) {
        when (creationStatus) {
            is CreationStatus.Loading -> {
                // Show loading indicator
            }
            is CreationStatus.Success -> {
                // Navigate back after event creation
                navController.popBackStack()
            }
            is CreationStatus.Error -> {
                Toast.makeText(
                    context,
                    (creationStatus as CreationStatus.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { innerPadding ->
            EventCreationContent(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                cities = cities,
                onUiStateChange = { newState -> viewModel.updateUiState(newState) },
                onCreateEvent = { viewModel.createEvent() },
                onChooseLocation = { navController.navigate("event_location?city=${uiState.city}") },
                onCitySelected = { navController.navigate("event_location?city=${uiState.city}") }
            )
        }
    )
}