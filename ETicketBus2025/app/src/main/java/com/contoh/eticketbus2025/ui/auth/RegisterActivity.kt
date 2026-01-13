package com.contoh.eticketbus2025.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.database.AppDatabase
import com.contoh.eticketbus2025.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mengatur layout untuk activity ini
        setContentView(R.layout.activity_register)

        // Inisialisasi komponen UI dari layout
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPass = findViewById<EditText>(R.id.etRegPassword)

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            // Mengambil data dari EditText saat tombol register diklik
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val phone = etPhone.text.toString()
            val password = etPass.text.toString()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
            } else {
                // Jika semua data terisi, panggil fungsi registerUser
                registerUser(
                    UserEntity(
                        // Membuat objek UserEntity dengan data yang diinput
                        fullName = name,
                        email = email,
                        phone = phone,
                        password = password
                    )
                )
            }
        }

        // Menangani klik pada tombol "Kembali ke Login"
        findViewById<TextView>(R.id.btnBackToLogin).setOnClickListener {
            finish() // Menutup activity saat ini dan kembali ke activity sebelumnya (LoginActivity)
        }
    }

    // Fungsi untuk mendaftarkan pengguna baru
    private fun registerUser(user: UserEntity) {
        // Menjalankan operasi database di thread IO menggunakan Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)

            val existingUser = db.userDao().getUserByEmail(user.email)

            withContext(Dispatchers.Main) {
                if (existingUser != null) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Email sudah terdaftar!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Jika email belum terdaftar, simpan pengguna baru ke database
                    saveUserToDB(user)
                }
            }
        }
    }

    // Fungsi untuk menyimpan data pengguna ke database
    private fun saveUserToDB(user: UserEntity) {
        // Menjalankan operasi database di thread IO
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            db.userDao().registerUser(user)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Registrasi Berhasil! Silakan Login.",
                    Toast.LENGTH_LONG
                ).show()
                finish() // Menutup activity Register dan kembali ke LoginActivity
            }
        }
    }
}