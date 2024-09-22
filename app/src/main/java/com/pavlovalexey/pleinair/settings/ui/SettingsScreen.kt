package com.pavlovalexey.pleinair.settings.ui


import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.utils.uiComponents.CustomButtonOne
import androidx.compose.ui.platform.LocalContext
import com.pavlovalexey.pleinair.utils.uiComponents.BackgroundImage
import com.pavlovalexey.pleinair.utils.uiComponents.CustomYesOrNoDialog

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
) {
    val isNightMode by viewModel.isNightMode.observeAsState(initial = false)
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val context = LocalContext.current as Activity
    val eventFlow = viewModel.eventFlow.collectAsState(initial = null)
    var showDonatDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    eventFlow.value?.let { event ->
        when (event) {
            is SettingsViewModel.Event.AccountDeleted -> {
                onNavigateToAuth()
                context.finish()
            }
            is SettingsViewModel.Event.DeleteAccountFailed ->
                Toast.makeText(context, "Ошибка удаления аккаунта", Toast.LENGTH_LONG).show()
            is SettingsViewModel.Event.ReauthenticationFailed ->
                Toast.makeText(context, "Ошибка реаутентификации", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        BackgroundImage(imageResId = R.drawable.back_lay)
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

            Spacer(modifier = Modifier.height(10.dp))
            CustomButtonOne(
                onClick = { viewModel.shareApp() },
                text = stringResource(R.string.share_app_title),
                iconResId = R.drawable.share_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            CustomButtonOne(
                onClick = { viewModel.goToHelp() },
                text = stringResource(R.string.write_to_support),
                iconResId = R.drawable.support_agent_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            CustomButtonOne(
                onClick = { viewModel.seeUserAgreement() },
                text = stringResource(R.string.ua),
                iconResId = R.drawable.description_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            CustomButtonOne(
                onClick = { viewModel.seePrivacyPolicy() },
                text = stringResource(R.string.pp),
                iconResId = R.drawable.description_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            CustomButtonOne(
                onClick = { showDonatDialog = true },
                text = stringResource(R.string.donats),
                iconResId = R.drawable.currency_ruble_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            CustomButtonOne(
                onClick = { showDeleteAccountDialog = true },
                text = stringResource(R.string.delit_me),
                iconResId = R.drawable.person_remove_30dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showDonatDialog) {
            CustomYesOrNoDialog(
                title = stringResource(R.string.confirmation),
                text = stringResource(R.string.confirmation_for_pay_terminal),
                onDismiss = { showDonatDialog = false },
                onConfirm = {
                    viewModel.seeDonat()
                    showDonatDialog = false
                }
            )
        }

        if (showDeleteAccountDialog) {
            CustomYesOrNoDialog(
                title = stringResource(R.string.confirmation),
                text = stringResource(R.string.confirmation_for_delete),
                onDismiss = { showDeleteAccountDialog = false },
                onConfirm = {
                    viewModel.deleteUserAccount(onNavigateToAuth)
                    showDeleteAccountDialog = false
                }
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
