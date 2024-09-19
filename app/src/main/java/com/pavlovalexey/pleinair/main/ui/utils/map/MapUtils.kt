package com.pavlovalexey.pleinair.main.ui.utils.map

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.pavlovalexey.pleinair.R

//object MapUtils {
//
//    fun setupMap(
//        googleMap: GoogleMap,
//        latitude: Double?,
//        longitude: Double?,
//        onLocationSelected: (LatLng) -> Unit
//    ) {
//        val defaultLatitude = 55.75
//        val defaultLongitude = 37.61
//        val initialPosition = if (latitude != null && longitude != null) {
//            LatLng(latitude, longitude)
//        } else {
//            LatLng(defaultLatitude, defaultLongitude)
//        }
//
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 12f))
//        googleMap.setOnMapClickListener { latLng ->
//            googleMap.clear()
//            googleMap.addMarker(MarkerOptions().position(latLng).title("Выбрано местоположение"))
//            onLocationSelected(latLng)
//        }
//    }
//
//    fun navigateToEventMapFragment(
//        fragment: Fragment,
//        cityCoordinatesMap: Map<String, LatLng>,
//        cityName: String
//    ) {
//        val cityCoordinates = cityCoordinatesMap[cityName]
//        if (cityCoordinates != null) {
//            val bundle = Bundle().apply {
//                putDouble("latitude", cityCoordinates.latitude)
//                putDouble("longitude", cityCoordinates.longitude)
//            }
//            fragment.findNavController().navigate(
//                R.id.action_newEventFragment_to_eventMapFragment,
//                bundle
//            )
//        } else {
//            Toast.makeText(fragment.requireContext(), "Выберите город", Toast.LENGTH_SHORT).show()
//        }
//    }
//}