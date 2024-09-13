package com.pavlovalexey.pleinair.calendar.data

import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.calendar.model.Event
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firebase: FirebaseFirestore
) {

    suspend fun addEvent(event: Event): String {
        return try {
            val documentRef = firebase.collection("events")
                .add(event)
                .await()
            documentRef.id // возвращаем ID нового события
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateEventImageUrl(eventId: String, imageUrl: String) {
        val eventRef = firebase.collection("events").document(eventId)
        eventRef.update("profileImageUrl", imageUrl).await()
    }
}