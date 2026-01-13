package com.contoh.eticketbus2025.data.model

import java.io.Serializable

data class BusModel(
    var id: String = "",
    val operatorName: String = "",
    val busClass: String = "",
    val price: Double = 0.0,
    val departTime: String = "",
    val arriveTime: String = "",
    val duration: String = "",
    val seatAvailable: Int = 0,
    val rating: Double = 0.0,
    val facilities: List<String> = listOf(),
    val origin: String = "",
    val destination: String = ""
) : Serializable