package com.pavlovalexey.pleinair.profile.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.type.LatLng
import com.pavlovalexey.pleinair.R

@Composable
fun ProfileScreen(
    onNavigateToUserMap: () -> Unit,
    viewModel: ProfileViewModel,
    onContinue: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val selectedArtStyles by viewModel.selectedArtStyles.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Image
        ProfileImage(
            bitmap = user?.profileImageBitmap,
            onClick = { viewModel.pickImageFromGallery() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display Name with clickable option to edit
        Text(
            text = user?.name ?: "User",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.clickable {
                viewModel.showEditNameDialog()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Edit Description Button
        Button(
            onClick = { viewModel.showEditDescriptionDialog() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Edit Description")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Logout")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Continue Button
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Continue")
        }
    }
}

@Composable
fun ProfileImage(bitmap: Bitmap?, onClick: () -> Unit) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.account_circle_50dp),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        )
    }
}