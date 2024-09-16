package com.pavlovalexey.pleinair.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.common.SignInButton
import com.pavlovalexey.pleinair.R

@Composable
fun AuthScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Фон как изображение
        Image(
            painter = painterResource(id = R.drawable.back_lay), // ваш фон
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Кнопка авторизации через Google
            AndroidView(
                factory = { context ->
                    SignInButton(context).apply {
                        // Дополнительная настройка кнопки, если нужно
                        setSize(SignInButton.SIZE_WIDE)
                    }
                },
                modifier = Modifier.wrapContentSize()
            ) {
                // Нажатие на кнопку Google Sign-In
                authViewModel.signInWithGoogle()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка выхода
            Button(
                onClick = {
                    navController.navigate("terms") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            ) {
                Text(text = "Exit")
            }

            // Навигация на основе состояния авторизации
            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated) {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            }
        }
    }
}