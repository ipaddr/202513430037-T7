package com.contoh.eticketbus2025.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.source.FirestoreHelper // <-- Menggunakan Firestore
import com.contoh.eticketbus2025.utils.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {

    private lateinit var session: UserSession
    private var currentUserId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Pastikan layout ini sesuai dengan nama file XML Anda
        setContentView(R.layout.activity_edit_profile)

        session = UserSession(this)
        currentUserId = session.getUserId()

        // 1. Inisialisasi View sesuai ID di XML activity_edit_profile.xml
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // 2. Load Data Awal dari Firestore
        loadCurrentProfile(etName, etEmail, etPhone)

        // 3. Logic Tombol Simpan
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Nama dan Email wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveProfileChanges(newName, newEmail, newPhone)
        }

        // 4. Logic Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadCurrentProfile(etName: EditText, etEmail: EditText, etPhone: EditText) {
        CoroutineScope(Dispatchers.IO).launch {
            // Ambil data user dari Firestore berdasarkan ID sesi
            val user = FirestoreHelper.getUserById(currentUserId)

            withContext(Dispatchers.Main) {
                user?.let {
                    etName.setText(it.fullName)
                    etEmail.setText(it.email)
                    etPhone.setText(it.phone)
                }
            }
        }
    }

    private fun saveProfileChanges(name: String, email: String, phone: String) {
        Toast.makeText(this, "Menyimpan perubahan...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            // Ambil user lama dulu agar password/ID tidak hilang saat update
            val currentUser = FirestoreHelper.getUserById(currentUserId)

            if (currentUser != null) {
                // Buat object user baru dengan data yang diedit
                val updatedUser = currentUser.copy(
                    fullName = name,
                    email = email,
                    phone = phone
                )

                // Update ke Firestore
                val success = FirestoreHelper.updateUser(updatedUser)

                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Profil Berhasil Diperbarui",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Kembali ke halaman profil
                    } else {
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Gagal memperbarui profil",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}