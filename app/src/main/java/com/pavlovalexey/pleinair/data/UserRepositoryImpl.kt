package com.pavlovalexey.pleinair.data

import com.pavlovalexey.pleinair.model.User

class UserRepositoryImpl(
    private val userDao: UserDao, // Интерфейс для работы с базой данных
    private val apiService: ApiService // Интерфейс для работы с сетью
) : UserRepository {
    override suspend fun getUsers(): List<User> {
        // Логика работы с сетью или базой данных
        return apiService.getUsers()
    }

    override suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)
    }
}