package com.pavlovalexey.pleinair.utils


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImageUtils {

    private const val MAX_WIDTH = 200
    private const val MAX_HEIGHT = 200
    private const val CIRCLE_DIAMETER = 200
    /**
     * Сжимает изображение до максимальных размеров (MAX_WIDTH и MAX_HEIGHT), сохраняя соотношение сторон.
     *
     * @param bitmap Исходное изображение.
     * @return Сжатое изображение.
     */

    // Примените круглый аватар перед загрузкой на сервер
    fun compressAndGetCircularBitmap(bitmap: Bitmap): Bitmap {
        // Сжимаем изображение
        val compressedBitmap = compressBitmap(bitmap)
        // Преобразуем сжатое изображение в круглое
        return getCircularBitmap(compressedBitmap)
    }

    fun compressBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Вычисляем масштаб для сохранения соотношения сторон
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

        // Создаем сжатое изображение
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

    /**
     * Декодирует изображение из URI с заданными размерами.
     *
     * @param imagePath Путь к изображению.
     * @return Bitmap изображение.
     */
    fun decodeSampledBitmapFromUri(imagePath: String): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)

        // Compute inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(imagePath, options)
    }

    /**
     * Вычисляет значение inSampleSize для сжатия изображения.
     *
     * @param options BitmapFactory.Options.
     * @param reqWidth Желаемая ширина.
     * @param reqHeight Желаемая высота.
     * @return inSampleSize.
     */
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

    /**
     * Преобразует Drawable в Bitmap с округлыми краями.
     *
     * @param drawable Drawable изображение.
     * @return Bitmap изображение.
     */
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
}