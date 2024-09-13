package com.pavlovalexey.pleinair.calendar.model

data class Event(
    var id: String = "",
    val userId: String = "",
    val profileImageUrl: String = "",
    val city: String = "",
    val place: String = "",
    val date: String = "",
    val time: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)