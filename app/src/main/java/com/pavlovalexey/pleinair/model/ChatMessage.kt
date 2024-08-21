package com.pavlovalexey.pleinair.model

data class ChatMessage(
    val id: String,
    val sender: User,
    val message: String,
    val timestamp: Long
)