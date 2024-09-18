package com.pavlovalexey.pleinair

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Цвета для светлой и темной темы
private val DarkColorPalette = darkColors(
    primary = Color.Blue,
    primaryVariant = Color.Red,
    secondary = Color.Magenta,
    onPrimary = Color.White, // Цвет текста на темном фоне
    background = Color.Black.copy(alpha = 0.6f) // Черный фон с альфой для темной темы
)

private val LightColorPalette = lightColors(
    primary = Color.Blue,
    primaryVariant = Color.Red,
    secondary = Color.Magenta,
    background = Color.White.copy(alpha = 0.6f) // Белый фон с альфой для светлой темы
)

@Composable
fun PleinairTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(
            body1 = MaterialTheme.typography.body1.copy(
                color = if (darkTheme) Color(0xFF9FBBF3) else Color(0xFF1C1E27) // Цвет текста
            )
        ),
        shapes = Shapes,
        content = content
    )
}

// Определение типографики и форм
private val Typography = androidx.compose.material.Typography()
private val Shapes = androidx.compose.material.Shapes()