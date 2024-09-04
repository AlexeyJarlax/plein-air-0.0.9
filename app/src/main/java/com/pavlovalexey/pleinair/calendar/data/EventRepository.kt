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
        withContext(Dispatchers.IO) {
            db.collection("events")
                .add(event)
                .await() // Используем await для асинхронного выполнения
        }
    }
}