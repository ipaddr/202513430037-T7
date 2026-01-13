package com.contoh.eticketbus2025.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String,
    val type: String, // "INFO", "PROMO", "TRANSACTION"
    val date: String,
    val isRead: Boolean = false
) : Serializable