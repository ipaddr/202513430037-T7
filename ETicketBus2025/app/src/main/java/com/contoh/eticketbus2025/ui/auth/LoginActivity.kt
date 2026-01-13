package com.contoh.eticketbus2025.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.database.AppDatabase
import com.contoh.eticketbus2025.ui.home.MainActivity
import com.contoh.eticketbus2025.utils.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * LoginActivity menangani proses otentikasi pengguna.
 * Ini mencakup pemeriksaan sesi untuk login otomatis, validasi input,
 * dan interaksi dengan database untuk memverifikasi kredensial pengguna.
 */
class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false // State untuk melacak visibilitas password
    private lateinit var session: UserSession // Manajer sesi untuk menangani status login pengguna

    // Metode onCreate dipanggil saat aktivitas pertama kali dibuat.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek Sesi Login (Auto Login)
        session = UserSession(this)
        if (session.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        // Inisialisasi komponen UI dari layout
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnTogglePass = findViewById<ImageButton>(R.id.btnTogglePass)

        // Mengatur listener untuk tombol toggle visibilitas password
        btnTogglePass.setOnClickListener {
            isPasswordVisible = !isPasswordVisible // Ubah state visibilitas
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnTogglePass.setImageResource(R.drawable.ic_visibility)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnTogglePass.setImageResource(R.drawable.ic_visibility_off)
            }
            etPassword.setSelection(etPassword.text.length) // Pindahkan kursor ke akhir teks
        }

        // Mengatur listener untuk tombol login
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            // Validasi input: pastikan email dan password tidak kosong
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Mohon isi email dan password", Toast.LENGTH_SHORT).show()
            } else {
                performLogin(email, password) // Jalankan proses login
            }
        }

        // Mengatur listener untuk tautan ke halaman registrasi
        findViewById<TextView>(R.id.btnRegisterLink).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Menjalankan proses login di background thread menggunakan Coroutine.
     * @param email Email yang dimasukkan pengguna.
     * @param pass Password yang dimasukkan pengguna.
     */
    private fun performLogin(email: String, pass: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Akses database di thread IO untuk menghindari pemblokiran UI
            val db = AppDatabase.getDatabase(applicationContext)
            val user = db.userDao().loginUser(email, pass)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    // Login Sukses -> Simpan Sesi
                    session.createLoginSession(user.id, user.fullName)

                    Toast.makeText(
                        this@LoginActivity,
                        "Selamat datang, ${user.fullName}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Pindah ke Home
                    val intent = Intent(this@LoginActivity, MainActivity::class.java) // Buat intent ke MainActivity
                    startActivity(intent) // Mulai MainActivity
                    finish() // Tutup LoginActivity agar pengguna tidak bisa kembali dengan tombol back
                } else {
                    // Jika user null, berarti login gagal
                    Toast.makeText(
                        this@LoginActivity,
                        "Email atau Password salah!", // Tampilkan pesan error
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}