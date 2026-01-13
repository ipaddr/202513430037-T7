package com.contoh.eticketbus2025.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.NotificationEntity

// import com.contoh.eticketbus2025.data.database.AppDatabase // <-- HAPUS INI

/**
 * Activity Notifikasi.
 * Saat ini diset kosong (Empty State) karena belum ada koleksi notifikasi di Firestore.
 */
class NotificationActivity : AppCompatActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var emptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        rvNotifications = findViewById(R.id.rvNotifications)
        emptyState = findViewById(R.id.emptyState)
        rvNotifications.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        loadNotifications()
    }

    private fun loadNotifications() {
        // TODO: Nanti sambungkan ke FirestoreHelper jika sudah ada koleksi 'notifications'
        // Saat ini kita buat list kosong agar tidak crash
        val list = listOf<NotificationEntity>()

        // Contoh Dummy Data (Jika ingin testing tampilan):
        /*
        val list = listOf(
            NotificationEntity("1", "Selamat Datang", "Selamat datang di aplikasi E-Ticket Bus!", "INFO", "Baru saja"),
            NotificationEntity("2", "Promo Baru", "Gunakan kode NEWUSER30", "PROMO", "1 Jam yang lalu")
        )
        */

        if (list.isEmpty()) {
            rvNotifications.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvNotifications.visibility = View.VISIBLE
            emptyState.visibility = View.GONE

            rvNotifications.adapter = NotificationAdapter(list) { notif ->
                // Aksi saat notif diklik (sementara kosong)
            }
        }
    }
}