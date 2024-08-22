package com.pavlovalexey.pleinair.domain

import com.pavlovalexey.pleinair.ChatMessage

class SendMessageUseCase(private val chatRepository: ChatRepository) {
    suspend fun execute(message: ChatMessage) {
        chatRepository.sendMessage(message)
    }
}