package com.pavlovalexey.pleinair.navigation

import androidx.compose.runtime.Composable

@Composable
fun NavGraph(startDestination: String = "user_list") {
    val navController = rememberNavController()

    NavHost(navController, startDestination = startDestination) {
        composable("user_list") { UserListScreen() }
        composable("event_list") { EventListScreen() }
        // Другие экраны
    }
}