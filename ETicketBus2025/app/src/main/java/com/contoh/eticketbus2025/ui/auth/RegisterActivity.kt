package com.contoh.eticketbus2025.ui.auth

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.UserEntity
import com.contoh.eticketbus2025.data.source.FirestoreHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = findViewById<TextView>(R.id.btnBackToLogin)

        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // Abaikan jika sudah diinit sebelumnya
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ID dikosongkan (""), nanti FirestoreHelper yang akan update ID-nya
            val newUser = UserEntity(
                id = "",
                fullName = name,
                email = email,
                phone = phone,
                password = password
            )

            performRegister(newUser)
        }

        btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegister(user: UserEntity) {
        Toast.makeText(this, "Mendaftarkan akun...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            // Panggil fungsi di Helper
            val isSuccess = FirestoreHelper.registerUser(user)

            withContext(Dispatchers.Main) {
                if (isSuccess) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registrasi Berhasil! Silakan Login",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    // Jika gagal, user disuruh cek Logcat atau koneksi
                    Toast.makeText(
                        this@RegisterActivity,
                        "Gagal Mendaftar. Cek koneksi internet.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("RegisterActivity", "Gagal mendaftar user: ${user.email}")
                }
            }
        }
    }
}