package com.example.barterskill.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val skills: List<String> = listOf(),
    val profileImageUrl: String = "",
    val rating: Float = 0f,
    val base64ProfileImage: String? = null
)
