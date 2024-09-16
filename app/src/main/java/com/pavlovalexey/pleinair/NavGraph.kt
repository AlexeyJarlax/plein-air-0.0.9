package com.pavlovalexey.pleinair

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pavlovalexey.pleinair.profile.ui.ProfileScreen
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "profile"
    ) {
        composable("profile") {
            ProfileScreen(
                onNavigateToUserMap = { navController.navigate("userMap") },
                viewModel = viewModel(),
                onContinue = { /* Обработка продолжения */ },
                onLogout = { /* Обработка выхода */ }
            )
        }

        composable("userMap") {
            UserMapScreen(
                // передайте необходимые параметры или ViewModel
            )
        }

        composable("settings") {
            SettingsScreen(
                // передайте необходимые параметры или ViewModel
            )
        }

        composable("map") {
            MapScreen(
                // передайте необходимые параметры или ViewModel
            )
        }

        composable("calendar") {
            CalendarScreen(
                onNavigateToNewEvent = { navController.navigate("newEvent") }
                // передайте необходимые параметры или ViewModel
            )
        }

        composable("newEvent") {
            NewEventScreen(
                onNavigateToEventMap = { navController.navigate("eventMap") }
                // передайте необходимые параметры или ViewModel
            )
        }

        composable("eventMap") {
            EventMapScreen(
                // передайте необходимые параметры или ViewModel
            )
        }
    }
}