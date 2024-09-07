package com.pavlovalexey.pleinair.profile.model

data class User(
    val userId: String = "",
    val name: String = "",
    val description: String? = null,
    val artStyles: List<String>? = null,
    val profileImageUrl: String = "",
    val location: Map<String, Double> = emptyMap(),
    val locationName: String = "",
    val isOnline: Boolean? = null
)