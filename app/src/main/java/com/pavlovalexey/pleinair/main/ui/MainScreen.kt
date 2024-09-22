package com.pavlovalexey.pleinair.main.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import com.pavlovalexey.pleinair.main.ui.authScreen.AuthScreen
import kotlinx.coroutines.delay

@Composable
fun MainScreen(navController: NavHostController) {
    // Новое состояние загрузки
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Задержка для имитации загрузки
        delay(2000)
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Анимация появления экранов
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 1500))
            ) {
                // Отображение основного контента
                NavGraph(
                    navController = navController,
                    activity = LocalContext.current as Activity,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Показ прогресс-бара во время загрузки
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000)),  // полупрозрачный черный фон
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF008F9C),
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }
    }
}