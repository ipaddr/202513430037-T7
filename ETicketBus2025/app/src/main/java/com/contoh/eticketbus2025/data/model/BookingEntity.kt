package com.contoh.eticketbus2025.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey
    val bookingId: String, // Contoh: ETB-12345
    val busName: String,
    val busClass: String,
    val origin: String,
    val destination: String,
    val date: String,
    val time: String,
    val seats: String, // Disimpan sebagai string koma: "1A, 1B"
    val totalPrice: Double,
    val status: String = "Aktif", // Aktif / Selesai
    val timestamp: Long = System.currentTimeMillis()
) : Serializable