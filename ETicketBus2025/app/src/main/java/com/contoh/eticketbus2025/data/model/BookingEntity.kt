package com.contoh.eticketbus2025.data.model

import java.io.Serializable

data class BookingEntity(
    var id: String = "", // String ID untuk Firestore
    val bookingCode: String = "",
    val userId: String = "",
    val busName: String = "",
    val route: String = "",
    val date: String = "",
    val seats: String = "",
    val totalPrice: Double = 0.0,
    val status: String = "Pending", // Pending, Paid, Cancelled
    val paymentMethod: String = "",
    val timestamp: Long = 0
) : Serializable