package com.pavlovalexey.pleinair

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pavlovalexey.pleinair.main.ui.authScreen.AuthScreen
import com.pavlovalexey.pleinair.main.ui.termsScreen.TermsScreen
import com.pavlovalexey.pleinair.profile.ui.MyLocationScreen
import com.pavlovalexey.pleinair.profile.ui.ProfileScreen
import com.pavlovalexey.pleinair.settings.ui.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController, activity: Activity, modifier: Modifier = Modifier) {
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
                onCancel = {
                    activity.finish()
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
                },
                onCancel = {
                    activity.finish()
                },
                viewModel = hiltViewModel()
            )
        }

        composable("profile") {
            ProfileScreen(
                viewModel = hiltViewModel(),
                onNavigateToUserMap = { navController.navigate("userMap") },
                onMyLocation = {
                    navController.navigate("myLocation")
                },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                onExit = { activity.finish() }
            )
        }

        composable("myLocation") {
            MyLocationScreen(
                navController = navController,
                onLocationSelected = {
                    navController.navigate("profile") {
                        popUpTo("myLocation") { inclusive = true }
                    }
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
