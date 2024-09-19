package com.pavlovalexey.pleinair.main.ui

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pavlovalexey.pleinair.R

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val primaryDayColor = colorResource(id = R.color.my_prime_day)
    val primeBackground = colorResource(id = R.color.my_secondary_background)

    val backgroundColor = primeBackground.copy(alpha = 1f)

    var showExitDialog by remember { mutableStateOf(false) }

    val activity = LocalContext.current as Activity

    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                showExitDialog = false
                activity.finish()
            }
        )
    }

    BottomNavigation(
        backgroundColor = backgroundColor
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
                    "Профиль",
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
                    contentDescription = "События",
                    tint = primaryDayColor // Цвет иконки
                )
            },
            label = {
                Text(
                    "События",
                    color = primaryDayColor // Цвет текста
                )
            },
            selected = currentRoute == "event_list",
            onClick = {
                navController.navigate("event_list") {
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
                    contentDescription = "Настройки",
                    tint = primaryDayColor // Цвет иконки
                )
            },
            label = {
                Text(
                    "Настройки",
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
        BottomNavigationItem(
            icon = {
                Icon(
                    Icons.Filled.ExitToApp,
                    contentDescription = "Выход",
                    tint = primaryDayColor // Цвет иконки
                )
            },
            label = {
                Text(
                    "Выход",
                    color = primaryDayColor // Цвет текста
                )
            },
            selected = false,
            onClick = {
                showExitDialog = true
            }
        )
    }
}

@Composable
fun ExitConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Подтверждение выхода") },
        text = { Text(text = "Вы уверены, что хотите выйти?") },
        confirmButton = {
            Button(
                onClick = { onConfirm() }
            ) {
                Text("✔️")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("❌")
            }
        },
        backgroundColor = Color.White,
        contentColor = Color.Black
    )
}
