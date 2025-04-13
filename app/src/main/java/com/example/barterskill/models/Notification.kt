package com.example.barterskill.models

import java.util.Date

data class Notification(
    val id: String = "",
    val type: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val postId: String = "",
    val details: Map<String, String> = mapOf(),
    val timestamp: Date = Date(),
    val read: Boolean = false
)