package com.pavlovalexey.pleinair.model

data class User(
    val id: String,
    val name: String,
    val bio: String,
    val profilePictureUrl: String,
    val interests: List<String>
)