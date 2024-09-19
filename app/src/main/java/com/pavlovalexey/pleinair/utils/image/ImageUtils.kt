package com.pavlovalexey.pleinair.utils.image


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.random.Random

object ImageUtils {

    private const val MAX_WIDTH = 200
    private const val MAX_HEIGHT = 200
    private const val CIRCLE_DIAMETER = 200

    fun compressAndGetCircularBitmap(bitmap: Bitmap): Bitmap {
        val compressedBitmap = compressBitmap(bitmap)
        return getCircularBitmap(compressedBitmap)
    }

    fun compressBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = MAX_WIDTH
            newHeight = (MAX_WIDTH / aspectRatio).toInt()
        } else {
            newHeight = MAX_HEIGHT
            newWidth = (MAX_HEIGHT * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = CIRCLE_DIAMETER
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)
        val roundPx = size / 2f

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.parseColor("#FFFFFFFF") // Цвет фона
        canvas.drawCircle(roundPx, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, null, rect, paint)

        return output
    }

    fun generateRandomAvatar(): Bitmap {
        val width = 200
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Генерация градиента
        val paint = Paint()
        val colorStart = (0xFF000000 or Random.nextInt(0xFFFFFF).toLong()).toInt()
        val colorEnd = (0xFF000000 or Random.nextInt(0xFFFFFF).toLong()).toInt()
        val shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), colorStart, colorEnd, Shader.TileMode.CLAMP)
        paint.shader = shader

        val canvas = Canvas(bitmap)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Размеры для квадратов
        val squareSize = 50
        val centerX = width / 2
        val centerY = height / 2

        // Координаты углов квадратиков
        val squares = listOf(
            Pair(centerX - squareSize, centerY - squareSize), // Левый верхний
            Pair(centerX, centerY - squareSize),              // Правый верхний
            Pair(centerX - squareSize, centerY),              // Левый нижний
            Pair(centerX, centerY)                            // Правый нижний
        )

        // Генерация случайных цветов для квадратиков
        val colors = List(4) { (0xFF000000 or Random.nextInt(0xFFFFFF).toLong()).toInt() }

        // Рисуем квадратики
        for (i in squares.indices) {
            val (startX, startY) = squares[i]
            val color = colors[i]
            for (x in startX until startX + squareSize) {
                for (y in startY until startY + squareSize) {
                    if (x < width && y < height) { // Проверка на выход за границы изображения
                        bitmap.setPixel(x, y, color)
                    }
                }
            }
        }

        return bitmap
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun getRoundedCornerBitmap(drawable: Drawable, cornerRadius: Float): Bitmap {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint()
        val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = android.graphics.RectF(rect)
        val roundPx = cornerRadius

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    fun decodeSampledBitmapFromUri(imagePath: String): ImageBitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)

        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(imagePath, options)
        return bitmap.asImageBitmap()
    }


}