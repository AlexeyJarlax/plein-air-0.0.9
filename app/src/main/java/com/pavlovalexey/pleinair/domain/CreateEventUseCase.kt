package com.pavlovalexey.pleinair.domain

class CreateEventUseCase(private val eventRepository: EventRepository) {
    suspend fun execute(event: Event) {
        eventRepository.createEvent(event)
    }
}