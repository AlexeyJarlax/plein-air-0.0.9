package com.pavlovalexey.pleinair.main.ui

/** Приложение построено как синглактивити на фрагментах с вью моделями и отправной точкой MainActivity.
 * Вместо xml применил Jetpack Compose — фреймворк для создания UI на Android, основанный на декларативном подходе.
 *
 * 1 Этап - подписание соглашений в TermsScreen
 * 2 Этап - авторизация в AuthScreen
 * 3 Этап - MainActivity и фрагменты по всему функционалу приложения с навигацией через НавГраф и BottomNavBar
 */

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
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

    private var selectedLocation: LatLng? = null
    private val defaultLocation =
        LatLng(59.9500019, 30.3166718)    // Координаты Петропавловской крепости

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)

        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = hiltViewModel()
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isNightMode by settingsViewModel.isNightMode.observeAsState(initial = false)
            val authState by authViewModel.authState.collectAsState()

            PleinairTheme(darkTheme = isNightMode) {
                if (authState.isAuthenticated) {
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

    @Composable
    private fun logoutAndRevokeAccess() {
        val authViewModel: AuthViewModel = hiltViewModel()
        authViewModel.signOut()
        googleSignInClient.revokeAccess().addOnCompleteListener {
            // перезапуск активити или навигация на экран авторизации
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
                // Ошибка обработки
            }
        })
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