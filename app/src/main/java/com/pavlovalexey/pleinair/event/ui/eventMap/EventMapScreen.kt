package com.pavlovalexey.pleinair.event.ui.eventMap

import androidx.compose.runtime.*
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.pavlovalexey.pleinair.profile.ui.MyLocationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun EventMapScreen(
    navController: NavController,
    viewModel: MyLocationViewModel = hiltViewModel(),
    city: String,
    onLocationSelected: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var mapProperties by remember { mutableStateOf(MapProperties()) }
    val cameraPositionState = rememberCameraPositionState()
    var markerPosition by remember { mutableStateOf<LatLng?>(null) }

    // Геокодирование города для получения начальной позиции
    LaunchedEffect(city) {
        withContext(Dispatchers.IO) {
            val geocoder = Geocoder(context)
            try {
                val addresses = geocoder.getFromLocationName(city, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    withContext(Dispatchers.Main) {
                        // Перемещение камеры к найденной позиции
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    }
                } else {
                    // Если геокодирование не удалось, установка начальной позиции в Москве
                    withContext(Dispatchers.Main) {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(55.75, 37.61), 12f))
                    }
                }
            } catch (e: IOException) {
                // Обработка исключения
                withContext(Dispatchers.Main) {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(55.75, 37.61), 12f))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выбор местоположения") },
                actions = {
                    IconButton(onClick = {
                        markerPosition?.let {
                            onLocationSelected(it.latitude, it.longitude)
                        } ?: run {
                            // Показываем сообщение, если местоположение не выбрано
                            Toast.makeText(context, "Выберите местоположение", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Подтвердить местоположение")
                    }
                }
            )
        }
    ) { innerPadding ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Применяем внутренние отступы
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markerPosition = latLng
            },
            properties = mapProperties
        ) {
            markerPosition?.let {
                Marker(
                    state = MarkerState(position = it)
                )
            }
        }
    }
}
