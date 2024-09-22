package com.pavlovalexey.pleinair.map.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pavlovalexey.pleinair.profile.model.User

@Composable
fun UserDetailsDialog(user: User, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(user.name) },
        text = {
            Column {
                Text("Техники: ${user.selectedArtStyles?.joinToString(", ") ?: "Не выбраны"}")
                Text("Описание: ${user.description ?: "Отсутствует"}")
            }
        },
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = { /* Добавить в друзья */ }) {
                    Text("Добавить в друзья")
                }
                Button(onClick = { /* Пригласить на пленэр */ }) {
                    Text("Пригласить на пленэр")
                }
                Button(onClick = { onDismiss() }) {
                    Text("Закрыть")
                }
            }
        }
    )
}
