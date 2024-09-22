package com.pavlovalexey.pleinair.event.ui.newEvent

sealed class CreationStatus {
    object Idle : CreationStatus() //тут ничего не происходит
    object Loading : CreationStatus()
    data class Success(val eventId: String) : CreationStatus()
    data class Error(val message: String) : CreationStatus()
}
