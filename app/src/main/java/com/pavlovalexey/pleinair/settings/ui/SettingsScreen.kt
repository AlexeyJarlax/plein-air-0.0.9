package com.pavlovalexey.pleinair.settings.ui


import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.main.ui.components.CustomButtonOne
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val isNightMode by viewModel.isNightMode.observeAsState(initial = false)
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val context = LocalContext.current as Activity
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SettingsViewModel.Event.FinishActivity -> {
                    context.finish()
                }
                is SettingsViewModel.Event.AccountDeleted -> {
                    // Успешное удаление аккаунта
                    context.finish()
                }
                is SettingsViewModel.Event.DeleteAccountFailed -> {
                    // Показать ошибку удаления
                    Toast.makeText(context, "Ошибка удаления аккаунта", Toast.LENGTH_LONG).show()
                }
                is SettingsViewModel.Event.ReauthenticationFailed -> {
                    // Показать ошибку реаутентификации
                    Toast.makeText(context, "Ошибка реаутентификации", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
            CustomButtonOne(
                onClick = { viewModel.shareApp() },
                text = stringResource(R.string.share_app_text),
                iconResId = R.drawable.share_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButtonOne(
                onClick = { viewModel.goToHelp() },
                text = stringResource(R.string.write_to_support),
                iconResId = R.drawable.ic_btn_support,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButtonOne(
                onClick = { viewModel.seeUserAgreement() },
                text = stringResource(R.string.ua),
                iconResId = R.drawable.description_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButtonOne(
                onClick = { viewModel.seePrivacyPolicy() },
                text = stringResource(R.string.pp),
                iconResId = R.drawable.description_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButtonOne(
                onClick = {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Подтверждение")
                    builder.setMessage("Пользователь перейдет на платежный терминал. Продолжить?")
                    builder.setPositiveButton("Перейти") { dialog, _ ->
                        viewModel.seeDonat()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                },
                text = stringResource(R.string.donats),
                iconResId = R.drawable.currency_ruble_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButtonOne(
                onClick = {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Подтверждение удаления")
                    builder.setMessage("Будут удалены все данные пользователя и аккаунт в этом приложении. Вы уверены, что хотите продолжить?")
                    builder.setPositiveButton("Удалить") { dialog, _ ->
                        viewModel.deleteUserAccount()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                },
                text = stringResource(R.string.delit_me),
                iconResId = R.drawable.person_remove_30dp,
                modifier = Modifier.fillMaxWidth()
            )
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