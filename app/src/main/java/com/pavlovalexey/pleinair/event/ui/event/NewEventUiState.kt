package com.pavlovalexey.pleinair.event.ui.event

data class NewEventUiState(
    val city: String = "",
    val date: String = "",
    val time: String = "",
    val description: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)