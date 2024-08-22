package com.pavlovalexey.pleinair.data

import com.pavlovalexey.pleinair.ChatMessage

interface ChatRepository {
    suspend fun getMessagesForEvent(eventId: String): List<ChatMessage>
    suspend fun sendMessage(message: ChatMessage)
}