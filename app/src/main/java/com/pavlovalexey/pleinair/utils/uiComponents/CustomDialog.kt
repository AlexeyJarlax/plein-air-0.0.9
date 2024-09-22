package com.pavlovalexey.pleinair.utils.uiComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pavlovalexey.pleinair.R
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.google.accompanist.flowlayout.SizeMode


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
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.my_very_blue)
                )
            ) {
                Text("✔️", style = TextStyle(fontSize = 18.sp))
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.my_very_blue)
                )
            ) {
                Text("❌", style = TextStyle(fontSize = 18.sp))
            }
        },
        backgroundColor = colorResource(id = R.color.my_very_blue),
        contentColor = colorResource(id = R.color.my_black)
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
        contentColor = colorResource(id = R.color.my_black)
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

@Composable
fun CustomTextInputDialog(
    title: String,
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit // Принимает новое значение текста
) {
    var text by remember { mutableStateOf(initialText) } // Для отслеживания введенного текста

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = title, style = TextStyle(fontSize = 18.sp)) },
        text = {
            Column {
                Text("Введите значение:", style = TextStyle(fontSize = 18.sp))
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 18.sp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text)
                } ,
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.my_very_blue))
            ) {
                Text("✔️", style = TextStyle(fontSize = 18.sp))
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.my_very_blue))
            ) {
                Text("❌", style = TextStyle(fontSize = 18.sp))
            }
        },
        backgroundColor = colorResource(id = R.color.my_very_blue),
        contentColor = colorResource(id = R.color.my_black)
    )
}
/**
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
 */

@Composable
fun CustomCheckboxDialog(
    title: String,
    options: Array<String>,
    selectedItems: MutableMap<String, Boolean>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = title,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            FlowRow(
                mainAxisSize = SizeMode.Expand,
                mainAxisSpacing = 16.dp,
                crossAxisSpacing = 8.dp
            ) {
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                    ) {
                        CustomCheckbox(
                            checked = selectedItems[option] ?: false,
                            onCheckedChange = { isChecked ->
                                selectedItems[option] = isChecked
                            },
                            enabled = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = TextStyle(fontSize = 16.sp),
                            modifier = Modifier.wrapContentWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selected = selectedItems.filter { it.value }.keys
                    onConfirm(selected.toSet())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.my_very_blue))
            ) {
                Text("✔️", style = TextStyle(fontSize = 18.sp))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.my_very_blue))
            ) {
                Text("❌", style = TextStyle(fontSize = 18.sp))
            }
        },
        backgroundColor = colorResource(id = R.color.my_very_blue),
        contentColor = colorResource(id = R.color.my_black)
    )
}