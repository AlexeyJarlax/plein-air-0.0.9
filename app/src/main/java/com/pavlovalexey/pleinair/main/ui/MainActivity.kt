package com.pavlovalexey.pleinair.main.ui

/** Приложение построено Jetpack Compose с сингл-активити и отправной точкой MainActivity.
 * Вместо xml и фрагментов применил Jetpack Compose — фреймворк для создания UI на Android, основанный на декларативном подходе.
 *
 * Этапы входа в приложение:
 * 1 Этап - подписание соглашений в TermsScreen
 * 2 Этап - авторизация в AuthScreen
 * 3 Этап - MainActivity и фрагменты по всему функционалу приложения с навигацией через НавГраф и BottomNavBar. Первый: Профиль фрагмент.
 */

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.pavlovalexey.pleinair.PleinairTheme
import com.pavlovalexey.pleinair.main.ui.authScreen.AuthScreen
import com.pavlovalexey.pleinair.main.ui.authScreen.AuthViewModel
import com.pavlovalexey.pleinair.settings.ui.SettingsViewModel
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), OnMapReadyCallback {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var loginAndUserUtils: LoginAndUserUtils

    private lateinit var storage: FirebaseStorage
    private lateinit var mMap: GoogleMap
    private lateinit var progressBar: ProgressBar
    private var selectedLocation: GeoPoint? = null
    private val defaultLocation = GeoPoint(59.948612, 30.314633)    // Координаты Петропавловской крепости

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        setContent {
            val navController = rememberNavController()
            val settingsViewModel: SettingsViewModel = hiltViewModel()
//            val isNightMode by settingsViewModel.isNightMode.observeAsState(initial = false)
//            settingsViewModel.changeNightMode(isNightMode) уберу ночную тему пока не решил по стилям

            // проверка на авторизованного
            val authState by authViewModel.authState.collectAsState()

            PleinairTheme() {
                if (authState.isAuthenticated && auth.currentUser != null) {
                    MainScreen(navController = navController)
                } else {
                    AuthScreen(
                        navController = navController,
                        onAuthSuccess = {
                            navController.navigate("profile") {
                                popUpTo("auth") { inclusive = true }
                            }
                        },
                        onCancel = { finish() }
                    )
                }
            }
        }

        setupOnlineStatusListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        resetAuthState()
    }

    private fun restartActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun resetAuthState() {
        // Use the ViewModelProvider initialized in onCreate
        authViewModel.resetAuthState()
        googleSignInClient.signOut()
    }

    @Composable
    private fun logoutAndRevokeAccess() {
        authViewModel.signOut()
        googleSignInClient.revokeAccess().addOnCompleteListener {
            finish()
            startActivity(intent)
        }
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

            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Выбрано местоположение"))
            selectedLocation = GeoPoint(latLng.latitude, latLng.longitude)
        }
        val initialPosition = selectedLocation ?: defaultLocation
        val initialLatLng = LatLng(initialPosition.latitude, initialPosition.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 12f))
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        val windowInsetsController = window.insetsController
        if (windowInsetsController != null) {
            windowInsetsController.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsets.Type.systemBars())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
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
