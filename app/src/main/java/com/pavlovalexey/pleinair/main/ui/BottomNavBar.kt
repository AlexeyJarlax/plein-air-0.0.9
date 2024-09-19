package com.pavlovalexey.pleinair.main.ui

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pavlovalexey.pleinair.R

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val primaryDayColor = colorResource(id = R.color.my_prime_day)
    val primeBackground = colorResource(id = R.color.my_prime_background)

    // Установите фоновый цвет с желаемой прозрачностью
    val backgroundColor = primeBackground.copy(alpha = 0.1f)

    BottomNavigation(
        backgroundColor = backgroundColor // Установка фонового цвета с прозрачностью
    ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Profile",
                    tint = primaryDayColor // Цвет иконки
                )
            },
            label = {
                Text(
                    "Profile",
                    color = primaryDayColor // Цвет текста
                )
            },
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    Icons.Filled.Event,
                    contentDescription = "Events",
                    tint = primaryDayColor // Цвет иконки
                )
            },
            label = {
                Text(
                    "Events",
                    color = primaryDayColor // Цвет текста
                )
            },
            selected = currentRoute == "events",
            onClick = {
                navController.navigate("events") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = primaryDayColor // Цвет иконки
                )
            },
            label = {
                Text(
                    "Settings",
                    color = primaryDayColor // Цвет текста
                )
            },
            selected = currentRoute == "settings",
            onClick = {
                navController.navigate("settings") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}
