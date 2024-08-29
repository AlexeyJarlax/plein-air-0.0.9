package com.pavlovalexey.pleinair.map.ui

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

class MapFragment : Fragment(), OnMapReadyCallback {

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
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        view.findViewById<Button>(R.id.btn_confirm_location).setOnClickListener {
            selectedLocation?.let {
                listener?.onLocationSelected(it)
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

        // При необходимости, установите начальную позицию карты
        val initialPosition = LatLng(0.0, 0.0)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 10f))
    }
}
