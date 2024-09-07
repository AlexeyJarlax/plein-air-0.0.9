package com.pavlovalexey.pleinair.profile.ui

import android.content.Context
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

class UserMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null
    private var listener: OnLocationSelectedListener? = null

    interface OnLocationSelectedListener {
        fun onLocationSelected(location: LatLng)
    }

    fun setOnLocationSelectedListener(listener: OnLocationSelectedListener) {
        this.listener = listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLocationSelectedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnLocationSelectedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        view.findViewById<Button>(R.id.btn_confirm_location).setOnClickListener {
            selectedLocation?.let {
                // Обновляем координаты пользователя в Firebase
                listener?.onLocationSelected(it)
                // Закрываем фрагмент карты и возвращаемся назад
                parentFragmentManager.popBackStack()
            } ?: run {
                Toast.makeText(requireContext(), "Выберите местоположение", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Выбрано местоположение"))
            selectedLocation = latLng
        }

        // Установите начальную позицию карты на Петропавловскую крепость
        val initialPosition = LatLng(59.9500019, 30.3166718)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 15f))
    }
}
