package com.contoh.eticketbus2025.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.ui.ticket.MyTicketActivity
import com.contoh.eticketbus2025.ui.profile.ProfileActivity
import com.contoh.eticketbus2025.data.model.PromoModel
import com.contoh.eticketbus2025.R
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Activity untuk menampilkan daftar promo yang tersedia.
 * Activity ini juga mengelola navigasi bawah (BottomNavigationView).
 */
class PromoActivity : AppCompatActivity() {

    /**
     * Fungsi yang dipanggil saat Activity pertama kali dibuat.
     * Di sini kita mengatur layout, mengunci mode terang, dan memanggil fungsi setup.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kunci Light Mode agar UI tetap konsisten
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_promo)

        // Memanggil fungsi untuk menyiapkan daftar promo dan navigasi bawah
        setupList()
        setupBottomNav()
    }

    /**
     * Menyiapkan RecyclerView untuk menampilkan daftar promo.
     * Fungsi ini membuat data promo tiruan (mock data) dan menampilkannya menggunakan PromoAdapter.
     */
    private fun setupList() {
        val rvPromos = findViewById<RecyclerView>(R.id.rvPromos)
        rvPromos.layoutManager = LinearLayoutManager(this)

        // Mock Data
        val promos = listOf(
            PromoModel(
                1,
                "Diskon Pengguna Baru",
                "Khusus pembelian pertama",
                "NEWUSER30",
                "30%",
                "31 Des 2025",
                "Rp 100.000",
                R.drawable.ic_notifications
            ),
            PromoModel(
                2,
                "Weekend Hemat",
                "Diskon perjalanan akhir pekan",
                "WEEKEND20",
                "20%",
                "30 Nov 2025",
                "Rp 150.000",
                R.drawable.ic_notifications
            ),
            PromoModel(
                3,
                "Cashback Kilat",
                "Cashback saldo e-wallet",
                "CASHBACK50",
                "Rp 50rb",
                "15 Des 2025",
                "Rp 200.000",
                R.drawable.ic_notifications
            )
        )

        // Mengatur adapter untuk RecyclerView dengan data promo yang telah dibuat
        rvPromos.adapter = PromoAdapter(promos)
    }

    /**
     * Menyiapkan BottomNavigationView untuk navigasi antar activity utama.
     * Fungsi ini mengatur item yang aktif dan menangani klik pada item navigasi.
     */
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_promo // Set aktif di Promo

        // Menangani aksi ketika item di navigasi bawah dipilih
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Jika item 'Home' dipilih
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                // Jika item 'My Ticket' dipilih
                R.id.nav_myticket -> {
                    val intent = Intent(this, MyTicketActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                // Jika item 'Promo' dipilih, tidak melakukan apa-apa karena sudah di halaman ini
                R.id.nav_promo -> true

                // Jika item 'Account' dipilih
                R.id.nav_account -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                // Jika item lain dipilih, kembalikan false
                else -> false
            }
        }
    }
}