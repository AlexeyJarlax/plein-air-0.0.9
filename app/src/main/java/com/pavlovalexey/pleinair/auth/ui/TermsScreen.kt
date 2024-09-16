package com.pavlovalexey.pleinair.auth.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlovalexey.pleinair.R

@Composable
fun TermsScreen(
    onContinue: () -> Unit,
    viewModel: TermsViewModel = hiltViewModel()
) {
    var isAgreementChecked by rememberSaveable { mutableStateOf(false) }
    var isPrivacyPolicyChecked by rememberSaveable { mutableStateOf(false) }
    val isButtonEnabled = isAgreementChecked && isPrivacyPolicyChecked && viewModel.isTermsLoaded
    val scrollState = rememberScrollState()

    LaunchedEffect(viewModel.areTermsAccepted) {
        if (viewModel.areTermsAccepted) {
            onContinue()
        }
    }

    if (viewModel.isTermsLoaded) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = viewModel.termsOfPrivacy,
                    fontSize = 18.sp,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = viewModel.privacyPolicyContent,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = viewModel.termsOfAgreement,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = viewModel.userAgreementContent,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Checkbox(
                        checked = isPrivacyPolicyChecked,
                        onCheckedChange = { isPrivacyPolicyChecked = it },
                        enabled = viewModel.isTermsLoaded
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.i_have_read_privacy_policy),
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        fontSize = 16.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Checkbox(
                        checked = isAgreementChecked,
                        onCheckedChange = { isAgreementChecked = it },
                        enabled = viewModel.isTermsLoaded
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.i_have_read_user_policy),
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        fontSize = 16.sp
                    )
                }
            }

            Button(
                onClick = {
                    if (isButtonEnabled) {
                        viewModel.acceptTerms()
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = stringResource(R.string.resume))
            }
        }
    } else {
        // Можно показать индикатор загрузки или просто ничего не показывать
        Text(text = stringResource(R.string.loading))
    }
}