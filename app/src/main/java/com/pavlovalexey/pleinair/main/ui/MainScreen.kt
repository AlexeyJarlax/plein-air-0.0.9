package com.pavlovalexey.pleinair.main.ui

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pavlovalexey.pleinair.NavGraph

@Composable
fun MainScreen(navController: NavHostController) {
    // Отслеживаем текущий маршрут
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Определяем, когда показывать нижнюю панель
    val showBottomNav = currentRoute == "profile" || currentRoute == "settings"

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            activity = LocalContext.current as Activity,
            modifier = Modifier.padding(innerPadding)
        )
    }
}