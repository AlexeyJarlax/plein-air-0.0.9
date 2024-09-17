package com.pavlovalexey.pleinair.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.settings.ui.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val isNightMode by viewModel.isNightMode.observeAsState(initial = false)
    val isLoading by viewModel.isLoading.observeAsState(initial = false)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(R.drawable.back_lay),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Темный режим", modifier = Modifier.weight(1f))
                Switch(
                    checked = isNightMode,
                    onCheckedChange = { viewModel.changeNightMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.shareApp() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(text = "Поделиться приложением")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.goToHelp() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(text = "Написать в поддержку")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.seeUserAgreement() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(text = "Пользовательское соглашение")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.seePrivacyPolicy() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(text = "Политика конфиденциальности")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Логика для отображения диалога подтверждения
                    viewModel.seeDonat()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(text = "Поддержать проект")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Логика удаления аккаунта
                    viewModel.deleteUserAccount()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "Удалить аккаунт")
            }
        }
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Blue)
            }
        }
    }
}
