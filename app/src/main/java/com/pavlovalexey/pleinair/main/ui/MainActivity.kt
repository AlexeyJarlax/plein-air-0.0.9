package com.pavlovalexey.pleinair.main.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.auth.AuthActivity
import com.pavlovalexey.pleinair.databinding.ActivityMainBinding
import com.pavlovalexey.pleinair.map.ui.MapFragment

class MainActivity : AppCompatActivity(), MapFragment.OnLocationSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    // Координаты Петропавловской крепости
    private val defaultLocation = LatLng(59.9500019, 30.3166718)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация Firebase
        com.google.firebase.Firebase.initialize(this)
        db = com.google.firebase.Firebase.firestore
        auth = FirebaseAuth.getInstance()
        storage = com.google.firebase.Firebase.storage

        if (auth.currentUser == null) {
            // Если пользователь не авторизован, перенаправляем его на AuthActivity
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        // Установка профиля пользователя в Firestore
        setUserProfile()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.newPlaylistFragment, R.id.playerFragment, R.id.openPlaylistFragment, R.id.editPlaylistFragment, R.id.mapFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setUserProfile() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val userDocRef = db.collection("users").document(userId)

        // Создаем начальные данные профиля пользователя, включая координаты по умолчанию
        val userProfile = hashMapOf(
            "name" to (user.displayName ?: "User Name"),
            "profileImageUrl" to (user.photoUrl?.toString() ?: "default_url"),
            "location" to hashMapOf(
                "latitude" to defaultLocation.latitude,
                "longitude" to defaultLocation.longitude
            )
        )

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Если документ не существует, создаем его
                    userDocRef.set(userProfile)
                        .addOnSuccessListener {
                            Log.d(TAG, "User profile created with ID: $userId")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error creating user profile", e)
                        }
                } else {
                    // Если документ существует, проверяем наличие координат
                    if (!document.contains("location")) {
                        userDocRef.update("location", userProfile["location"])
                            .addOnSuccessListener {
                                Log.d(TAG, "Default location set for user ID: $userId")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error setting default location", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting user profile", e)
            }
    }

    override fun onLocationSelected(location: LatLng) {
        // Обработка выбранного местоположения
        Log.d(TAG, "Location selected: $location")
        // Вы можете обновить местоположение пользователя в Firestore или сделать что-то другое
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
