package com.pavlovalexey.pleinair

import com.pavlovalexey.pleinair.model.User

data class ChatMessage(
    val id: String,
    val sender: User,
    val message: String,
    val timestamp: Long
)