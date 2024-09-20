package com.pavlovalexey.pleinair.event.data

import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.event.model.Event
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firebase: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun addEvent(event: Event): String {
        return try {
            val documentRef = firebase.collection("events")
                .add(event)
                .await()
            documentRef.id
        } catch (e: Exception) {
            throw e
        }
    }
    suspend fun createEvent(event: Event): String {
        return addEvent(event)
    }

    suspend fun updateEventImageUrl(eventId: String, imageUrl: String) {
        val eventRef = firebase.collection("events").document(eventId)
        eventRef.update("profileImageUrl", imageUrl).await()
    }
}