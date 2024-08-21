package com.pavlovalexey.pleinair.data

import com.pavlovalexey.pleinair.model.Event

interface EventRepository {
    suspend fun getEvents(): List<Event>
    suspend fun createEvent(event: Event)
}