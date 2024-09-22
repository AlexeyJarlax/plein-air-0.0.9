package com.pavlovalexey.pleinair.profile.ui.profileList

import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.utils.uiComponents.BackgroundImage
import com.pavlovalexey.pleinair.utils.uiComponents.CustomOptionDialog
import com.pavlovalexey.pleinair.utils.uiComponents.CustomTextInputDialog
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.pavlovalexey.pleinair.utils.uiComponents.CustomCheckboxDialog

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToUserMap: () -> Unit,
    onMyLocation: () -> Unit,
    onLogout: () -> Unit,
    onExit: () -> Unit,

    ) {
    val user by viewModel.user.observeAsState()
    var showTechniquesDialog by remember { mutableStateOf(false) }
    val selectedArtStyles by viewModel.selectedArtStyles.observeAsState(emptySet())
    val bitmap by viewModel.bitmap.observeAsState()
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showImageSelectionDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newDescription by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(user) {
        if (viewModel.isUserSignedIn()) { // прогоняю неавторизованного
            Log.d("ProfileScreen", "=== Текущий пользователь: $user")
        } else {
            Log.d("ProfileScreen", "=== Текущий пользователь: $user")
            Toast.makeText(context, "Переход на авторизацию...", Toast.LENGTH_SHORT).show()
            viewModel.logout()
            onLogout()
        }
    }

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

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BackgroundImage(imageResId = R.drawable.back_lay)
        Column(
            modifier = Modifier
                .fillMaxSize()
            .padding(start = 30.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileImage(
                    imageUrl = user?.profileImageUrl,
                    bitmap = bitmap,
                    onClick = { showImageSelectionDialog = true },

                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = user?.name ?: "User",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEditNameDialog = true },
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
            Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 200.dp)

        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 16.dp,
                crossAxisSpacing = 16.dp,
                mainAxisAlignment = FlowMainAxisAlignment.Center
            ) {
                ProfilePanel(
                    text = stringResource(R.string.description),
                    iconResId = R.drawable.description_30dp,
                    onClick = { showDescriptionDialog = true }
                )
                ProfilePanel(
                    text = stringResource(R.string.location),
                    iconResId = R.drawable.location_on_50dp,
                    onClick = onMyLocation
                )
                ProfilePanel(
                    text = "Техники",
                    iconResId = R.drawable.palette_30dp,
                    onClick = { showTechniquesDialog = true }
                )
                ProfilePanel(
                    text = stringResource(R.string.logout),
                    iconResId = R.drawable.logout_30dp,
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    }
                )
            }
        }
    }



    if (showDescriptionDialog) {
        CustomTextInputDialog(
            title = stringResource(id = R.string.edit_description),
            initialText = if (user?.description.isNullOrEmpty()) {
                stringResource(id = R.string.description_sample)
            } else {
                user?.description ?: ""
            },
            onDismiss = { showDescriptionDialog = false },
            onConfirm = { newDescription ->
                viewModel.updateUserDescription(newDescription) {
                    showDescriptionDialog = false
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
    if (showTechniquesDialog) {
        TechniquesSelectionDialog(
            artStyles = stringArrayResource(id = R.array.art_styles),
            selectedArtStyles = selectedArtStyles,
            onDismissRequest = { showTechniquesDialog = false },
            onConfirm = { newSelections ->
                viewModel.updateSelectedArtStyles(newSelections) {
                    showTechniquesDialog = false
                }
            }
        )
    }
}


    @Composable
    private fun ProfileImage(onClick: () -> Unit, imageUrl: String?, bitmap: Bitmap?) {
        val imageModifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = imageModifier
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl ?: R.drawable.account_circle_50dp)
                    .crossfade(true)
                    .placeholder(R.drawable.account_circle_50dp)
                    .error(R.drawable.account_circle_50dp)
                    .build(),
                contentDescription = null,
                modifier = imageModifier
            )
        }
    }

@Composable
fun ProfilePanel(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = text,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun showImageSelectionDialog(
    cameraActivityResultLauncher: ActivityResultLauncher<Intent>,
    galleryActivityResultLauncher: ActivityResultLauncher<Intent>,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val options = listOf("Сделать фото", "Выбрать из галереи")

    CustomOptionDialog(
        title = "Выберите аватарку",
        options = options,
        onDismiss = onDismissRequest,
        onOptionSelected = { selectedOption ->
            when (selectedOption) {
                "Сделать фото" -> {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(context.packageManager) != null) {
                        cameraActivityResultLauncher.launch(takePictureIntent)
                    }
                }

                "Выбрать из галереи" -> {
                    val pickPhotoIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    galleryActivityResultLauncher.launch(pickPhotoIntent)
                }
            }
            onDismissRequest()
        }
    )
}

@Composable
private fun showEditNameDialog(
    viewModel: ProfileViewModel,
    currentName: String,
    onDismissRequest: () -> Unit
) {
    CustomTextInputDialog(
        title = stringResource(id = R.string.change_name),
        initialText = currentName,
        onDismiss = onDismissRequest,
        onConfirm = { newName ->
            viewModel.updateUserName(newName) {
                onDismissRequest()
            }
        }
    )
}

@Composable
fun TechniquesSelectionDialog(
    artStyles: Array<String>,
    selectedArtStyles: Set<String>,
    onDismissRequest: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val selectedItems = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        artStyles.forEach { style ->
            selectedItems[style] = selectedArtStyles.contains(style)
        }
    }

    CustomCheckboxDialog(
        title = "Выберите техники",
        options = artStyles,
        selectedItems = selectedItems,
        onDismiss = onDismissRequest,
        onConfirm = { selected ->
            onConfirm(selected)
        }
    )
}


