package com.pavlovalexey.pleinair.main.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

    var isLoading by remember { mutableStateOf(true) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val shouldShowLoading = isLoading && currentRoute !in listOf("terms", "auth")

    LaunchedEffect(Unit) {
        delay(1000)
        isLoading = false
    }

    fun shouldHideBottomBar(navController: NavHostController): Boolean {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        return currentRoute == "auth" || currentRoute == "terms"
    }
    Scaffold(
        bottomBar = {
            if (!isLoading && !shouldHideBottomBar(navController)) { // прячу нижнюю бару в момент загрузки
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {

            AnimatedVisibility( // Анимация появления экранов
                visible = !isLoading,
                enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 1500))
            ) {
                NavGraph(
                    navController = navController,
                    activity = LocalContext.current as Activity,
                    modifier = Modifier.padding(innerPadding)
                )
            }


            if (shouldShowLoading) { // кроме "terms" и "auth"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000)),
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