package com.pavlovalexey.pleinair.main.ui

/** Приложение построено как синглактивити на фрагментах с отправной точкой MainActivity.
 * TermsActivity и AuthActivity выделены как отдельные активности чтобы изолировать
 * от основной структуры фрагментов и навигации через НавХостКонтроллер.
 * Вместо xml применил Jetpack Compose — фреймворк для создания UI на Android, основанный на декларативном подходе.
 *
 * 1 Этап - подписание соглашений в TermsActivity
 * 2 Этап - авторизация в AuthActivity
 * 3 Этап - MainActivity и фрагменты по всему функционалу приложения с навигацией через НавГраф
 */

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pavlovalexey.pleinair.MainNavGraph
import com.pavlovalexey.pleinair.PleinairTheme
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.auth.ui.AuthActivity
import com.pavlovalexey.pleinair.databinding.ActivityMainBinding
import com.pavlovalexey.pleinair.profile.ui.UserMapFragment
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), UserMapFragment.OnLocationSelectedListener, OnMapReadyCallback {

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore
    @Inject lateinit var googleSignInClient: GoogleSignInClient
    @Inject lateinit var loginAndUserUtils: LoginAndUserUtils

    private lateinit var binding: ActivityMainBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var mMap: GoogleMap
    private lateinit var progressBar: ProgressBar

    private var selectedLocation: LatLng? = null
    private val defaultLocation = LatLng(59.9500019, 30.3166718)    // Координаты Петропавловской крепости

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PleinairTheme {
                val navController = rememberNavController()
                MainNavGraph(navController = navController)
            }
        }

        if (auth.currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemUI()
        progressBar = findViewById(R.id.loading_indicator)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
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
        val userStatusDatabaseRef = database.getReference("status/${auth.currentUser?.uid}")

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
                // Ошибка обработки
            }
        })
    }

    override fun onLocationSelected(location: LatLng) {
        selectedLocation = location
        val user = auth.currentUser ?: return
        val userId = user.uid
        val userDocRef = firestore.collection("users").document(userId)

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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Выбрано местоположение"))
            selectedLocation = latLng
        }
        val initialPosition = selectedLocation ?: defaultLocation
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 12f))
    }

    fun logoutAndRevokeAccess() {
        loginAndUserUtils.logout()
        googleSignInClient.revokeAccess().addOnCompleteListener {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
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