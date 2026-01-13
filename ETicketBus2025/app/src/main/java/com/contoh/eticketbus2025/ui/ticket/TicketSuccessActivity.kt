package com.contoh.eticketbus2025.ui.ticket

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.BusModel
import com.contoh.eticketbus2025.data.model.TicketHistoryModel
import com.contoh.eticketbus2025.data.source.FirestoreHelper
import com.contoh.eticketbus2025.ui.home.MainActivity
import com.contoh.eticketbus2025.utils.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketSuccessActivity : AppCompatActivity() {

    private var currentTicket: TicketHistoryModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_success)

        setupAnimations()
        processAndSaveTicket()

        findViewById<Button>(R.id.btnViewTicket).setOnClickListener {
            if (currentTicket != null) {
                val intentNext = Intent(this, ETicketDetailActivity::class.java)
                intentNext.putExtra("TICKET_DATA", currentTicket)
                startActivity(intentNext)
                finish()
            } else {
                Toast.makeText(this, "Data tiket sedang diproses...", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(R.id.btnBackHome).setOnClickListener {
            val intentHome = Intent(this, MainActivity::class.java)
            intentHome.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intentHome)
        }
    }

    private fun processAndSaveTicket() {
        val session = UserSession(this)
        val userId = session.getUserId()

        try {
            @Suppress("DEPRECATION")
            val busDepart = intent.getSerializableExtra("BUS_DEPART") as? BusModel

            @Suppress("DEPRECATION")
            val busReturn = intent.getSerializableExtra("BUS_RETURN") as? BusModel

            if (busDepart != null) {
                val origin = intent.getStringExtra("ORIGIN") ?: ""
                val dest = intent.getStringExtra("DESTINATION") ?: ""
                val dateDepart = intent.getStringExtra("DATE") ?: ""
                val dateReturn = intent.getStringExtra("DATE_RETURN") ?: ""
                val seatsDepart =
                    intent.getStringArrayListExtra("SEATS_DEPART")?.joinToString(", ") ?: ""
                val seatsReturn =
                    intent.getStringArrayListExtra("SEATS_RETURN")?.joinToString(", ") ?: ""
                val bookingId =
                    intent.getStringExtra("BOOKING_ID") ?: "ETB-${System.currentTimeMillis()}"
                val totalPrice = intent.getDoubleExtra("TOTAL_PRICE", busDepart.price)

                // PERBAIKAN: Gunakan parameter roundTrip
                currentTicket = TicketHistoryModel(
                    bookingId = bookingId,
                    userId = userId,
                    operatorName = busDepart.operatorName,
                    busClass = busDepart.busClass,
                    origin = origin,
                    destination = dest,
                    date = dateDepart,
                    time = busDepart.departTime,
                    price = totalPrice,
                    status = "Aktif",
                    seats = seatsDepart,
                    timestamp = System.currentTimeMillis(),

                    // Parameter roundTrip (sesuai Model baru)
                    roundTrip = (busReturn != null),
                    returnOperatorName = busReturn?.operatorName,
                    returnBusClass = busReturn?.busClass,
                    returnOrigin = dest,
                    returnDestination = origin,
                    returnDate = dateReturn,
                    returnTime = busReturn?.departTime,
                    returnSeats = seatsReturn
                )

                CoroutineScope(Dispatchers.IO).launch {
                    FirestoreHelper.saveTicket(currentTicket!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupAnimations() {
        val imgSuccess = findViewById<ImageView>(R.id.imgSuccess)
        val glowBackground = findViewById<View>(R.id.viewGlow) ?: imgSuccess.parent as View
        imgSuccess.scaleX = 0f; imgSuccess.scaleY = 0f
        glowBackground.scaleX = 0f; glowBackground.scaleY = 0f
        imgSuccess.animate().scaleX(1f).scaleY(1f).setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()
        glowBackground.animate().scaleX(1f).scaleY(1f).setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { startPulseAnimation(imgSuccess); startGlowAnimation(glowBackground) }
            .start()
    }

    private fun startPulseAnimation(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f)
        scaleX.repeatCount = ObjectAnimator.INFINITE; scaleX.repeatMode = ObjectAnimator.REVERSE
        scaleY.repeatCount = ObjectAnimator.INFINITE; scaleY.repeatMode = ObjectAnimator.REVERSE
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY); animatorSet.duration = 1000; animatorSet.start()
    }

    private fun startGlowAnimation(view: View) {
        val fade = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.6f)
        fade.repeatCount = ObjectAnimator.INFINITE; fade.repeatMode = ObjectAnimator.REVERSE
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f)
        scaleX.repeatCount = ObjectAnimator.INFINITE; scaleX.repeatMode = ObjectAnimator.REVERSE
        scaleY.repeatCount = ObjectAnimator.INFINITE; scaleY.repeatMode = ObjectAnimator.REVERSE
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fade, scaleX, scaleY); animatorSet.duration =
            1500; animatorSet.start()
    }
}