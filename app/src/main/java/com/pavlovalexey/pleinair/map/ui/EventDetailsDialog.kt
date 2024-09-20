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
import com.pavlovalexey.pleinair.event.model.Event

@Composable
fun EventDetailsDialog(event: Event, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(event.city) },
        text = {
            Column {
                Text("Дата: ${event.date}")
                Text("Время: ${event.time}")
                Text("Описание: ${event.description}")
            }
        },
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = { /* Участвовать */ }) {
                    Text("Участвовать")
                }
                Button(onClick = { /* Добавить в друзья */ }) {
                    Text("Добавить в друзья")
                }
                Button(onClick = { onDismiss() }) {
                    Text("Закрыть")
                }
            }
        }
    )
}
