package com.pavlovalexey.pleinair.utils.uiComponents

import android.content.Intent
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import com.pavlovalexey.pleinair.R

@Composable
fun CustomYesOrNoDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = title, style = TextStyle(fontSize = 18.sp)) },
        text = { Text(text = text, style = TextStyle(fontSize = 18.sp)) },
        confirmButton = {
            Button(
                onClick = { onConfirm() }
            ) {
                Text("✔️", style = TextStyle(fontSize = 18.sp))
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("❌", style = TextStyle(fontSize = 18.sp))
            }
        },
        backgroundColor = colorResource(id = R.color.my_very_blue),
        contentColor = colorResource(id = R.color.my_normal_blue)
    )
}
/** пример реализации:
CustomYesOrNoDialog(
stringResource(id = R.string.exit_dialog),
"",
onDismiss,
onConfirm
)
*/

@Composable
fun CustomOptionDialog(
    title: String,
    options: List<String>, // Список опций для выбора
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit // Коллбэк для обработки выбранной опции
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = title, style = TextStyle(fontSize = 18.sp)) },
        text = {
            Column {
                options.forEach { option ->
                    Text(
                        text = option,
                        style = TextStyle(fontSize = 18.sp), // Установка размера шрифта
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                onOptionSelected(option) // Выбор опции
                                onDismiss() // Закрытие диалога
                            }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Закрыть", style = TextStyle(fontSize = 18.sp)) // Установка размера шрифта
            }
        },
        dismissButton = null, // Можно оставить кнопку "Закрыть" или убрать
        backgroundColor = colorResource(id = R.color.my_very_blue),
        contentColor = colorResource(id = R.color.my_normal_blue)
    )
}

/** пример реализации:
val options = listOf("Сделать фото", "Выбрать из галереи")
CustomOptionDialog(
title = "Выберите аватарку",
options = options,
onDismiss = onDismissRequest,
onOptionSelected = { selectedOption ->
    when (selectedOption) {
        "Сделать фото" -> {

            }
        }
        "Выбрать из галереи" -> {

            )

        }
    }
    onDismissRequest()
}
)
 */
