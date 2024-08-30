package com.pavlovalexey.pleinair.profile.model

data class User(
    val displayName: String? = null, // Имя пользователя
    val photoUrl: String? = null,    // URL аватара пользователя
    val locationName: String? = null // Название текущего местоположения
)