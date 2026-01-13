package com.contoh.eticketbus2025.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.source.FirestoreHelper
import com.contoh.eticketbus2025.ui.auth.LoginActivity
import com.contoh.eticketbus2025.ui.home.MainActivity
import com.contoh.eticketbus2025.ui.home.PromoActivity
import com.contoh.eticketbus2025.ui.ticket.MyTicketActivity
import com.contoh.eticketbus2025.utils.UserSession
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var session: UserSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_profile)

        session = UserSession(this)

        setupBottomNav()
        setupMenuActions()
    }

    override fun onResume() {
        super.onResume()
        loadUserDataAndStats()
    }

    private fun loadUserDataAndStats() {
        val userId = session.getUserId()

        CoroutineScope(Dispatchers.IO).launch {
            val user = FirestoreHelper.getUserById(userId)
            val tickets = FirestoreHelper.getMyTickets(userId)

            val totalTrips = tickets.size
            val activeTickets = tickets.count { it.status == "Aktif" }
            val points = totalTrips * 50

            withContext(Dispatchers.Main) {
                user?.let {
                    findViewById<TextView>(R.id.tvName).text = it.fullName
                    findViewById<TextView>(R.id.tvEmail).text = it.email
                    findViewById<TextView>(R.id.tvPhone).text = it.phone
                }

                findViewById<TextView>(R.id.tvStatTotalTrips).text = totalTrips.toString()
                findViewById<TextView>(R.id.tvStatActiveTickets).text = activeTickets.toString()

                val formatNumber = NumberFormat.getNumberInstance(Locale("id", "ID"))
                findViewById<TextView>(R.id.tvStatPoints).text = formatNumber.format(points)
            }
        }
    }

    private fun setupMenuActions() {
        // 1. Edit Profil
        findViewById<LinearLayout>(R.id.menuEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // 2. Metode Pembayaran (BARU)
        findViewById<LinearLayout>(R.id.menuPayment).setOnClickListener {
            startActivity(Intent(this, PaymentMethodActivity::class.java))
        }

        // 3. Bantuan & Dukungan (BARU)
        findViewById<LinearLayout>(R.id.menuHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        // 4. Logout
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.logoutUser()
            Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_account

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
                    startActivity(Intent(this, MyTicketActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_promo -> {
                    startActivity(Intent(this, PromoActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_account -> true
                else -> false
            }
        }
    }
}