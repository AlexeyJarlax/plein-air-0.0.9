package com.pavlovalexey.pleinair.main.ui

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigation {
//        BottomNavigationItem(
//            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
//            label = { Text("Home") },
//            selected = currentRoute == "home",
//            onClick = {
//                navController.navigate("home") {
//                    popUpTo("home") { saveState = true }
//                    launchSingleTop = true
//                }
//            }
//        )
//        BottomNavigationItem(
//            icon = { Icon(Icons.Filled.Map, contentDescription = "Map") },
//            label = { Text("Map") },
//            selected = currentRoute == "map",
//            onClick = {
//                navController.navigate("map") {
//                    popUpTo("map") { saveState = true }
//                    launchSingleTop = true
//                }
//            }
//        )
//        BottomNavigationItem(
//            icon = { Icon(Icons.Filled.PersonPin, contentDescription = "UserMap") },
//            label = { Text("UserMap") },
//            selected = currentRoute == "userMap",
//            onClick = {
//                navController.navigate("userMap") {
//                    popUpTo("userMap") { saveState = true }
//                    launchSingleTop = true
//                }
//            }
//        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo("profile") { saveState = true }
                    launchSingleTop = true
                }
            }
        )
    }
}