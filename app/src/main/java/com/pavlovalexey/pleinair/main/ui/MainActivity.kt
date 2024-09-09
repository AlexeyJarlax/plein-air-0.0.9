package com.pavlovalexey.pleinair.main.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.auth.ui.AuthActivity
import com.pavlovalexey.pleinair.databinding.ActivityMainBinding
import com.pavlovalexey.pleinair.profile.ui.UserMapFragment
import kotlin.random.Random

class MainActivity : AppCompatActivity(), UserMapFragment.OnLocationSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var mMap: GoogleMap
    private lateinit var progressBar: ProgressBar

    private var selectedLocation: LatLng? = null
    private val defaultLocation = LatLng(59.9500019, 30.3166718)    // Координаты Петропавловской крепости

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        hideSystemUI()
        progressBar = findViewById(R.id.loading_indicator)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.menu_background))
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.mapFragment, R.id.userMapFragment, R.id.eventMapFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
        setupOnlineStatusListener()
    }

    private fun setupOnlineStatusListener() {
        val database = FirebaseDatabase.getInstance()
        val userStatusDatabaseRef = database.getReference("status/${FirebaseAuth.getInstance().currentUser?.uid}")

        val onlineStatus = mapOf(
            "state" to "online",
            "last_changed" to ServerValue.TIMESTAMP
        )

        val offlineStatus = mapOf(
            "state" to "offline",
            "last_changed" to ServerValue.TIMESTAMP
        )

        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    userStatusDatabaseRef.onDisconnect().setValue(offlineStatus)
                    userStatusDatabaseRef.setValue(onlineStatus)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Обработка ошибки
            }
        })
    }

    private fun setUserProfile() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val userDocRef = db.collection("users").document(userId)

        // Генерация случайного забавного имени
        val funnyNames = listOf("FlyingPanda", "JumpingCat", "DancingBanana", "CrazyKoala", "SingingPenguin")
        val randomName = funnyNames[Random.nextInt(funnyNames.size)]

        // Путь к дефолтной аватарке
        val defaultAvatar = R.drawable.defaut_avatar_120dp

        // Создаем начальные данные профиля пользователя
        val userProfile = hashMapOf(
            "name" to randomName,
            "profileImageUrl" to defaultAvatar, // Используем дефолтную аватарку
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
                    } else {
                        // Загрузка текущего местоположения из Firestore
                        val location = document.get("location") as? Map<*, *>
                        val latitude = location?.get("latitude") as? Double
                        val longitude = location?.get("longitude") as? Double
                        if (latitude != null && longitude != null) {
                            selectedLocation = LatLng(latitude, longitude)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting user profile", e)
            }
    }

    override fun onLocationSelected(location: LatLng) {
        Log.d(TAG, "Location selected: $location")
        selectedLocation = location

        // Обновление местоположения пользователя в Firestore
        val user = auth.currentUser ?: return
        val userId = user.uid
        val userDocRef = db.collection("users").document(userId)

        val updatedLocation = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )

        userDocRef.update("location", updatedLocation)
            .addOnSuccessListener {
                Log.d(TAG, "User location updated to: $location")
                Toast.makeText(this, "Координаты обновлены!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating user location", e)
                Toast.makeText(this, "Ошибка координат!", Toast.LENGTH_SHORT).show()
            }
    }

    fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Выбрано местоположение"))
            selectedLocation = latLng
        }

        // Установите начальную позицию карты на выбранное местоположение или значение по умолчанию
        val initialPosition = selectedLocation ?: defaultLocation
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 15f))
    }

    fun logoutAndRevokeAccess() {
        auth.signOut()
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
