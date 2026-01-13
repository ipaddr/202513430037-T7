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
import com.contoh.eticketbus2025.data.source.FirestoreHelper // <-- GANTI KE FIRESTORE
import com.contoh.eticketbus2025.ui.home.MainActivity
import com.contoh.eticketbus2025.utils.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * LoginActivity menangani proses otentikasi pengguna menggunakan Firestore.
 */
class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false
    private lateinit var session: UserSession

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

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnTogglePass = findViewById<ImageButton>(R.id.btnTogglePass)

        // Toggle Password Visibility
        btnTogglePass.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnTogglePass.setImageResource(R.drawable.ic_visibility)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnTogglePass.setImageResource(R.drawable.ic_visibility_off)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // Login Button
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = etEmail.text.toString().trim() // Tambahkan trim() untuk hapus spasi
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Mohon isi email dan password", Toast.LENGTH_SHORT).show()
            } else {
                performLogin(email, password)
            }
        }

        // Register Link
        findViewById<TextView>(R.id.btnRegisterLink).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Menjalankan proses login ke Firestore.
     */
    private fun performLogin(email: String, pass: String) {
        // Tampilkan loading (Optional, bisa tambahkan ProgressBar nanti)
        Toast.makeText(this, "Sedang masuk...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            // --- MENGGUNAKAN FIRESTORE HELPER ---
            val user = FirestoreHelper.loginUser(email, pass)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    // Login Sukses -> Simpan Sesi
                    // NOTE: Pastikan UserSession Anda mendukung ID bertipe String
                    session.createLoginSession(user.id, user.fullName)

                    Toast.makeText(
                        this@LoginActivity,
                        "Selamat datang, ${user.fullName}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Email atau Password salah!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}