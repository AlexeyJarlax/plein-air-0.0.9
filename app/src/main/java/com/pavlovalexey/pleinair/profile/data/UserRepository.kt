package com.pavlovalexey.pleinair.profile.data

import android.util.Log
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.profile.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getUserById(userId: String): User? {
        return try {
            val documentSnapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val user = documentSnapshot.toObject(User::class.java)
            Log.d(TAG, "=== Пользователь успешно получен: $user")
            user
        } catch (e: Exception) {
            Log.e(TAG, "=== Ошибка при получении пользователя", e)
            null
        }
    }
}