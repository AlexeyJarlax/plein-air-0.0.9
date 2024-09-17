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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pavlovalexey.pleinair.NavGraph
import com.pavlovalexey.pleinair.settings.ui.SettingsScreen

@Composable
fun MainScreen(navController: NavHostController) {
    // Отслеживаем текущий маршрут
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Определяем, когда показывать нижнюю панель
    val showBottomNav = remember { mutableStateOf(false) }

    // Отображаем навигационную панель только на экранах profile и settings
    showBottomNav.value = currentRoute == "profile" || currentRoute == "settings"

    Scaffold(
        bottomBar = {
            if (showBottomNav.value) {
                BottomNavBar(navController = navController)
            }
        }
    ) {
    }
}