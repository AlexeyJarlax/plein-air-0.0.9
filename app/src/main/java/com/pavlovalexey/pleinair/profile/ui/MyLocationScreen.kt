package com.pavlovalexey.pleinair.profile.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MyLocationScreen(
    viewModel: MyLocationViewModel = hiltViewModel(),
    onLocationSelected: (LatLng) -> Unit
) {
    val context = LocalContext.current
    val locationEnabled by viewModel.locationEnabled
    val cameraPositionState by viewModel.cameraPositionState
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) } // Переменная для хранения координат маркера

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.checkLocationPermission()
        } else {
            Toast.makeText(context, "Геолокация не включена", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationEnabled) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng // Сохраняем координаты клика
                    viewModel.updateUserLocation(latLng)
                    onLocationSelected(latLng)
                }
            ) {
                // Если выбранное местоположение есть, отображаем маркер
                selectedLocation?.let { Marker(position = it) }
            }
        } else {
            Text(text = "Геолокация выключена", modifier = Modifier.align(Alignment.Center))
        }

        Button(
            onClick = {
                selectedLocation?.let {
                    onLocationSelected(it)
                    viewModel.updateUserLocation(it) // Вызываем updateUserLocation с координатами маркера
                } ?: run {
                    Toast.makeText(context, "Выберите местоположение", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Подтвердить")
        }
    }
}}