package com.contoh.eticketbus2025.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.contoh.eticketbus2025.R

// PaymentMethodActivity adalah kelas yang mengatur tampilan dan fungsionalitas halaman "Metode Pembayaran".
class PaymentMethodActivity : AppCompatActivity() {
    // Fungsi onCreate dipanggil saat aktivitas pertama kali dibuat.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menetapkan layout XML 'activity_payment_method' sebagai tampilan untuk aktivitas ini.
        setContentView(R.layout.activity_payment_method)

        // Menemukan tombol kembali (btnBack) berdasarkan ID-nya.
        // Saat tombol ini diklik, panggil fungsi finish() untuk menutup aktivitas saat ini dan kembali ke layar sebelumnya.
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Menemukan tombol "Tambah Kartu" (btnAddCard) berdasarkan ID-nya.
        // Saat tombol ini diklik, sebuah pesan Toast akan ditampilkan.
        findViewById<Button>(R.id.btnAddCard).setOnClickListener {
            // Menampilkan pesan singkat (Toast) yang memberi tahu pengguna bahwa fitur ini belum tersedia.
            Toast.makeText(this, "Fitur Tambah Kartu Segera Hadir!", Toast.LENGTH_SHORT).show()
        }
    }
}