package com.pavlovalexey.pleinair.model

data class Event(
    val id: String,
    val name: String,
    val description: String,
    val date: Long,
    val location: String,
    val participants: List<User>
)