package com.contoh.eticketbus2025.data.model

import java.io.Serializable

data class UserEntity(
    var id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = ""
) : Serializable