package com.pavlovalexey.pleinair.calendar.data

import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.calendar.model.Event
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firebase: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences
) {

    private suspend fun addEvent(event: Event): String {
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

    // Исправление: добавлен тип параметра eventId
    suspend fun createEvent(eventId: String): String {
        val userId = sharedPreferences.getString("userId", "") ?: ""
        val profileImageUrl = sharedPreferences.getString("profileEventImageUrl", "") ?: ""
        val city = sharedPreferences.getString("eventCity", "") ?: ""
        val date = sharedPreferences.getString("eventDate", "") ?: ""
        val time = sharedPreferences.getString("eventTime", "") ?: ""
        val description = sharedPreferences.getString("eventDescription", "") ?: ""
        val latitude = sharedPreferences.getFloat("eventLatitude", 0f).toDouble()
        val longitude = sharedPreferences.getFloat("eventLongitude", 0f).toDouble()

        val event = Event(
            id = eventId,
            userId = userId,
            profileImageUrl = profileImageUrl,
            city = city,
            date = date,
            time = time,
            description = description,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis()
        )

        return try {
            val newEventId = addEvent(event)
            newEventId
        } catch (e: Exception) {
            throw e
        }
    }
}