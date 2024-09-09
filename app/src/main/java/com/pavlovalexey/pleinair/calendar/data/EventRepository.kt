package com.pavlovalexey.pleinair.calendar.data

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.calendar.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EventRepository(private val application: Application) {

    private val db = FirebaseFirestore.getInstance()

    suspend fun addEvent(event: Event) {
        try {
            db.collection("events")
                .add(event)
                .await()  // Используйте coroutines для ожидания завершения операции
        } catch (e: Exception) {
            throw e
        }
    }

    // Метод для обновления URL изображения события
    suspend fun updateEventImageUrl(eventId: String, imageUrl: String) {
        val eventRef = db.collection("events").document(eventId)
        eventRef.update("imageUrl", imageUrl)
            .await() // Используйте Kotlin Coroutines для ожидания завершения операции
    }
}