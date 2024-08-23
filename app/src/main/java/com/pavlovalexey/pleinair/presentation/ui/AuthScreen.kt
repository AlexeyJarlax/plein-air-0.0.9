package com.pavlovalexey.pleinair.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pavlovalexey.pleinair.presentation.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel = hiltViewModel()) {
    var isLoginScreen by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val email = viewModel.email
        val password = viewModel.password

        TextField(
            value = email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isLoginScreen) {
                    viewModel.signIn(email, password)
                } else {
                    viewModel.register(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoginScreen) "Login" else "Register")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { isLoginScreen = !isLoginScreen }
        ) {
            Text(if (isLoginScreen) "Don't have an account? Register" else "Already have an account? Login")
        }

        if (viewModel.isLoggedIn) {
            Text("You are logged in!", color = Color.Green)
        }
    }
}