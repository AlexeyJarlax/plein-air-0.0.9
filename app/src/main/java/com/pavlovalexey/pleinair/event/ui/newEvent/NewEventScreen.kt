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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pavlovalexey.pleinair.event.model.NewEventUiState

@Composable
fun NewEventScreen(
    navController: NavController,
) {
    val viewModel: NewEventViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState = remember { mutableStateOf(NewEventUiState()) }

    val creationStatus by viewModel.creationStatus.observeAsState()
    val event by viewModel.event.observeAsState()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val location = savedStateHandle?.getStateFlow<Pair<Double, Double>?>("location", null)?.collectAsState()

    if (location?.value != null) {
        val (lat, lng) = location.value!!
        uiState.value = uiState.value.copy(latitude = lat, longitude = lng)
        // Clear the saved state to prevent re-triggering
        savedStateHandle?.remove<Pair<Double, Double>>("location")
    }

    LaunchedEffect(creationStatus) {
        when (creationStatus) {
            is CreationStatus.Loading -> {
                // Show loading indicator
            }

            is CreationStatus.Success -> {
                // Navigation back once event is created
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
                title = { Text("Создание нового события") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        content = { innerPadding ->
            EventCreationContent(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState.value,
                onUiStateChange = { newState -> uiState.value = newState },
                onCreateEvent = {
                    viewModel.createEvent(uiState.value)
                },
                onChooseLocation = {
                    // Navigate to map screen, passing the selected city
                    navController.navigate("map?city=${uiState.value.city}")
                }
            )
        }
    )
}