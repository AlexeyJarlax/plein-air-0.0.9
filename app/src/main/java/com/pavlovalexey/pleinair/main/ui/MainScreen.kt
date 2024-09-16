package com.pavlovalexey.pleinair.main.ui

import ProfileScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
            composable("profile") {
                ProfileScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToUserMap = { navController.navigate("userMap") },
                    onContinue = { /* Handle continue */ },
                    onLogout = {
                        onLogout() // Use the provided onLogout callback
                    },
                    onExit = { /* Handle exit */ }
                )
            }
        }
    }
}
