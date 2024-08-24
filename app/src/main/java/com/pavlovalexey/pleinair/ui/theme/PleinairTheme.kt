package com.pavlovalexey.pleinair.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Определите цвета для вашей темы
private val LightColorPalette = lightColors(
    primary = Color.Blue,
    primaryVariant = Color.DarkGray,
    secondary = Color.Green
    // Добавьте другие цвета по необходимости
)

@Composable
fun PleinairTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}