package com.contoh.eticketbus2025.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.contoh.eticketbus2025.R

/**
 * Activity HelpActivity menampilkan halaman bantuan kepada pengguna.
 * Di halaman ini, pengguna dapat menemukan tombol untuk kembali ke halaman sebelumnya
 * dan tombol untuk menghubungi dukungan pelanggan melalui email.
 */
class HelpActivity : AppCompatActivity() {
    /**
     * Metode onCreate dipanggil saat activity pertama kali dibuat.
     * Metode ini menginisialisasi layout dan menyiapkan listener untuk elemen UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menetapkan layout untuk activity ini dari file res/layout/activity_help.xml
        setContentView(R.layout.activity_help)

        // Menemukan tombol kembali (btnBack) dan menambahkan listener klik.
        // Saat diklik, activity ini akan ditutup dan pengguna kembali ke layar sebelumnya.
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Menemukan tombol hubungi dukungan (btnContactSupport) dan menambahkan listener klik.
        findViewById<Button>(R.id.btnContactSupport).setOnClickListener {
            // Membuat Intent implisit untuk mengirim email (ACTION_SENDTO).
            val intent = Intent(Intent.ACTION_SENDTO)
            // Menetapkan data intent ke URI email, yang akan membuka aplikasi email.
            intent.data = Uri.parse("mailto:support@eticketbus.com") // Alamat email tujuan
            // Menambahkan subjek default untuk email.
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bantuan Aplikasi E-Ticket")
            // Memeriksa apakah ada aplikasi di perangkat yang dapat menangani intent ini.
            if (intent.resolveActivity(packageManager) != null) {
                // Jika ada, mulai activity untuk mengirim email.
                startActivity(intent)
            }
        }
    }
}