package com.pavlovalexey.pleinair

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Определение цветов для светлой и темной темы
private val DarkColorPalette = darkColors(
    primary = Color.Blue,
    primaryVariant = Color.Red,
    secondary = Color.Magenta
)

private val LightColorPalette = lightColors(
    primary = Color.Blue,
    primaryVariant = Color.Red,
    secondary = Color.Magenta
)

// Основная функция темы
@Composable
fun PleinairTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Определение типографики и форм (для примера, можно настроить по своему)
private val Typography = androidx.compose.material.Typography()
private val Shapes = androidx.compose.material.Shapes()