package com.pavlovalexey.pleinair.presentation

import androidx.compose.runtime.Composable
import com.pavlovalexey.pleinair.model.User

@Composable
fun UserListScreen(viewModel: UserViewModel = hiltViewModel()) {
    val users by viewModel.users.collectAsState()

    LazyColumn {
        items(users) { user ->
            UserListItem(user)
        }
    }
}

@Composable
fun UserListItem(user: User) {
    Row(modifier = Modifier.padding(8.dp)) {
        Image(
            painter = rememberImagePainter(user.profilePictureUrl),
            contentDescription = "Profile Picture",
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = user.name, fontWeight = FontWeight.Bold)
            Text(text = user.bio)
        }
    }
}