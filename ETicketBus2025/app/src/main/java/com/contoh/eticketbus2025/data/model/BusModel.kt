package com.contoh.eticketbus2025.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "buses")
data class BusModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val operatorName: String,
    val busClass: String,
    val price: Double,
    val departTime: String,
    val arriveTime: String,
    val duration: String,
    val seatAvailable: Int,
    val rating: Double,
    val facilities: List<String>, // Butuh TypeConverter

    // Tambahan kolom untuk pencarian
    val origin: String = "Padang",
    val destination: String = "Jakarta"
) : Serializable

