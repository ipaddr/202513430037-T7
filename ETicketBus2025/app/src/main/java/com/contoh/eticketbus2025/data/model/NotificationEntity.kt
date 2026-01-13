package com.contoh.eticketbus2025.data.model

data class NotificationEntity(
    var id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // "INFO" or "PROMO"
    val date: String = "",
    val isRead: Boolean = false
)