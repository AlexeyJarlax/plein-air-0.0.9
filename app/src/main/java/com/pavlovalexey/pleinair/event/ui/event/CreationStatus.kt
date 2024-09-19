package com.pavlovalexey.pleinair.event.ui.event

sealed class CreationStatus {
    data object Loading : CreationStatus()
    data class Success(val eventId: String) : CreationStatus()
    data class Error(val message: String) : CreationStatus()
}