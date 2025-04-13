package com.example.barterskill.models

import java.util.Date

data class Post(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val offerSkill: String = "",
    val wantSkill: String = "",
    val timestamp: Date = Date(),
    val active: Boolean = true
)