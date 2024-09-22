package com.pavlovalexey.pleinair.event.data

import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.event.model.Event
import kotlinx.coroutines.runBlocking
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

    fun getEventByUserId(userId: String): Event? {
        var event: Event? = null
        runBlocking {
            val querySnapshot = firebase.collection("events")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                event = document.toObject(Event::class.java)
            }
        }
        return event
    }

    suspend fun updateEventImageUrl(eventId: String, imageUrl: String) {
        val eventRef = firebase.collection("events").document(eventId)
        eventRef.update("profileImageUrl", imageUrl).await()
    }

    suspend fun deleteEvent(eventId: String) {
        try {
            firebase.collection("events").document(eventId).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }
}