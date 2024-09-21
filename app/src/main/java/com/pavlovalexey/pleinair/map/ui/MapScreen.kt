package com.pavlovalexey.pleinair.map.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.map.data.ImageRepository

@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val imageRepository = ImageRepository()
    val users by viewModel.users.collectAsState()
    val events by viewModel.events.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var filterDialogShown by remember { mutableStateOf(true) }
    var selectedOption by remember { mutableStateOf("") }
    val iconPalette: BitmapDescriptor? = BitmapDescriptorFactory.fromResource(R.drawable.palette_30dp)
    if (filterDialogShown) {
        FilterDialog(onOptionSelected = { option ->
            selectedOption = option
            filterDialogShown = false
            when (option) {
                "Показать ивенты" -> viewModel.loadEvents()
                "Показать пользователей онлайн" -> viewModel.loadUsers(onlyOnline = true)
                "Показать пользователей офлайн" -> viewModel.loadUsers(onlyOnline = false)
            }
        })
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(59.9343, 30.3351), 12f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    if (selectedOption == "Показать ивенты") {
                        events.forEach { event ->
                            val position = LatLng(event.latitude, event.longitude)
                            Marker(
                                state = MarkerState(position = position),
                                title = event.city,
                                snippet = "${event.date} ${event.time}",
                                icon = iconPalette
                            )
                        }
                    } else {
                        users.forEach { user ->
                            val location = user.location
                            if (location != null) {
                                val position = LatLng(location.latitude, location.longitude)
                                val icon = getUserMarkerIcon(user.profileImageUrl) ?: BitmapDescriptorFactory.defaultMarker()
                                Marker(
                                    state = MarkerState(position = position),
                                    title = user.name,
                                    snippet = user.locationName,
                                    icon = icon
                                )
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { filterDialogShown = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Фильтр")
            }
        }
    }
}

/**
Как преобразовывать между GeoPoint и LatLng:
Из LatLng в GeoPoint:
kotlin
val geoPoint = GeoPoint(latLng.latitude, latLng.longitude)
Из GeoPoint в LatLng:
kotlin
val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
 */
