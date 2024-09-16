package com.pavlovalexey.pleinair.main.ui

import ProfileScreen
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pavlovalexey.pleinair.auth.ui.AuthScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val bottomNavController = rememberNavController()
    val showBottomNav by remember { mutableStateOf(true) } // Update based on the current screen

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(navController = bottomNavController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "profile",
            Modifier.padding(innerPadding)
        ) {

            composable("profile") { ProfileScreen(
                viewModel = hiltViewModel(),
                onNavigateToUserMap = { navController.navigate("userMap") },
                onContinue = { /* Handle continue */ },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                onExit = { /* Handle exit */ }
            ) }
            composable("auth") { AuthScreen(navController = navController) }
        }
    }
}