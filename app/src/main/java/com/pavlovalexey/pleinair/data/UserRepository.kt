package com.pavlovalexey.pleinair.data

import com.pavlovalexey.pleinair.model.User

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: String): User?
}