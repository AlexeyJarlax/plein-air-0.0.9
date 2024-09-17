package com.pavlovalexey.pleinair.main.ui.termsScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.main.ui.components.CustomButtonOne
import com.pavlovalexey.pleinair.main.ui.components.CustomCheckbox

@Composable
fun TermsScreen(
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    viewModel: TermsViewModel = hiltViewModel()
) {
    var isAgreementChecked by rememberSaveable { mutableStateOf(false) }
    var isPrivacyPolicyChecked by rememberSaveable { mutableStateOf(false) }

    val isButtonEnabled = isAgreementChecked && isPrivacyPolicyChecked && viewModel.isTermsLoaded
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.back_lay),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.4f)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.termsOfPrivacy,
                    fontSize = 21.sp,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = viewModel.privacyPolicyContent,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = viewModel.termsOfAgreement,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontSize = 21.sp,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = viewModel.userAgreementContent,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    CustomCheckbox(
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
                    CustomCheckbox(
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                CustomButtonOne(
                    onClick = onContinue,
                    text = stringResource(R.string.resume),
                    iconResId = R.drawable.circle_down_30dp // Замените на нужный ресурс
                )
                CustomButtonOne(
                    onClick = onCancel,
                    text = stringResource(R.string.cancel),
                    iconResId = R.drawable.door_open_30dp // Замените на нужный ресурс
                )
            }
        }
    }
}
