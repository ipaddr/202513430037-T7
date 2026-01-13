package com.contoh.eticketbus2025.ui.ticket

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.database.AppDatabase
import com.contoh.eticketbus2025.data.model.BusModel
import com.contoh.eticketbus2025.data.model.TicketHistoryModel
import com.contoh.eticketbus2025.ui.home.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activity yang ditampilkan setelah proses pemesanan tiket berhasil.
 * Bertanggung jawab untuk:
 * 1. Menampilkan animasi sukses.
 * 2. Menyimpan data tiket yang berhasil dipesan ke database lokal (Room).
 * 3. Memberikan opsi navigasi untuk melihat detail tiket atau kembali ke halaman utama.
 */
class TicketSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_success)

        // --- 1. INISIALISASI DAN ANIMASI UI ---
        // Mengambil referensi view dari layout
        val imgSuccess = findViewById<ImageView>(R.id.imgSuccess)
        val glowBackground = findViewById<View>(R.id.viewGlow) ?: imgSuccess.parent as View

        // Atur skala awal gambar dan background menjadi 0 agar tidak terlihat
        imgSuccess.scaleX = 0f
        imgSuccess.scaleY = 0f
        glowBackground.scaleX = 0f
        glowBackground.scaleY = 0f

        // Animasi untuk memunculkan gambar (checklis) dari kecil ke besar
        imgSuccess.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500) // Durasi animasi 0.5 detik
            .setInterpolator(AccelerateDecelerateInterpolator()) // Efek percepatan dan perlambatan
            .start()

        // Animasi untuk memunculkan background glow dari kecil ke besar
        glowBackground.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500) // Durasi animasi 0.5 detik
            .setInterpolator(AccelerateDecelerateInterpolator()) // Efek percepatan dan perlambatan
            .withEndAction { // Aksi yang dijalankan setelah animasi ini selesai
                // Memulai animasi berulang (pulse dan glow) untuk efek visual
                startPulseAnimation(imgSuccess)
                startGlowAnimation(glowBackground)
            }
            .start()

        // --- 2. PENYIMPANAN DATA ---
        // Memanggil fungsi untuk menyimpan detail tiket ke database lokal
        saveTicketToDatabase()

        // --- 3. NAVIGASI ---
        // Memberi aksi klik pada tombol "Lihat Tiket"
        findViewById<Button>(R.id.btnViewTicket).setOnClickListener {
            // Membuat intent untuk pindah ke halaman detail tiket
            val intentNext = Intent(this, ETicketDetailActivity::class.java)
            // Membawa semua data ekstra (seperti detail bus, tanggal, dll) dari intent sebelumnya
            if (intent.extras != null) {
                intentNext.putExtras(intent.extras!!)
            }
            startActivity(intentNext)
            finish()
        }

        // Memberi aksi klik pada tombol "Kembali ke Beranda"
        findViewById<TextView>(R.id.btnBackHome).setOnClickListener {
            // Membuat intent untuk kembali ke MainActivity (halaman utama)
            val intentHome = Intent(this, MainActivity::class.java)
            // Membersihkan tumpukan activity sebelumnya, agar tombol back tidak kembali ke halaman ini
            intentHome.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intentHome)
        }
    }

    /**
     * Mengambil data tiket dari intent, membuat objek [TicketHistoryModel],
     * dan menyimpannya ke database Room. Fungsi ini mendukung tiket sekali jalan dan pulang-pergi.
     */
    private fun saveTicketToDatabase() {
        try {
            // Mengambil data objek BusModel untuk tiket pergi dari intent
            @Suppress("DEPRECATION")
            val busDepart = intent.getSerializableExtra("BUS_DEPART") as? BusModel

            // Mengambil data objek BusModel untuk tiket pulang dari intent (bisa null jika sekali jalan)
            @Suppress("DEPRECATION")
            val busReturn = intent.getSerializableExtra("BUS_RETURN") as? BusModel

            // Proses hanya jika data bus keberangkatan ada
            if (busDepart != null) {
                // Mengambil data string lainnya dari intent dengan nilai default string kosong
                val origin = intent.getStringExtra("ORIGIN") ?: ""
                val dest = intent.getStringExtra("DESTINATION") ?: ""
                val dateDepart = intent.getStringExtra("DATE") ?: ""
                val dateReturn = intent.getStringExtra("DATE_RETURN") ?: ""

                // Mengambil daftar kursi yang dipilih dan menggabungkannya menjadi satu string
                val seatsDepart =
                    intent.getStringArrayListExtra("SEATS_DEPART")?.joinToString(", ") ?: ""
                val seatsReturn =
                    intent.getStringArrayListExtra("SEATS_RETURN")?.joinToString(", ") ?: ""

                // Membuat ID booking unik, atau menggunakan yang sudah ada jika diteruskan dari intent
                val bookingId =
                    intent.getStringExtra("BOOKING_ID") ?: "ETB-${System.currentTimeMillis()}"

                // Menghitung total harga. Dimulai dengan harga tiket pergi.
                var totalPrice = busDepart.price
                // Jika ada tiket pulang, tambahkan harganya ke total.
                // Catatan: Logika ini bisa disesuaikan, misalnya dikali jumlah kursi.
                if (busReturn != null) totalPrice += busReturn.price // (Logic sederhana, bisa dikali jumlah kursi)

                // Membuat satu objek TicketHistoryModel yang berisi semua informasi tiket (termasuk data pulang jika ada)
                val newTicket = TicketHistoryModel(
                    // --- Data Umum & Keberangkatan ---
                    bookingId = bookingId,
                    operatorName = busDepart.operatorName,
                    busClass = busDepart.busClass,
                    origin = origin,
                    destination = dest,
                    date = dateDepart,
                    time = busDepart.departTime,
                    price = totalPrice, // Simpan total harga
                    status = "Aktif",
                    seats = seatsDepart,

                    // --- Data Pulang (diisi jika busReturn tidak null) ---
                    isRoundTrip = (busReturn != null),
                    returnOperatorName = busReturn?.operatorName,
                    returnBusClass = busReturn?.busClass,
                    returnOrigin = dest, // Balik: Jakarta
                    returnDestination = origin, // Balik: Padang
                    returnDate = dateReturn,
                    returnTime = busReturn?.departTime,
                    returnSeats = seatsReturn
                )

                // Menjalankan operasi database di thread background (IO) menggunakan Coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(applicationContext)
                    db.appDao().insertTicket(newTicket)
                }
            }
        } catch (e: Exception) {
            // Menangani jika terjadi error saat mengambil data atau menyimpan ke database
            e.printStackTrace()
        }
    }

    /**
     * Helper function untuk memulai animasi "pulse" (berdenyut) yang berulang tanpa henti.
     * @param view View yang akan dianimasikan (misal: ImageView).
     */
    private fun startPulseAnimation(view: View) {
        // Animasi untuk skala sumbu X dari 100% ke 115% dan kembali
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f)
        scaleX.repeatCount = ObjectAnimator.INFINITE
        scaleX.repeatMode = ObjectAnimator.REVERSE
        scaleY.repeatCount = ObjectAnimator.INFINITE
        scaleY.repeatMode = ObjectAnimator.REVERSE

        // Menggabungkan kedua animasi (scaleX dan scaleY) agar berjalan bersamaan
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 1000
        animatorSet.start()
    }

    /**
     * Helper function untuk memulai animasi "glow" (bercahaya) yang berulang tanpa henti.
     * @param view View yang akan dianimasikan (misal: background View).
     */
    private fun startGlowAnimation(view: View) {
        // Animasi untuk alpha (transparansi) dari 100% ke 60% dan kembali
        val fade = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.6f) // Efek pudar
        fade.repeatCount = ObjectAnimator.INFINITE
        fade.repeatMode = ObjectAnimator.REVERSE
        // Animasi untuk skala dari 100% ke 130% dan kembali
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f)
        scaleX.repeatCount = ObjectAnimator.INFINITE
        scaleX.repeatMode = ObjectAnimator.REVERSE
        scaleY.repeatCount = ObjectAnimator.INFINITE
        scaleY.repeatMode = ObjectAnimator.REVERSE
        val animatorSet = AnimatorSet()

        // Menggabungkan semua animasi (fade, scaleX, scaleY) agar berjalan bersamaan
        animatorSet.playTogether(fade, scaleX, scaleY)
        animatorSet.duration = 1500
        animatorSet.start()
    }
}