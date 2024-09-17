package com.pavlovalexey.pleinair

import ProfileScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pavlovalexey.pleinair.main.ui.authScreen.AuthScreen
import com.pavlovalexey.pleinair.main.ui.termsScreen.TermsScreen
import com.pavlovalexey.pleinair.settings.ui.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "terms",
        modifier = modifier
    ) {
        composable("terms") {
            TermsScreen(
                onContinue = {
                    navController.navigate("auth") {
                        popUpTo("terms") { inclusive = true }
                    }
                },
                viewModel = hiltViewModel()
            )
        }
        composable("auth") {
            AuthScreen(
                navController = navController,
                onAuthSuccess = {
                    navController.navigate("profile") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                viewModel = hiltViewModel(),
                onNavigateToUserMap = { navController.navigate("userMap") },
                onContinue = { /* Handle continue */ },
                onLogout = {
                    // Logout logic
                    navController.navigate("auth") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                onExit = { /* Handle exit */ }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}