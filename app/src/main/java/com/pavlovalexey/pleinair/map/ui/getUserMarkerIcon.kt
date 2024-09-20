package com.pavlovalexey.pleinair.map.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.pavlovalexey.pleinair.map.data.ImageRepository

@Composable
fun getUserMarkerIcon(profileImageUrl: String): BitmapDescriptor? {
    val context = LocalContext.current
    val imageRepository = ImageRepository()

    val imageUrl = if (profileImageUrl.isNotEmpty()) profileImageUrl else "url_to_placeholder_image"
    return try {
        // Получение Bitmap из URL с использованием репозитория
        val bitmap: Bitmap = imageRepository.glideLoadSync(context, imageUrl)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}