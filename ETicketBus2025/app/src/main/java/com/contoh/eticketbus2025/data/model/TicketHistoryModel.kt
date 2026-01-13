package com.contoh.eticketbus2025.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tickets")
data class TicketHistoryModel(
    @PrimaryKey
    val bookingId: String,

    // DATA PERGI (Wajib)
    val operatorName: String,
    val busClass: String,
    val origin: String,
    val destination: String,
    val date: String,
    val time: String,
    val price: Double,
    val seats: String,

    // DATA UMUM
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRoundTrip: Boolean = false, // Penanda PP

    // DATA PULANG (Opsional / Nullable)
    val returnOperatorName: String? = null,
    val returnBusClass: String? = null,
    val returnOrigin: String? = null, // Biasanya kebalikan dari origin
    val returnDestination: String? = null,
    val returnDate: String? = null,
    val returnTime: String? = null,
    val returnSeats: String? = null
) : Serializable