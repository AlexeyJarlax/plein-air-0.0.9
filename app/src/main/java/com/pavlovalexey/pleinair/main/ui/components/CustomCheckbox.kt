package com.pavlovalexey.pleinair.main.ui.components

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.res.colorResource
import com.pavlovalexey.pleinair.R

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    val checkboxColors = CheckboxDefaults.colors(
        checkedColor = colorResource(id = R.color.my_blue_light), // Цвет для отмеченного состояния
        uncheckedColor = Color.Gray, // Цвет для неотмеченного состояния
        checkmarkColor = Color.White, // Цвет галочки
        disabledCheckedColor = Color.LightGray, // Цвет для отмеченного состояния при отключенной чекбоксе
        disabledUncheckedColor = Color.DarkGray // Цвет для неотмеченного состояния при отключенной чекбоксе
    )

    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = checkboxColors,
        enabled = enabled
    )
}

@Preview
@Composable
fun PreviewCustomCheckbox() {
    var checked by remember { mutableStateOf(false) }

    CustomCheckbox(
        checked = checked,
        onCheckedChange = { checked = it },
        enabled = true
    )
}