package com.pavlovalexey.pleinair.profile.ui.profileList

import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.utils.uiComponents.BackgroundImage
import com.pavlovalexey.pleinair.utils.uiComponents.CustomButtonOne

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToUserMap: () -> Unit,
    onMyLocation: () -> Unit,
    onLogout: () -> Unit,
    onExit: () -> Unit,

    ) {
    val user by viewModel.user.observeAsState()
    val selectedArtStyles by viewModel.selectedArtStyles.observeAsState(emptySet())
    val bitmap by viewModel.bitmap.observeAsState()

    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showImageSelectionDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newDescription by remember { mutableStateOf("") }

    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleCameraResult(result)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGalleryResult(result, context)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BackgroundImage(imageResId = R.drawable.back_lay)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ProfileImage(
                imageUrl = user?.profileImageUrl,
                onClick = { showImageSelectionDialog = true },
            )

            Spacer(modifier = Modifier.height(16.dp))


            Text( // User Name
                text = user?.name ?: "User",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.clickable {
                    showEditNameDialog = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButtonOne(
                onClick = { showDescriptionDialog = true },
                text = stringResource(R.string.description),
                iconResId = R.drawable.description_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButtonOne(  // Choose Location Button
                onClick = onMyLocation,
                text = stringResource(R.string.location),
                iconResId = R.drawable.location_on_50dp,
                modifier = Modifier.fillMaxWidth()
            )

            CustomButtonOne( // account Button
                onClick = {
                    viewModel.logout()
                    onLogout()
//                    navigateToLogin()
                },
                text = stringResource(R.string.logout),
                iconResId = R.drawable.logout_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButtonOne(
                onClick = onExit,
                text = stringResource(R.string.exit),
                iconResId = R.drawable.door_open_30dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showDescriptionDialog) {
            AlertDialog(
                onDismissRequest = { showDescriptionDialog = false },
                title = { Text("Edit Description") },
                text = {
                    TextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("New Description") }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateUserDescription(newDescription) {
                                showDescriptionDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDescriptionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showImageSelectionDialog) {
            showImageSelectionDialog(
                cameraActivityResultLauncher = cameraLauncher,
                galleryActivityResultLauncher = galleryLauncher,
                onDismissRequest = { showImageSelectionDialog = false }
            )
        }

        if (showEditNameDialog) {
            showEditNameDialog(
                viewModel = viewModel,
                currentName = user?.name ?: "User",
                onDismissRequest = { showEditNameDialog = false }
            )
        }
    }
}

@Composable
private fun ProfileImage(onClick: () -> Unit, imageUrl: String?) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl ?: R.drawable.account_circle_50dp)
            .crossfade(true)
            .placeholder(R.drawable.account_circle_50dp)
            .error(R.drawable.account_circle_50dp)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun showImageSelectionDialog(
    cameraActivityResultLauncher: ActivityResultLauncher<Intent>,
    galleryActivityResultLauncher: ActivityResultLauncher<Intent>,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val options = arrayOf("Сделать фото", "Выбрать из галереи")

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Выберите аватарку") },
        backgroundColor = MaterialTheme.colors.background,
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Text(
                        text = option,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                if (index == 0) {
                                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    if (takePictureIntent.resolveActivity(context.packageManager) != null) {
                                        cameraActivityResultLauncher.launch(takePictureIntent)
                                    }
                                } else {
                                    val pickPhotoIntent = Intent(
                                        Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                    )
                                    galleryActivityResultLauncher.launch(pickPhotoIntent)
                                }
                                onDismissRequest()
                            }
                    )
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("❌")
            }
        }
    )
}

@Composable
private fun showEditNameDialog(
    viewModel: ProfileViewModel,
    currentName: String,
    onDismissRequest: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Изменить имя") },
        text = {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Новое имя") }
            )
        },
        confirmButton = {
            Button(onClick = {
                viewModel.updateUserName(newName) {
                    onDismissRequest()
                }
            }) {
                Text("✔️")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("❌")
            }
        }
    )
}
