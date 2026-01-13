package com.contoh.eticketbus2025.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity untuk menampilkan daftar notifikasi kepada pengguna.
 * Activity ini mengambil data notifikasi dari database lokal (Room) dan menampilkannya dalam RecyclerView.
 * Pengguna dapat menandai notifikasi sebagai "telah dibaca".
 */
class NotificationActivity : AppCompatActivity() {

    // RecyclerView untuk menampilkan daftar notifikasi.
    private lateinit var rvNotifications: RecyclerView
    // Layout yang ditampilkan ketika tidak ada notifikasi.
    private lateinit var emptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // Inisialisasi view dari layout.
        rvNotifications = findViewById(R.id.rvNotifications)
        emptyState = findViewById(R.id.emptyState)
        // Mengatur layout manager untuk RecyclerView.
        rvNotifications.layoutManager = LinearLayoutManager(this)

        // Mengatur listener untuk tombol kembali (back).
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Memuat notifikasi dari database saat activity dibuat.
        loadNotifications()
    }

    /**
     * Fungsi untuk memuat notifikasi dari database dan menampilkannya di UI.
     * Operasi database dijalankan di background thread menggunakan Coroutines.
     */
    private fun loadNotifications() {
        // Membuat CoroutineScope untuk menjalankan tugas di background (IO dispatcher).
        CoroutineScope(Dispatchers.IO).launch {
            // Mendapatkan instance database.
            val db = AppDatabase.getDatabase(applicationContext)
            // Mengambil semua notifikasi dari DAO (Data Access Object).
            val list = db.appDao().getAllNotifications()

            // Beralih ke Main thread untuk memperbarui UI.
            withContext(Dispatchers.Main) {
                // Cek jika daftar notifikasi kosong.
                if (list.isEmpty()) {
                    // Jika kosong, sembunyikan RecyclerView dan tampilkan layout 'emptyState'.
                    rvNotifications.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                } else {
                    // Jika ada notifikasi, tampilkan RecyclerView dan sembunyikan 'emptyState'.
                    rvNotifications.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE

                    // Mengatur adapter untuk RecyclerView dengan daftar notifikasi yang didapat.
                    // Juga menyertakan lambda function yang akan dipanggil saat item notifikasi diklik.
                    rvNotifications.adapter = NotificationAdapter(list) { notif ->
                        // Saat item diklik, panggil fungsi markAsRead dengan ID notifikasi.
                        markAsRead(notif.id)
                    }
                }
            }
        }
    }
    /**
     * Fungsi untuk menandai notifikasi sebagai telah dibaca di database.
     * @param id ID dari notifikasi yang akan ditandai.
     */
    private fun markAsRead(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            // Memanggil fungsi DAO untuk update status notifikasi.
            db.appDao().markNotificationAsRead(id)
            loadNotifications() // Memuat ulang daftar notifikasi untuk memperbarui UI.
        }
    }
}