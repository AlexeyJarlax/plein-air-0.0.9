package com.pavlovalexey.pleinair.calendar.model

data class Event(
    val userId: String = "",
    val avatarUrl: String = "",
    val city: String = "",
    val place: String = "",
    val date: String = "",
    val time: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis() // используется для удаления просроченных событий
)