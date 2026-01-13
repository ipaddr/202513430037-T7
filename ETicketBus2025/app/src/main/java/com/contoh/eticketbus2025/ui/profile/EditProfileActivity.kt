package com.contoh.eticketbus2025.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.database.AppDatabase
import com.contoh.eticketbus2025.data.model.UserEntity
import com.contoh.eticketbus2025.utils.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Kelas EditProfileActivity adalah sebuah activity yang memungkinkan pengguna untuk mengedit data profil mereka.
class EditProfileActivity : AppCompatActivity() {

    // Variabel untuk mengelola sesi pengguna, seperti mendapatkan ID pengguna yang sedang login.
    private lateinit var session: UserSession
    // Variabel untuk menyimpan data pengguna saat ini yang diambil dari database.
    private var currentUser: UserEntity? = null

    /**
     * Fungsi ini dipanggil saat activity pertama kali dibuat.
     * Ini digunakan untuk menginisialisasi UI, session, memuat data pengguna,
     * dan mengatur listener untuk tombol.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Inisialisasi UserSession.
        session = UserSession(this)
        // Memuat data pengguna dari database dan menampilkannya di UI.
        loadUserData()

        // Mengatur listener untuk tombol kembali (btnBack). Saat diklik, activity akan ditutup.
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        // Mengatur listener untuk tombol simpan (btnSave). Saat diklik, akan memanggil fungsi saveChanges().
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveChanges() }
    }

    /**
     * Fungsi untuk memuat data pengguna dari database berdasarkan ID pengguna dari session.
     * Operasi database dijalankan di background thread menggunakan Coroutine.
     * Setelah data didapat, UI di-update di main thread.
     */
    private fun loadUserData() {
        // Mendapatkan ID pengguna dari session.
        val userId = session.getUserId()
        // Menjalankan operasi database di thread IO untuk tidak memblokir UI.
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            currentUser = db.userDao().getUserById(userId)

            // Beralih ke main thread untuk memperbarui UI.
            withContext(Dispatchers.Main) {
                // Jika data pengguna ditemukan (tidak null).
                currentUser?.let {
                    // Mengisi EditText dengan data pengguna yang ada.
                    findViewById<EditText>(R.id.etName).setText(it.fullName)
                    findViewById<EditText>(R.id.etEmail).setText(it.email)
                    findViewById<EditText>(R.id.etPhone).setText(it.phone)
                }
            }
        }
    }

    /**
     * Fungsi untuk menyimpan perubahan data profil ke database.
     * Mengambil data baru dari EditText, melakukan validasi,
     * dan kemudian memperbarui data di database.
     */
    private fun saveChanges() {
        // Mengambil nilai teks terbaru dari setiap EditText.
        val name = findViewById<EditText>(R.id.etName).text.toString()
        val email = findViewById<EditText>(R.id.etEmail).text.toString()
        val phone = findViewById<EditText>(R.id.etPhone).text.toString()

        // Validasi: memastikan tidak ada field yang kosong.
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        // Memastikan currentUser tidak null sebelum melanjutkan.
        currentUser?.let { user ->
            // Update Object
            val updatedUser = user.copy(fullName = name, email = email, phone = phone)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(applicationContext)
                db.userDao().updateUser(updatedUser)

                // Memperbarui nama di session jika nama lengkap berubah.
                session.createLoginSession(user.id, name)

                // Beralih ke main thread untuk menampilkan pesan dan menutup activity.
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke halaman profil
                }
            }
        }
    }
}
