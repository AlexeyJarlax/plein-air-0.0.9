package com.pavlovalexey.pleinair.utils.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import kotlin.random.Random

class RandomAvatar {

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
}