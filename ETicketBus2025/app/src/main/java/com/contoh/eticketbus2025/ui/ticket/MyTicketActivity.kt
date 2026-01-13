package com.contoh.eticketbus2025.ui.ticket

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.database.AppDatabase
import com.contoh.eticketbus2025.data.model.TicketHistoryModel
import com.contoh.eticketbus2025.ui.home.MainActivity
import com.contoh.eticketbus2025.ui.home.PromoActivity
import com.contoh.eticketbus2025.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MyTicketActivity adalah kelas Activity yang bertanggung jawab untuk menampilkan daftar tiket
 * yang telah dipesan oleh pengguna. Halaman ini memiliki fitur filter tiket berdasarkan status
 * (Semua, Aktif, Selesai) dan navigasi bawah untuk berpindah antar halaman utama aplikasi.
 */
class MyTicketActivity : AppCompatActivity() {

    // Komponen UI untuk menampilkan daftar tiket dalam bentuk list.
    private lateinit var rvTickets: RecyclerView
    // Layout yang akan ditampilkan jika tidak ada tiket (empty state).
    private lateinit var emptyState: LinearLayout

    // Variabel untuk menampung semua data tiket yang diambil dari database lokal.
    private var allTicketsFromDB: List<TicketHistoryModel> = listOf()

    // Menyimpan status filter yang sedang aktif ("Semua", "Aktif", atau "Selesai").
    private var currentFilter = "Semua"

    /**
     * Fungsi yang dipanggil saat Activity pertama kali dibuat.
     * Fungsi ini menginisialisasi layout, komponen UI, tab filter, dan navigasi bawah.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_ticket)

        // Inisialisasi komponen UI dari layout.
        rvTickets = findViewById(R.id.rvTickets)
        emptyState = findViewById(R.id.emptyState)
        rvTickets.layoutManager = LinearLayoutManager(this)

        // Menyiapkan fungsi untuk tab filter dan navigasi bawah.
        setupTabs()
        setupBottomNav()

        // Mematikan animasi transisi default saat berpindah ke Activity ini.
        overridePendingTransition(0, 0)

        // Secara programatis, "klik" tab "Semua" untuk mengatur tampilan awal.
        findViewById<TextView>(R.id.tabAll).performClick()
    }

    /**
     * Fungsi yang dipanggil setiap kali Activity ini kembali ke foreground (misalnya, setelah
     * kembali dari halaman detail tiket). Ini memastikan data tiket selalu yang terbaru.
     */
    override fun onResume() {
        super.onResume()
        // Muat ulang data tiket dari database.
        loadTicketsFromDB()
    }

    /**
     * Mengambil data tiket dari database lokal (Room) secara asynchronous menggunakan Coroutines.
     * Setelah data berhasil diambil, fungsi ini memperbarui UI pada Main Thread.
     */
    private fun loadTicketsFromDB() {
        // Menjalankan operasi database di thread I/O untuk tidak memblokir UI.
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Mendapatkan instance dari database.
            val db = AppDatabase.getDatabase(applicationContext)

            // 2. Mengambil semua data tiket dari tabel.
            val tickets = db.appDao().getAllTickets()

            // 3. Update UI di Main Thread
            withContext(Dispatchers.Main) {
                allTicketsFromDB = tickets
                // Refresh list sesuai filter yang sedang aktif
                updateList(currentFilter)
            }
        }
    }

    /**
     * Mengatur logika dan listener untuk tab filter (Semua, Aktif, Selesai).
     * Fungsi ini menangani perubahan tampilan tab saat salah satu dipilih.
     */
    private fun setupTabs() {
        val tabAll = findViewById<TextView>(R.id.tabAll)
        val tabActive = findViewById<TextView>(R.id.tabActive)
        val tabCompleted = findViewById<TextView>(R.id.tabCompleted)

        val colorWhite = ContextCompat.getColor(this, R.color.white)
        val colorDim = ContextCompat.getColor(this, R.color.white_dim)
        val colorBlue = ContextCompat.getColor(this, R.color.primary_blue)

        // Fungsi internal untuk mengatur status visual dari tab yang dipilih.
        fun setTabState(selectedTab: TextView, filter: String) {
            // Simpan filter yang dipilih ke variabel global
            currentFilter = filter

            // 1. Reset tampilan semua tab ke kondisi tidak aktif (inactive).
            listOf(tabAll, tabActive, tabCompleted).forEach { tab ->
                tab.setBackgroundResource(R.drawable.bg_tab_inactive)
                tab.backgroundTintList = null
                tab.setTextColor(colorDim)
            }

            // 2. Atur tampilan tab yang dipilih menjadi aktif (active).
            selectedTab.setBackgroundResource(R.drawable.bg_tab_active)
            selectedTab.backgroundTintList = ColorStateList.valueOf(colorBlue)
            selectedTab.setTextColor(colorWhite)

            // 3. Perbarui daftar tiket sesuai dengan filter yang baru dipilih.
            updateList(filter)
        }

        // Menambahkan OnClickListener untuk setiap tab.
        tabAll.setOnClickListener { setTabState(tabAll, "Semua") }
        tabActive.setOnClickListener { setTabState(tabActive, "Aktif") }
        tabCompleted.setOnClickListener { setTabState(tabCompleted, "Selesai") }
    }

    /**
     * Memperbarui RecyclerView berdasarkan filter yang dipilih.
     * Fungsi ini menyaring daftar tiket dan menampilkannya di RecyclerView.
     * Jika daftar kosong, maka akan menampilkan tampilan "empty state".
     * @param filter String filter yang akan digunakan ("Semua", "Aktif", "Selesai").
     */
    private fun updateList(filter: String) {
        // Melakukan filter pada `allTicketsFromDB` berdasarkan parameter filter.
        val filteredList = if (filter == "Semua") {
            allTicketsFromDB
        } else {
            allTicketsFromDB.filter { it.status == filter }
        }

        // Memperbarui teks yang menunjukkan jumlah pesanan.
        findViewById<TextView>(R.id.tvTotalTickets).text = "${filteredList.size} Pesanan"

        // Memeriksa apakah daftar hasil filter kosong atau tidak.
        if (filteredList.isEmpty()) {
            // Jika kosong, sembunyikan RecyclerView dan tampilkan layout empty state.
            rvTickets.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            // Jika ada isinya, tampilkan RecyclerView, sembunyikan empty state,
            // dan set adapter baru dengan data yang sudah difilter.
            rvTickets.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            rvTickets.adapter = TicketHistoryAdapter(filteredList)
        }
    }

    /**
     * Mengatur fungsionalitas BottomNavigationView untuk navigasi antar halaman utama.
     * Menangani perpindahan ke Home, Promo, dan Profile.
     */
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_myticket

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Pindah ke MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                // Jika item yang dipilih adalah halaman ini sendiri, tidak melakukan apa-apa.
                R.id.nav_myticket -> true

                R.id.nav_promo -> {
                    // Pindah ke PromoActivity
                    val intent = Intent(this, PromoActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.nav_account -> {
                    // Pindah ke ProfileActivity
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}