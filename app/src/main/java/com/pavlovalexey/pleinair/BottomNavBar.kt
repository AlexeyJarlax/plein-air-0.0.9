package com.pavlovalexey.pleinair

import android.app.Activity
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pavlovalexey.pleinair.utils.uiComponents.CustomYesOrNoDialog

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val primaryDayColor = colorResource(id = R.color.my_prime_day)
    val primeBackground = colorResource(id = R.color.my_normal_blue)

//    val backgroundColor = primeBackground.copy(alpha = 0.0f)

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
//        backgroundColor = backgroundColor
    ) {
        BottomAppBar(
            backgroundColor =primeBackground.copy(alpha = 1f),
            modifier = Modifier.height(80.dp)
        ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Profile",
                    tint = primaryDayColor
                )
            },
            label = {
                Text(
                    stringResource(R.string.profile),
                    color = primaryDayColor
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
                    tint = primaryDayColor
                )
            },
            label = {
                Text(
                    stringResource(R.string.event),
                    color = primaryDayColor
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
                    Icons.Filled.Map,
                    contentDescription = "Карта",
                    tint = primaryDayColor
                )
            },
            label = {
                Text(
                    stringResource(R.string.map),
                    color = primaryDayColor
                )
            },
            selected = currentRoute == "map",
            onClick = {
                navController.navigate("map") {
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
                    tint = primaryDayColor
                )
            },
            label = {
                Text(
                    stringResource(R.string.settings),
                    color = primaryDayColor
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
                    Icons.Filled.DoorFront,
                    contentDescription = "Выход",
                    tint = primaryDayColor
                )
            },
            label = {
                Text(
                    stringResource(R.string.exit),
                    color = primaryDayColor
                )
            },
            selected = false,
            onClick = {
                showExitDialog = true
            }
        )
    }}
}

@Composable
fun ExitConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    CustomYesOrNoDialog(
        stringResource(id = R.string.exit_dialog),
        "",
        onDismiss,
        onConfirm
    )
}
