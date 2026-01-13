package com.contoh.eticketbus2025.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.source.FirestoreHelper // <-- Menggunakan Firestore
import com.contoh.eticketbus2025.ui.profile.ProfileActivity
import com.contoh.eticketbus2025.ui.ticket.MyTicketActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PromoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_promo)

        setupList()
        setupBottomNav()
    }

    private fun setupList() {
        val rvPromos = findViewById<RecyclerView>(R.id.rvPromos)
        rvPromos.layoutManager = LinearLayoutManager(this)

        // --- AMBIL DATA DARI FIRESTORE (CLOUD) ---
        CoroutineScope(Dispatchers.IO).launch {
            // Panggil fungsi getAllPromos dari FirestoreHelper
            val promosFromCloud = FirestoreHelper.getAllPromos()

            withContext(Dispatchers.Main) {
                if (promosFromCloud.isEmpty()) {
                    // Jika data kosong (belum di-seed atau gagal load)
                    Toast.makeText(
                        this@PromoActivity,
                        "Belum ada promo tersedia saat ini",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Pasang Adapter dengan data dari Cloud
                rvPromos.adapter = PromoAdapter(promosFromCloud)
            }
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_promo

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.nav_myticket -> {
                    val intent = Intent(this, MyTicketActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.nav_promo -> true // Tetap di sini

                R.id.nav_account -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}