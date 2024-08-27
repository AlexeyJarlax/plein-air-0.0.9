package com.pavlovalexey.pleinair.main.ui

/** точка входа в приложение после авторизации.*/

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.initialize
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.auth.AuthActivity
import com.pavlovalexey.pleinair.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация Firebase
        com.google.firebase.Firebase.initialize(context = this)
        db = com.google.firebase.Firebase.firestore
        auth = FirebaseAuth.getInstance()
        storage = com.google.firebase.Firebase.storage

        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

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
                R.id.newPlaylistFragment, R.id.playerFragment, R.id.openPlaylistFragment, R.id.editPlaylistFragment -> {
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

        // Создаем начальные данные профиля пользователя
        val userProfile = hashMapOf(
            "name" to (user.displayName ?: "User Name"),
            "profileImageUrl" to (user.photoUrl?.toString() ?: "default_url")
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
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting user profile", e)
            }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}