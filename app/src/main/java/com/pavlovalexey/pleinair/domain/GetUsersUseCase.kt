package com.pavlovalexey.pleinair.domain

import com.pavlovalexey.pleinair.data.UserRepository
import com.pavlovalexey.pleinair.model.User

class GetUsersUseCase(private val userRepository: UserRepository) {
    suspend fun execute(): List<User> {
        return userRepository.getUsers()
    }
}