package com.pavlovalexey.pleinair.main.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.auth.AuthActivity
import com.pavlovalexey.pleinair.databinding.ActivityMainBinding
import com.pavlovalexey.pleinair.map.ui.UserMapFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : AppCompatActivity(), UserMapFragment.OnLocationSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var mMap: GoogleMap
    private lateinit var googleSignInClient: GoogleSignInClient

    // Переменная для хранения выбранного местоположения
    private var selectedLocation: LatLng? = null

    // Координаты Петропавловской крепости
    private val defaultLocation = LatLng(59.9500019, 30.3166718)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация Firebase
        com.google.firebase.Firebase.initialize(this)
        db = com.google.firebase.Firebase.firestore
        auth = FirebaseAuth.getInstance()
        storage = com.google.firebase.Firebase.storage
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
                R.id.mapFragment, R.id.userMapFragment -> {
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
        // Обработка выбранного местоположения
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
        googleSignInClient.revokeAccess().addOnCompleteListener {
            // После выхода из аккаунта можно перенаправить пользователя на экран авторизации
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    fun onLogout() {
        logoutAndRevokeAccess()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}