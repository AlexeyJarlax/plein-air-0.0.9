package com.pavlovalexey.pleinair.profile.data

import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.profile.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getUserById(userId: String): User? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null // Можно также обработать ошибку или вернуть дефолтного пользователя
        }
    }
}