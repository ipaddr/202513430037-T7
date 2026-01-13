package com.contoh.eticketbus2025.utils

import android.content.Context
import android.content.SharedPreferences

class UserSession(context: Context) {

    private val pref: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    companion object {
        private const val IS_LOGIN = "isLoggedIn"
        private const val KEY_USER_ID = "userId" // Sekarang String
        private const val KEY_FULL_NAME = "fullName"
    }

    // Update parameter id menjadi String
    fun createLoginSession(id: String, name: String) {
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_USER_ID, id) // Simpan sebagai String
        editor.putString(KEY_FULL_NAME, name)
        editor.commit()
    }

    fun isLoggedIn(): Boolean {
        return pref.getBoolean(IS_LOGIN, false)
    }

    // Return String (bukan Int lagi)
    fun getUserId(): String {
        return pref.getString(KEY_USER_ID, "") ?: ""
    }

    fun getUserName(): String {
        return pref.getString(KEY_FULL_NAME, "") ?: "User"
    }

    fun logoutUser() {
        editor.clear()
        editor.commit()
    }
}