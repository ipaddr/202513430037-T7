package com.contoh.eticketbus2025.data.model

import java.io.Serializable

data class PromoModel(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val code: String = "",
    val discount: String = "",
    val validUntil: String = "",
    val minPurchase: String = "",
    val iconRes: Int = 0
) : Serializable