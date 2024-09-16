package com.pavlovalexey.pleinair

import ProfileScreen
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pavlovalexey.pleinair.auth.ui.AuthScreen
import com.pavlovalexey.pleinair.auth.ui.TermsScreen
import com.pavlovalexey.pleinair.auth.ui.TermsViewModel
import com.pavlovalexey.pleinair.main.ui.MainScreen

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "terms"
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
                navController = navController
            )
        }
        composable("main") {
            MainScreen(onLogout = {
                navController.navigate("auth") {
                    popUpTo("terms") { inclusive = true }
                }
            })
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
//        composable("userMap") {
//            UserMapScreen(
//                onBack = { navController.popBackStack() }
//            )
//        }
    }
}
