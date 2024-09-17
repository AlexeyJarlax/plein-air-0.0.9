import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.profile.viewmodel.ProfileViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToUserMap: () -> Unit,
    onContinue: () -> Unit,
    onLogout: () -> Unit,
    onExit: () -> Unit
) {
    val user by viewModel.user.observeAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newDescription by remember { mutableStateOf("") }

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
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Image
            ProfileImage(
                imageUrl = user?.profileImageUrl,
                onClick = { viewModel.checkAndGenerateAvatar {} }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = user?.name ?: "User",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.clickable {
                    user?.name?.let { currentName ->
                        viewModel.updateUserName(newName = currentName) {
                            // Handle success
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDialog = true },
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

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
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
                            showDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileImage(onClick: () -> Unit, imageUrl: String?) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .placeholder(R.drawable.account_circle_50dp)  // Use resource ID directly
            .error(R.drawable.account_circle_50dp)         // Use resource ID directly
            .build(),
        contentDescription = null,
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    )
}
