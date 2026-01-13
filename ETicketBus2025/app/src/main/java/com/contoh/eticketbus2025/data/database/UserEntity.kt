package com.contoh.eticketbus2025.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String // Di aplikasi nyata, password harus di-hash/enkripsi
) : Serializable