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
import com.contoh.eticketbus2025.data.database.AppDatabase
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

/**
 * ProfileActivity menangani tampilan halaman profil pengguna.
 * Halaman ini menampilkan informasi pengguna, statistik perjalanan, dan menu untuk berbagai aksi
 * seperti edit profil, metode pembayaran, bantuan, dan logout.
 * Activity ini juga mengelola navigasi bawah (BottomNavigationView).
 */
class ProfileActivity : AppCompatActivity() {

    // Variabel untuk mengelola sesi pengguna, seperti mendapatkan ID pengguna dan proses logout.
    private lateinit var session: UserSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Memaksa mode terang (menonaktifkan mode gelap) untuk konsistensi UI.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_profile)

        // Inisialisasi UserSession untuk mengakses data sesi.
        session = UserSession(this)

        // Mengatur fungsi untuk navigasi bawah dan menu aksi.
        setupBottomNav()
        setupMenuActions()
    }

    /**
     * Dipanggil setiap kali activity kembali ke foreground.
     * Digunakan untuk memuat ulang data pengguna agar informasi yang ditampilkan selalu terbaru,
     * terutama setelah pengguna mengedit profilnya di halaman lain.
     */
    override fun onResume() {
        super.onResume()
        // Reload data setiap kali halaman ini muncul (agar update setelah edit profil)
        loadUserDataAndStats()
    }

    /**
     * Memuat data pengguna (nama, email, telepon) dan statistik (total perjalanan, tiket aktif, poin)
     * dari database secara asynchronous menggunakan Coroutines.
     */
    private fun loadUserDataAndStats() {
        // Mendapatkan ID pengguna yang sedang login dari sesi.
        val userId = session.getUserId()

        // Menjalankan operasi database di thread I/O untuk tidak memblokir UI.
        CoroutineScope(Dispatchers.IO).launch {
            // Mendapatkan instance database.
            val db = AppDatabase.getDatabase(applicationContext)

            // 1. Mengambil data pengguna berdasarkan ID.
            val user = db.userDao().getUserById(userId)

            // 2. Mengambil semua tiket untuk menghitung statistik.
            val tickets = db.appDao().getAllTickets()
            val totalTrips = tickets.size
            val activeTickets = tickets.count { it.status == "Aktif" }
            // Logika Poin Sederhana: 1 Tiket yang telah dibeli = 50 Poin.
            val points = totalTrips * 50

            withContext(Dispatchers.Main) {
                if (user != null) {
                    findViewById<TextView>(R.id.tvName).text = user.fullName
                    findViewById<TextView>(R.id.tvEmail).text = user.email
                    findViewById<TextView>(R.id.tvPhone).text = user.phone
                }

                // TODO: Update Statistik UI (Pastikan ID di XML sesuai urutan LinearLayout)
                // Tips: Anda mungkin perlu memberi ID pada TextView angka statistik di XML Anda
                // Asumsi ID: tvStatTrips, tvStatActive, tvStatPoints

                // Cara akses manual ke child LinearLayout jika belum ada ID spesifik:
                // (Ini cara cepat, sebaiknya beri ID di XML nanti)
                // val statLayout = findViewById<LinearLayout>(R.id.statLayout) ...
            }
        }
    }

    /**
     * Mengatur listener OnClickListener untuk setiap menu di halaman profil.
     */
    private fun setupMenuActions() {
        // Aksi saat menu "Edit Profil" diklik: membuka EditProfileActivity.
        findViewById<LinearLayout>(R.id.menuEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Aksi saat menu "Metode Pembayaran" diklik: membuka PaymentMethodActivity.
        findViewById<LinearLayout>(R.id.menuPayment).setOnClickListener {
            startActivity(Intent(this, PaymentMethodActivity::class.java))
        }

        // Aksi saat menu "Bantuan" diklik: membuka HelpActivity.
        findViewById<LinearLayout>(R.id.menuHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        // Aksi saat tombol "Logout" diklik.
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.logoutUser()
            Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * Mengatur BottomNavigationView, menandai item "Akun" sebagai aktif,
     * dan menangani navigasi ketika item lain dipilih.
     */
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_account

        bottomNav.setOnNavigationItemSelectedListener { item ->
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
                    val intent = Intent(this, MyTicketActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_promo -> {
                    val intent = Intent(this, PromoActivity::class.java)
                    startActivity(intent)
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