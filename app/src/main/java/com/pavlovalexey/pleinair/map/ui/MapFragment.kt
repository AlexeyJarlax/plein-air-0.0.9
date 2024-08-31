package com.pavlovalexey.pleinair.map.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentMapBinding
import com.pavlovalexey.pleinair.profile.model.User
import com.pavlovalexey.pleinair.utils.CircleTransform
import com.squareup.picasso.Picasso

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.exitButton.setOnClickListener {
            findNavController().popBackStack() // Возвращаемся на предыдущий экран
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        loadUserMarkers()
    }

    private fun loadUserMarkers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    addUserMarker(user)
                }
            }
            .addOnFailureListener { exception ->
                // Обработка ошибки
            }
    }

    private fun addUserMarker(user: User) {
        val location = LatLng(user.location["latitude"] as Double, user.location["longitude"] as Double)

        Picasso.get().load(user.profileImageUrl).transform(CircleTransform()).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                val markerOptions = MarkerOptions()
                    .position(location)
                    .title(user.name)
                    .snippet(user.locationName)
                    .icon(bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
                googleMap.addMarker(markerOptions)
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                // Обработка ошибки
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                // Подготовка к загрузке
            }
        })

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
    }
}
