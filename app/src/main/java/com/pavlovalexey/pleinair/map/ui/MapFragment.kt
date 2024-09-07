package com.pavlovalexey.pleinair.map.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.calendar.model.Event
import com.pavlovalexey.pleinair.databinding.FragmentMapBinding
import com.pavlovalexey.pleinair.profile.model.User
import com.pavlovalexey.pleinair.utils.CircleTransform
import com.squareup.picasso.Picasso

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var showOnlineOnly = false
    private var showEventsOnly = false

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
        val initialPosition = LatLng(59.9343, 30.3351) // Координаты центра Санкт-Петербурга
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 12f))
        showFilterDialog()
    }

    private fun showFilterDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выберите фильтр")
            .setItems(arrayOf("Показать только ивенты\n", "\nПоказать только онлайн пользователей\n", "\nПоказать всех пользователей")) { _, which ->
                when (which) {
                    0 -> {
                        showEventsOnly = true
                        showOnlineOnly = false
                        loadEventMarkers()
                    }
                    1 -> {
                        showOnlineOnly = true
                        showEventsOnly = false
                        loadUserLocationAndMarkers() // Загружаем только онлайн пользователей
                    }
                    2 -> {
                        showOnlineOnly = false
                        showEventsOnly = false
                        loadUserLocationAndMarkers() // Загружаем всех пользователей
                    }
                }
            }
            .show()
    }

    private fun loadUserLocationAndMarkers() {
        showLoadingIndicator(true)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Получаем местоположение пользователя
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val locationMap = document.get("location") as? Map<String, Any>
                if (locationMap != null) {
                    val latitude = locationMap["latitude"] as? Double
                    val longitude = locationMap["longitude"] as? Double

                    if (latitude != null && longitude != null) {
                        val latLng = LatLng(latitude, longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))

                        // Загружаем маркеры пользователей
                        loadUserMarkers()
                    } else {
                        // Обработка ситуации, когда latitude или longitude отсутствует
                    }
                } else {
                    // Если местоположение не найдено, можно обработать это
                }
            }
            .addOnFailureListener { exception ->
                // Обработка ошибки
            }
    }

    private fun loadEventMarkers() {
        showLoadingIndicator(true)

        firestore.collection("events")
            .get()
            .addOnSuccessListener { result ->
                googleMap.clear() // Очищаем карту перед добавлением новых маркеров

                for (document in result) {
                    val event = document.toObject(Event::class.java)

                    // Проверяем, что координаты присутствуют
                    if (event.latitude != 0.0 && event.longitude != 0.0) {
                        val location = LatLng(event.latitude, event.longitude)
                        addEventMarker(event, location)
                    }
                }

                showLoadingIndicator(false) // Скрываем ProgressBar
            }
            .addOnFailureListener { exception ->
                // Обработка ошибки
                showLoadingIndicator(false) // Скрываем ProgressBar
            }
    }

    private fun loadUserMarkers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                googleMap.clear() // Очищаем карту перед добавлением новых маркеров

                for (document in result) {
                    val user = document.toObject(User::class.java)

                    // Проверяем статус пользователя, если выбран режим "только онлайн"
                    if (!showOnlineOnly || user.isOnline == true) {
                        val locationMap = user.location
                        val latitude = locationMap?.get("latitude") as? Double
                        val longitude = locationMap?.get("longitude") as? Double

                        if (latitude != null && longitude != null) {
                            val location = LatLng(latitude, longitude)
                            addUserMarker(user, location)
                        }
                    }
                }

                showLoadingIndicator(false) // Скрываем ProgressBar
            }
            .addOnFailureListener { exception ->
                // Обработка ошибки
                showLoadingIndicator(false) // Скрываем ProgressBar
            }
    }

    private fun showLoadingIndicator(show: Boolean) {
        binding.loadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun addEventMarker(event: Event, location: LatLng) {
        val markerOptions = MarkerOptions()
            .position(location)
            .title(event.city)
            .snippet("${event.place} - ${event.date} ${event.time}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))

        val marker = googleMap.addMarker(markerOptions)
        marker?.tag = event // Связываем маркер с объектом Event
    }

    private fun addUserMarker(user: User, location: LatLng) {
        Picasso.get().load(user.profileImageUrl).transform(CircleTransform()).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                val markerOptions = MarkerOptions()
                    .position(location)
                    .title(user.name)
                    .snippet(user.locationName)
                    .icon(bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
                val marker = googleMap.addMarker(markerOptions)
                marker?.tag = user // Связываем маркер с объектом User
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

    private fun showEventDetailsDialog(event: Event) {
        AlertDialog.Builder(requireContext())
            .setTitle(event.city)
            .setMessage(
                "Место: ${event.place}\n" +
                        "Дата: ${event.date}\n" +
                        "Время: ${event.time}\n" +
                        "Описание: ${event.description}"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showUserDetailsDialog(user: User) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_user_details, null)
        val callToPaintButton = dialogView.findViewById<Button>(R.id.callToPaintButton)
        val addFriendButton = dialogView.findViewById<Button>(R.id.addFriendButton)
        val blockButton = dialogView.findViewById<Button>(R.id.blockButton)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(user.name)
            .setMessage(
                "\nТехники: ${user.artStyles?.joinToString(", ") ?: "Техники не выбраны"}\n\n" +
                        "Описание профиля: ${user.description ?: "Описания нет"}\n"
            )
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .create()

        alertDialog.show()

        callToPaintButton.setOnClickListener { showContextMenu(callToPaintButton) }
        addFriendButton.setOnClickListener { showContextMenu(addFriendButton) }
        blockButton.setOnClickListener { showContextMenu(blockButton) }
        closeButton.setOnClickListener { alertDialog.dismiss() }
    }

    private fun showContextMenu(view: View) {
        val contextMenu = PopupMenu(requireContext(), view)
        contextMenu.menuInflater.inflate(R.menu.context_menu, contextMenu.menu)
        contextMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_yes -> true
                R.id.menu_item_no -> true
                else -> false
            }
        }
        contextMenu.show()
    }
}
