package com.pavlovalexey.pleinair.auth.ui

/**
 * собрал на Jetpack Compose — фреймворк для создания UI на Android, основанный на декларативном подходе без xml
 */

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pavlovalexey.pleinair.R

@Composable
fun TermsScreen(onContinue: () -> Unit, viewModel: TermsViewModel) {
    var isAgreementChecked by rememberSaveable { mutableStateOf(false) }
    var isPrivacyPolicyChecked by rememberSaveable { mutableStateOf(false) }

    val isButtonEnabled = isAgreementChecked && isPrivacyPolicyChecked && viewModel.isTermsLoaded
    val scrollState = rememberScrollState()

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
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = viewModel.privacyPolicyContent,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = viewModel.termsOfAgreement,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = viewModel.userAgreementContent,
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
                    fontSize = 16.sp
                )
            }
        }

        Button(
            onClick = onContinue,
            enabled = isButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = stringResource(R.string.resume))
        }
    }
}