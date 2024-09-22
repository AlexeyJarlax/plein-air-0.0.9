package com.pavlovalexey.pleinair.profile.ui.myLocation

import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.State
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MyLocationViewModel @Inject constructor(
    application: Application,
    private val firebaseUserManager: FirebaseUserManager
) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val _locationEnabled = mutableStateOf(false)
    val locationEnabled: State<Boolean> = _locationEnabled

    private val _cameraPositionState = mutableStateOf(
        CameraPositionState(
            position = CameraPosition.fromLatLngZoom(LatLng(59.9343, 30.3351), 10f)
        )
    )
    val cameraPositionState: State<CameraPositionState> = _cameraPositionState

    fun checkLocationPermission() {
        viewModelScope.launch {
            val permissionGranted = ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (permissionGranted) {
                _locationEnabled.value = true
                getLastLocation()
            } else {
                _locationEnabled.value = false
            }
        }
    }

    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    _cameraPositionState.value = CameraPositionState(
                        position = CameraPosition.fromLatLngZoom(userLatLng, 12f)
                    )
                }
            }
        }
    }

    fun updateUserLocation(location: GeoPoint) {
        val userId = firebaseUserManager.getCurrentUserId()
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                firebaseUserManager.updateUserLocation(
                    userId = userId,
                    location = geoPoint,
                    collectionName = "users",
                    onSuccess = {
                        // Успешно обновлено
                    },
                    onFailure = { e ->
                        // Обработка ошибки
                    }
                )
            }
        }
    }
}