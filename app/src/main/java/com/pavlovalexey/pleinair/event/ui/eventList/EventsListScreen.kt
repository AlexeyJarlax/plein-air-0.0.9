package com.pavlovalexey.pleinair.event.ui.eventList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.event.model.Event
import com.pavlovalexey.pleinair.utils.uiComponents.BackgroundImage
import kotlinx.coroutines.launch

@Composable
fun EventListScreen(
    navController: NavHostController,
    viewModel: EventListViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val events = viewModel.events.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("new_event") },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Event")
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                BackgroundImage(imageResId = R.drawable.back_lay)
                // Main Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            scope.launch {
                                viewModel.searchEvents(query)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search events") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Events List
                    LazyColumn {
                        items(events.value) { event ->
                            EventItem(event = event)
                        }
                    }
                }
            }
        }
    )
}