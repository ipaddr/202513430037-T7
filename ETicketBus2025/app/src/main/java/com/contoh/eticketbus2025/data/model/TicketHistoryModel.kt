package com.contoh.eticketbus2025.data.model

import java.io.Serializable

data class TicketHistoryModel(
    var bookingId: String = "",
    val userId: String = "",
    val operatorName: String = "",
    val busClass: String = "",
    val origin: String = "",
    val destination: String = "",
    val date: String = "",
    val time: String = "",
    val price: Double = 0.0,
    val status: String = "",
    val seats: String = "",
    val timestamp: Long = 0,

    // --- PERBAIKAN DI SINI ---
    // Ubah "isRoundTrip" jadi "roundTrip" agar cocok dengan Database
    val roundTrip: Boolean = false,

    val returnOperatorName: String? = null,
    val returnBusClass: String? = null,
    val returnOrigin: String? = null,
    val returnDestination: String? = null,
    val returnDate: String? = null,
    val returnTime: String? = null,
    val returnSeats: String? = null
) : Serializable