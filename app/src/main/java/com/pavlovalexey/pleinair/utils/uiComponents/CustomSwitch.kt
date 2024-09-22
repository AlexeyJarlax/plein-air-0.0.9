package com.pavlovalexey.pleinair.utils.uiComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.Blue,
            checkedTrackColor = Color.Black,
            uncheckedThumbColor = Color.LightGray,
            uncheckedTrackColor = Color.DarkGray
        ),
        modifier = modifier
            .padding(end = 12.dp, bottom = 12.dp)
            .background(Color.Transparent)
    )
}
