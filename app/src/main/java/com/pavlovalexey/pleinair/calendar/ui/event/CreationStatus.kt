package com.pavlovalexey.pleinair.calendar.ui.event

sealed class CreationStatus {
    object Loading : CreationStatus()
    data class Success(val eventId: String) : CreationStatus()
    data class Error(val message: String) : CreationStatus()
}