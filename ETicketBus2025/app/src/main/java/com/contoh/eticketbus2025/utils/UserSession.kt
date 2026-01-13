package com.contoh.eticketbus2025.utils

import android.content.Context
import android.content.SharedPreferences

class UserSession(context: Context) {

    private val pref: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
    }

    fun createLoginSession(userId: Int, name: String) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, name)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(): Int {
        return pref.getInt(KEY_USER_ID, -1)
    }

    fun getUserName(): String? {
        return pref.getString(KEY_USER_NAME, "User")
    }

    fun logoutUser() {
        editor.clear()
        editor.commit()
    }
}