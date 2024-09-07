package com.pavlovalexey.pleinair.calendar.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.pavlovalexey.pleinair.R

class EventMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_map, container, false)

        // Инициализация карты
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Обработка нажатия на кнопку подтверждения местоположения
        view.findViewById<Button>(R.id.btn_confirm_location).setOnClickListener {
            selectedLocation?.let {
                // Передаем координаты обратно в NewEventFragment через FragmentResultAPI
                val resultBundle = Bundle().apply {
                    putDouble("latitude", it.latitude)
                    putDouble("longitude", it.longitude)
                }
                parentFragmentManager.setFragmentResult("locationRequestKey", resultBundle)

                // Закрываем фрагмент карты
                parentFragmentManager.popBackStack()
            } ?: run {
                Toast.makeText(requireContext(), "Выберите местоположение", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Когда карта готова
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Обработка клика по карте для выбора местоположения
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Выбрано местоположение"))
            selectedLocation = latLng
        }

        // Установка начальной позиции карты (Петропавловская крепость)
        val initialPosition = LatLng(59.9500019, 30.3166718)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 15f))
    }
}
