package com.pavlovalexey.pleinair

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

@Composable
fun PleinairTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {

    // моя палитра:
    val MyBlack = Color(0xFF1C1E27)
    val MyBlueLight = Color(0xFF9FBBF3)
    val MySecondaryBackground = colorResource(id = R.color.my_normal_blue)

    // день и ночь:
    val LightColorPalette = lightColors(
        primary = MyBlack,
        primaryVariant = MySecondaryBackground,
        secondary = Color.Cyan,
        background = Color.White
    )

    val DarkColorPalette = darkColors( // пока не разобрался с концепцией стиля отключил
        primary = MyBlueLight,
        primaryVariant = MySecondaryBackground,
        secondary = MyBlack,
        onPrimary = Color.Gray,
        background = MyBlack,
    )

    val colors = LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography(
            body1 = MaterialTheme.typography.body1.copy(color = MyBlack),
            h6 = MaterialTheme.typography.h6.copy(color = MyBlack),
            body2 = MaterialTheme.typography.body2.copy(color = MyBlack)
        ),
        shapes = Shapes,
        content = content
    )
}

private val Typography = androidx.compose.material.Typography()
private val Shapes = androidx.compose.material.Shapes()