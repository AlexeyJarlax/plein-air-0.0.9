package com.pavlovalexey.pleinair.calendar.ui.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.calendar.model.Event
import com.pavlovalexey.pleinair.main.ui.uiComponents.BackgroundImage
import kotlinx.coroutines.launch

@Composable
fun EventListScreen(
    viewModel: EventListViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val events = viewModel.events.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
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
                    // Search Box
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

@Composable
fun EventItem(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.city, style = MaterialTheme.typography.h6)
            Text(text = "Date: ${event.date}", style = MaterialTheme.typography.body2)
            Text(text = "Time: ${event.time}", style = MaterialTheme.typography.body2)
            Text(text = event.description, style = MaterialTheme.typography.body2)
        }
    }
}