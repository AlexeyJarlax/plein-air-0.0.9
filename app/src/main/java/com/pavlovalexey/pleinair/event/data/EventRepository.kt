package com.pavlovalexey.pleinair.event.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.event.model.Event
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firebase: FirebaseFirestore,
) {

    fun getEventById(eventId: String): Event? {
        var event: Event? = null
        runBlocking {
            try {
                val documentSnapshot = firebase.collection("events")
                    .document(eventId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    event = documentSnapshot.toObject(Event::class.java)
                    Log.d("EventRepository", "=== Event fetched: $event")
                } else {
                    Log.e("EventRepository", "=== No event found with id $eventId")
                }
            } catch (e: Exception) {
                Log.e(
                    "EventRepository",
                    "=== Failed to fetch event with id $eventId: ${e.localizedMessage}"
                )
            }
        }
        return event
    }

    fun getEventByUserId(userId: String): Event? {
        var event: Event? = null
        runBlocking {
            try {
                val querySnapshot = firebase.collection("events")
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .await()

                if (querySnapshot.documents.isNotEmpty()) {
                    event = querySnapshot.documents[0].toObject(Event::class.java)
                    Log.d("EventRepository", "=== Event fetched: $event")
                } else {
                    Log.e("EventRepository", "=== No event found for user with id $userId")
                }
            } catch (e: Exception) {
                Log.e(
                    "EventRepository",
                    "=== Failed to fetch event for user with id $userId: ${e.localizedMessage}"
                )
            }
        }
        return event
    }

    suspend fun createEvent(event: Event): String {
        return try {
            val documentRef = firebase.collection("events")
                .add(event)
                .await()
            documentRef.id
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateEventImageUrl(eventId: String, imageUrl: String) {
        val eventRef = firebase.collection("events").document(eventId)
        eventRef.update("profileImageUrl", imageUrl).await()
    }

    suspend fun deleteEvent(userId: String, eventId: String) {
        try {
            firebase.collection("events").document(eventId).delete().await()
        } catch (e: Exception) {
            Log.e("EventRepository", "=== не удалилось eventId: $eventId: ${e.localizedMessage}")
            throw e
        }
        try {
            firebase.collection("events").document(userId).delete().await()
        } catch (e: Exception) {
            Log.e("EventRepository", "=== не удалилось userId: $userId: ${e.localizedMessage}")
            throw e
        }
    }
}


