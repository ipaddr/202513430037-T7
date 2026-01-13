package com.contoh.eticketbus2025.ui.ticket

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.TicketHistoryModel
import com.contoh.eticketbus2025.data.source.FirestoreHelper
import com.contoh.eticketbus2025.ui.home.MainActivity
import com.contoh.eticketbus2025.ui.home.PromoActivity
import com.contoh.eticketbus2025.ui.profile.ProfileActivity
import com.contoh.eticketbus2025.utils.UserSession
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyTicketActivity : AppCompatActivity() {

    private lateinit var rvTickets: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var session: UserSession

    private var allTicketsFromDB: List<TicketHistoryModel> = listOf()
    private var currentFilter = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_ticket)

        session = UserSession(this)

        rvTickets = findViewById(R.id.rvTickets)
        emptyState = findViewById(R.id.emptyState)
        rvTickets.layoutManager = LinearLayoutManager(this)

        setupTabs()
        setupBottomNav()

        overridePendingTransition(0, 0)
    }

    override fun onResume() {
        super.onResume()
        loadTicketsFromCloud()
    }

    private fun loadTicketsFromCloud() {
        val userId = session.getUserId()
        Log.d("MyTicket", "Mengambil tiket untuk User ID: $userId")

        CoroutineScope(Dispatchers.IO).launch {
            // Ambil data dari Firestore
            val tickets = FirestoreHelper.getMyTickets(userId)

            withContext(Dispatchers.Main) {
                Log.d("MyTicket", "Jumlah tiket ditemukan: ${tickets.size}")

                if (tickets.isEmpty()) {
                    // Cek apakah User ID di Session cocok dengan di Database?
                    // Toast.makeText(this@MyTicketActivity, "Tidak ada tiket ditemukan", Toast.LENGTH_SHORT).show()
                }

                allTicketsFromDB = tickets

                // Set filter default ke "Semua" saat pertama load
                // Agar data langsung tampil tanpa harus klik tab
                if (currentFilter == "Semua") {
                    findViewById<TextView>(R.id.tabAll).performClick()
                } else {
                    updateList(currentFilter)
                }
            }
        }
    }

    private fun setupTabs() {
        val tabAll = findViewById<TextView>(R.id.tabAll)
        val tabActive = findViewById<TextView>(R.id.tabActive)
        val tabCompleted = findViewById<TextView>(R.id.tabCompleted)

        val colorWhite = ContextCompat.getColor(this, R.color.white)
        val colorDim = ContextCompat.getColor(this, R.color.white_dim)
        val colorBlue = ContextCompat.getColor(this, R.color.primary_blue)

        fun setTabState(selectedTab: TextView, filter: String) {
            currentFilter = filter

            listOf(tabAll, tabActive, tabCompleted).forEach { tab ->
                tab.setBackgroundResource(R.drawable.bg_tab_inactive)
                tab.backgroundTintList = null
                tab.setTextColor(colorDim)
            }

            selectedTab.setBackgroundResource(R.drawable.bg_tab_active)
            selectedTab.backgroundTintList = ColorStateList.valueOf(colorBlue)
            selectedTab.setTextColor(colorWhite)

            updateList(filter)
        }

        tabAll.setOnClickListener { setTabState(tabAll, "Semua") }
        tabActive.setOnClickListener { setTabState(tabActive, "Aktif") }
        tabCompleted.setOnClickListener { setTabState(tabCompleted, "Selesai") }
    }

    private fun updateList(filter: String) {
        // Filter Data
        val filteredList = if (filter == "Semua") {
            allTicketsFromDB
        } else {
            // Pastikan pengecekan status Case-Insensitive (mengabaikan huruf besar/kecil)
            allTicketsFromDB.filter { it.status.equals(filter, ignoreCase = true) }
        }

        findViewById<TextView>(R.id.tvTotalTickets).text = "${filteredList.size} Pesanan"

        if (filteredList.isEmpty()) {
            rvTickets.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvTickets.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            // Pasang adapter
            rvTickets.adapter = TicketHistoryAdapter(filteredList)
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_myticket

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }

                R.id.nav_myticket -> true
                R.id.nav_promo -> {
                    startActivity(Intent(this, PromoActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }

                R.id.nav_account -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }

                else -> false
            }
        }
    }
}