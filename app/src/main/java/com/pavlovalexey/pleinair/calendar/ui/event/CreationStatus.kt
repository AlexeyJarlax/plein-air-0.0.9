package com.pavlovalexey.pleinair.calendar.ui.event

sealed class CreationStatus {
    object Loading : CreationStatus()
    object Success : CreationStatus()
    data class Error(val message: String) : CreationStatus()
}