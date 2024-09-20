package com.pavlovalexey.pleinair.map.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.util.concurrent.CountDownLatch

class ImageRepository {

    fun glideLoadSync(context: Context, url: String): Bitmap {
        val latch = CountDownLatch(1)
        var bitmap: Bitmap? = null

        Glide.with(context)
            .asBitmap()
            .load(url)
            .apply(RequestOptions().override(100, 100)) // Укажите размеры изображения
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bitmap = resource
                    latch.countDown()
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    latch.countDown()
                }
            })

        latch.await()
        return bitmap ?: throw RuntimeException("Failed to load image")
    }
}