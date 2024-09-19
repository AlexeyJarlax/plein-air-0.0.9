package com.pavlovalexey.pleinair.utils.uiComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha

@Composable
fun BackgroundImage(
    imageResId: Int,
    alpha: Float = 0.8f
) {
    Image(
        painter = painterResource(imageResId),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    )
}
