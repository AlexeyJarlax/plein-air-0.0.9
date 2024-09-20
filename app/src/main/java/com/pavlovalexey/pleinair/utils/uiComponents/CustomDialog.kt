package com.pavlovalexey.pleinair.utils.uiComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.pavlovalexey.pleinair.R

@Composable
fun CustomDialog(
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {

    val dialogTextStyle = TextStyle(
        fontSize = 16.sp,
        color = colorResource(id = R.color.my_very_blue)
    )


    @Composable
    fun dialogTextColor() = colorResource(id = R.color.my_prime_day)

    val dialogModifier = Modifier
        .background(colorResource(id = R.color.my_very_blue))
        .padding(16.dp)

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = "Выберите фильтр",
                style = dialogTextStyle.copy(color = dialogTextColor())
            )
        },
        text = {
            Column(modifier = dialogModifier) {
                options.forEach { option ->
                    TextButton(
                        onClick = { onOptionSelected(option) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = option,
                            style = dialogTextStyle.copy(color = dialogTextColor())
                        )
                    }
                }
            }
        },
        buttons = {}
    )
}

//CustomDialog(  для вызова
//options = options,
//onOptionSelected = onOptionSelected
//)