package com.pavlovalexey.pleinair.event.model

data class Event(
    var id: String = "",
    val userId: String = "",
    val profileImageUrl: String = "",
    val city: String = "",
    val date: String = "",
    val time: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)