package com.contoh.eticketbus2025

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inisialisasi Firebase secara eksplisit
        FirebaseApp.initializeApp(this)
    }
}