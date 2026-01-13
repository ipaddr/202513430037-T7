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
import androidx.appcompat.app.AppCompatDelegate
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.TicketHistoryModel

class ETicketDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_eticket_detail)

        // 1. Ambil Data
        @Suppress("DEPRECATION")
        val ticket = intent.getSerializableExtra("TICKET_DATA") as? TicketHistoryModel

        if (ticket != null) {
            setupUI(ticket)
        } else {
            Toast.makeText(this, "Data tiket tidak valid", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupListeners()
    }

    private fun setupUI(ticket: TicketHistoryModel) {
        // --- A. DATA UMUM ---
        findViewById<TextView>(R.id.tvBookingId).text = ticket.bookingId
        findViewById<TextView>(R.id.tvTicketCode).text = ticket.bookingId

        // --- B. TIKET PERGI ---
        findViewById<TextView>(R.id.tvOperatorDepart).text = ticket.operatorName
        findViewById<TextView>(R.id.tvClassDepart).text = ticket.busClass
        findViewById<TextView>(R.id.tvOriginDepart).text = ticket.origin
        findViewById<TextView>(R.id.tvDestDepart).text = ticket.destination
        findViewById<TextView>(R.id.tvTimeDepart).text = ticket.time
        findViewById<TextView>(R.id.tvDateDepart).text = ticket.date
        findViewById<TextView>(R.id.tvSeatsDepart).text = ticket.seats
        findViewById<TextView>(R.id.tvTimeArriveDepart).text = "-"

        // --- C. TIKET PULANG ---
        val layoutReturn = findViewById<LinearLayout>(R.id.layoutReturnTicket)

        // PERBAIKAN: Gunakan .roundTrip (bukan .isRoundTrip)
        if (ticket.roundTrip) {
            layoutReturn.visibility = View.VISIBLE

            findViewById<TextView>(R.id.tvOperatorReturn).text = ticket.returnOperatorName ?: "-"
            findViewById<TextView>(R.id.tvClassReturn).text = ticket.returnBusClass ?: "-"

            // Logika Rute Balik
            findViewById<TextView>(R.id.tvOriginReturn).text = ticket.returnOrigin ?: ticket.destination
            findViewById<TextView>(R.id.tvDestReturn).text = ticket.returnDestination ?: ticket.origin

            findViewById<TextView>(R.id.tvTimeDepartReturn).text = ticket.returnTime ?: "-"
            findViewById<TextView>(R.id.tvDateReturn).text = ticket.returnDate ?: "-"
            findViewById<TextView>(R.id.tvSeatsReturn).text = ticket.returnSeats ?: "-"
            findViewById<TextView>(R.id.tvTimeArriveReturn).text = "-"

        } else {
            layoutReturn.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnShare).setOnClickListener {
            val id = findViewById<TextView>(R.id.tvBookingId).text
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "E-Ticket: $id")
            startActivity(Intent.createChooser(intent, "Bagikan"))
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            Toast.makeText(this, "Fitur Simpan PDF segera hadir", Toast.LENGTH_SHORT).show()
        }
    }
}