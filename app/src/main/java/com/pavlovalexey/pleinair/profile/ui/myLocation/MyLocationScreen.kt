package com.pavlovalexey.pleinair.profile.ui.myLocation

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.utils.uiComponents.CustomButtonOne


@Composable
fun MyLocationScreen(
    navController: NavController,
    viewModel: MyLocationViewModel = hiltViewModel(),
    onLocationSelected: (LatLng) -> Unit
) {
    val context = LocalContext.current
    val locationEnabled by viewModel.locationEnabled
    val cameraPositionState = rememberCameraPositionState {
        position = viewModel.cameraPositionState.value.position
    }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

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

    Scaffold(
        bottomBar = {
            BottomAppBar(
                backgroundColor = Color.White,
                        modifier = Modifier.height(100.dp)
            ) {
                CustomButtonOne(
                    text = stringResource(R.string.geo_mark),
                    iconResId = R.drawable.palette_30dp,
                    textColor = MaterialTheme.colors.primary,
                    iconColor = MaterialTheme.colors.primary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        selectedLocation?.let {
                            onLocationSelected(it)
                            navController.popBackStack()
                        } ?: run {
                            Toast.makeText(context, "Выберите местоположение", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                )
            }
        }
    ){
        if (locationEnabled) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    viewModel.updateUserLocation(latLng)
                }
            ) {
                selectedLocation?.let {
                    Marker(
                        state = MarkerState(position = it)
                    )
                }
            }
        } else {
            Text(text = "Геолокация выключена")
        }
    }
}

