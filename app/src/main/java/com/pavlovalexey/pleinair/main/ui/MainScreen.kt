package com.pavlovalexey.pleinair.main.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pavlovalexey.pleinair.BottomNavBar
import com.pavlovalexey.pleinair.NavGraph
import kotlinx.coroutines.delay

@Composable
fun MainScreen(navController: NavHostController) {
    // Отслеживаем текущий маршрут
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Определяем, когда показывать нижнюю панель
    val showBottomNav = currentRoute == "profile" || currentRoute == "settings" || currentRoute == "event_list" || currentRoute == "new_event"

    // Новое состояние загрузки
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Имитируем задержку для демонстрации прогресс-бара
        delay(2000)
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavGraph(
                navController = navController,
                activity = LocalContext.current as Activity,
                modifier = Modifier.padding(innerPadding)
            )

            // Отображение прогресс-бара во время загрузки
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000)),  // полупрозрачный черный фон
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF008F9C),  // ваш цвет
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }
    }
}
