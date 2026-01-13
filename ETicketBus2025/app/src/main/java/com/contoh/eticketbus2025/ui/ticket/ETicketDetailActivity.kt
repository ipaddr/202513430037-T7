package com.contoh.eticketbus2025.ui.ticket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.BusModel
import com.contoh.eticketbus2025.data.model.TicketHistoryModel
import com.contoh.eticketbus2025.ui.home.MainActivity

/**
 * ETicketDetailActivity adalah activity yang bertanggung jawab untuk menampilkan detail e-tiket.
 * Activity ini dapat menampilkan tiket dari dua sumber:
 * 1. Dari alur pemesanan baru (setelah pembayaran berhasil).
 * 2. Dari riwayat tiket yang sudah tersimpan di database (MyTicketActivity).
 */
class ETicketDetailActivity : AppCompatActivity() {

    /**
     * Fungsi yang dipanggil saat activity pertama kali dibuat.
     * Mengatur layout dan memulai proses memuat data tiket.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eticket_detail)

        // Memuat data dan mengatur UI, jika berhasil, maka atur listener untuk tombol.
        if (loadDataAndSetupUI()) {
            setupListeners()
        }
    }

    /**
     * Fungsi utama untuk memuat data dari Intent dan memutuskan alur mana yang akan digunakan.
     * Fungsi ini membedakan antara tiket dari riwayat (TicketHistoryModel) atau dari pemesanan baru (BusModel).
     * Menggunakan try-catch untuk menangani error jika data yang diterima tidak valid.
     * @return Boolean - true jika data berhasil dimuat, false jika terjadi error.
     */
    private fun loadDataAndSetupUI(): Boolean {
        return try {
            // Cek apakah data datang dari Riwayat (Database)
            @Suppress("DEPRECATION")
            val historyTicket = intent.getSerializableExtra("TICKET_HISTORY") as? TicketHistoryModel

            if (historyTicket != null) {
                // SKENARIO 1: Data berasal dari riwayat tiket. Panggil fungsi untuk setup UI dari history.
                setupUIFromHistory(historyTicket)
            } else {
                // SKENARIO 2: Data berasal dari alur pemesanan baru. Panggil fungsi untuk setup UI dari booking.
                setupUIFromBooking()
            }

            // Kembalikan true menandakan proses berhasil.
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal memuat data tiket", Toast.LENGTH_SHORT).show()
            finish()
            false
        }
    }

    /**
     * Menyiapkan dan menampilkan data tiket ke UI berdasarkan data dari [TicketHistoryModel].
     * Fungsi ini dipanggil jika pengguna membuka detail tiket dari halaman riwayat.
     * @param ticket Objek TicketHistoryModel yang berisi detail tiket dari database.
     */
    private fun setupUIFromHistory(ticket: TicketHistoryModel) {
        // HEADER
        findViewById<TextView>(R.id.tvBookingId).text = ticket.bookingId
        findViewById<TextView>(R.id.tvTicketCode).text =
            "CODE: ${ticket.bookingId.replace("ETB", "QR")}-HIS"

        // === BAGIAN PERGI ===
        findViewById<TextView>(R.id.tvOperatorDepart).text = ticket.operatorName
        findViewById<TextView>(R.id.tvClassDepart).text = ticket.busClass
        findViewById<TextView>(R.id.tvOriginDepart).text = ticket.origin
        findViewById<TextView>(R.id.tvDestDepart).text = ticket.destination
        findViewById<TextView>(R.id.tvTimeDepart).text = ticket.time
        findViewById<TextView>(R.id.tvDateDepart).text = ticket.date
        findViewById<TextView>(R.id.tvSeatsDepart).text = ticket.seats

        // Placeholder untuk jam tiba (karena tidak disimpan di history sederhana)
        findViewById<TextView>(R.id.tvTimeArriveDepart).text = "-"

        // === BAGIAN PULANG (CEK IS_ROUND_TRIP) ===
        val layoutReturn = findViewById<LinearLayout>(R.id.layoutReturnTicket)

        if (ticket.isRoundTrip) {
            layoutReturn.visibility = View.VISIBLE

            // Isi data dari kolom Return di database
            findViewById<TextView>(R.id.tvOperatorReturn).text = ticket.returnOperatorName
            findViewById<TextView>(R.id.tvClassReturn).text = ticket.returnBusClass

            findViewById<TextView>(R.id.tvOriginReturn).text = ticket.returnOrigin
            findViewById<TextView>(R.id.tvDestReturn).text = ticket.returnDestination

            findViewById<TextView>(R.id.tvTimeDepartReturn).text = ticket.returnTime
            findViewById<TextView>(R.id.tvDateReturn).text = ticket.returnDate
            findViewById<TextView>(R.id.tvSeatsReturn).text = ticket.returnSeats

            findViewById<TextView>(R.id.tvTimeArriveReturn).text = "-" // Placeholder
        } else {
            layoutReturn.visibility = View.GONE
        }
    }

    /**
     * Menyiapkan dan menampilkan data tiket ke UI berdasarkan data dari alur pemesanan baru.
     * Fungsi ini dipanggil setelah pengguna berhasil melakukan pembayaran.
     * Data diambil dari Intent yang membawa [BusModel] dan informasi lainnya.
     */
    private fun setupUIFromBooking() {
        // Mengambil data tiket pergi (wajib ada).
        @Suppress("DEPRECATION")
        val busDepart = intent.getSerializableExtra("BUS_DEPART") as BusModel

        // Mengambil data tiket pulang (opsional, bisa null jika bukan perjalanan pulang-pergi).
        @Suppress("DEPRECATION")
        val busReturn = intent.getSerializableExtra("BUS_RETURN") as? BusModel

        val origin = intent.getStringExtra("ORIGIN") ?: "-"
        val dest = intent.getStringExtra("DESTINATION") ?: "-"
        val dateStr = intent.getStringExtra("DATE") ?: "-"
        val dateReturnStr = intent.getStringExtra("DATE_RETURN") ?: "-"
        val seatsDepart = intent.getStringArrayListExtra("SEATS_DEPART") ?: arrayListOf()
        val seatsReturn = intent.getStringArrayListExtra("SEATS_RETURN") ?: arrayListOf()
        val bookingId = intent.getStringExtra("BOOKING_ID") ?: "ETB-NEW"

        // Mengisi data Header
        findViewById<TextView>(R.id.tvBookingId).text = bookingId
        findViewById<TextView>(R.id.tvTicketCode).text =
            "CODE: ${bookingId.replace("ETB", "QR")}-NEW"

        // Mengisi data tiket Pergi
        findViewById<TextView>(R.id.tvOperatorDepart).text = busDepart.operatorName
        findViewById<TextView>(R.id.tvClassDepart).text = busDepart.busClass
        findViewById<TextView>(R.id.tvOriginDepart).text = origin
        findViewById<TextView>(R.id.tvDestDepart).text = dest
        findViewById<TextView>(R.id.tvTimeDepart).text = busDepart.departTime
        findViewById<TextView>(R.id.tvTimeArriveDepart).text = busDepart.arriveTime
        findViewById<TextView>(R.id.tvDateDepart).text = dateStr
        findViewById<TextView>(R.id.tvSeatsDepart).text = seatsDepart.joinToString(", ")

        // Mengecek dan mengisi data tiket Pulang jika ada.
        val layoutReturn = findViewById<LinearLayout>(R.id.layoutReturnTicket)
        if (busReturn != null) {
            layoutReturn.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvOperatorReturn).text = busReturn.operatorName
            findViewById<TextView>(R.id.tvClassReturn).text = busReturn.busClass
            findViewById<TextView>(R.id.tvOriginReturn).text = dest
            findViewById<TextView>(R.id.tvDestReturn).text = origin
            findViewById<TextView>(R.id.tvTimeDepartReturn).text = busReturn.departTime
            findViewById<TextView>(R.id.tvTimeArriveReturn).text = busReturn.arriveTime
            findViewById<TextView>(R.id.tvDateReturn).text = dateReturnStr
            findViewById<TextView>(R.id.tvSeatsReturn).text = seatsReturn.joinToString(", ")
        } else {
            layoutReturn.visibility = View.GONE
        }
    }

    /**
     * Mengatur listener untuk semua tombol interaktif di dalam activity ini.
     * Termasuk tombol kembali, simpan, dan bagikan.
     */
    private fun setupListeners() {
        // Listener untuk tombol kembali (btnBack)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            // Arahkan kembali ke MyTicketActivity. Ini adalah tindakan default yang aman,
            // karena baik dari alur baru maupun riwayat, pengguna sering ingin melihat daftar tiketnya.
            // FLAG_ACTIVITY_CLEAR_TOP dan FLAG_ACTIVITY_SINGLE_TOP memastikan tidak ada tumpukan activity yang tidak perlu.
            val intent = Intent(this, MyTicketActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Listener untuk tombol simpan (btnSave)
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            // Menampilkan pesan Toast sebagai simulasi fitur "Simpan Tiket".
            Toast.makeText(this, "Tiket disimpan (Simulasi)", Toast.LENGTH_SHORT).show()
        }

        // Listener untuk tombol bagikan (btnShare)
        findViewById<Button>(R.id.btnShare).setOnClickListener {
            // Mengambil ID booking dari TextView dan membuat intent untuk berbagi teks.
            // Ini memungkinkan pengguna membagikan informasi tiketnya ke aplikasi lain.
            val bookingId = findViewById<TextView>(R.id.tvBookingId).text
            val shareBody = "E-Ticket Bus: $bookingId"
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(intent, "Bagikan Tiket"))
        }
    }

    /**
     * Override fungsi [onBackPressed] standar Android.
     * Ketika pengguna menekan tombol kembali fisik di perangkat,
     * kita memicu klik pada tombol kembali di UI (btnBack) untuk menjaga konsistensi alur navigasi.
     */
    override fun onBackPressed() {
        findViewById<ImageButton>(R.id.btnBack).performClick()
    }
}