package com.pavlovalexey.pleinair.main.ui

/** точка входа в приложение. SingleActivity(+Fragments) */

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.auth.AuthActivity
import com.pavlovalexey.pleinair.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            // Если пользователь не авторизован, перенаправляем его на AuthActivity
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
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
//        bottomNavigationView.menu.findItem(R.id.profileFragment).setOnMenuItemClickListener {
//            if (auth.currentUser != null) {
//                navController.navigate(R.id.profileFragment)
//            } else {
//                startActivity(Intent(this, AuthActivity::class.java))
//            }
//            true
//        }
    }
}

