package com.pavlovalexey.pleinair.main.ui.authScreen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.common.SignInButton
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.main.ui.components.CustomButtonOne

@Composable
fun AuthScreen(
    navController: NavHostController,
    onAuthSuccess: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current as Activity
    val authState by viewModel.authState.collectAsState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleSignInResult(result.data)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.back_lay),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AndroidView(
                factory = { context ->
                    SignInButton(context).apply {
                        setSize(SignInButton.SIZE_WIDE)
                        setOnClickListener {
                            viewModel.signInWithGoogle(googleSignInLauncher)
                        }
                    }
                },
                modifier = Modifier.wrapContentSize()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.wrapContentSize() // Заполняет весь доступный экран
            ) {
                CustomButtonOne(
                    onClick = onCancel,
                    text = stringResource(R.string.cancel),
                    iconResId = R.drawable.door_open_30dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd) // Расположить в нижнем правом углу
                        .padding(end = 16.dp, bottom = 70.dp) // Отступы от краёв
                )
            }

            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated) {
                    onAuthSuccess()
                }
            }
        }
    }
}