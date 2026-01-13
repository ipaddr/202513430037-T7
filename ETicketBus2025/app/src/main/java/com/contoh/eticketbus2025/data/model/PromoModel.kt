package com.contoh.eticketbus2025.data.model

import java.io.Serializable

data class PromoModel(
    val id: Int,
    val title: String,
    val description: String,
    val code: String,
    val discount: String,
    val validUntil: String,
    val minPurchase: String,
    val iconRes: Int // Resource ID untuk ikon (misal: R.drawable.ic_gift)
) : Serializable