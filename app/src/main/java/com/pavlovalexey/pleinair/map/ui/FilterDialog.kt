package com.pavlovalexey.pleinair.map.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun FilterDialog(onOptionSelected: (String) -> Unit) {
    val options = listOf(
        "Показать ивенты",
        "Показать пользователей онлайн",
        "Показать пользователей офлайн"
    )
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Выберите фильтр") },
        text = {
            Column {
                options.forEach { option ->
                    TextButton(onClick = { onOptionSelected(option) }) {
                        Text(option)
                    }
                }
            }
        },
        buttons = {}
    )
}
