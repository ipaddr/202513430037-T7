package com.contoh.eticketbus2025.data.source

import android.util.Log
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.BusModel
import com.contoh.eticketbus2025.data.model.CityEntity
import com.contoh.eticketbus2025.data.model.PromoModel
import com.contoh.eticketbus2025.data.model.TicketHistoryModel
import com.contoh.eticketbus2025.data.model.UserEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object FirestoreHelper {

    // --- PERBAIKAN INISIALISASI (LAZY & SAFE) ---
    private val db: FirebaseFirestore
        get() {
            return try {
                FirebaseFirestore.getInstance()
            } catch (e: IllegalStateException) {
                // Jika error "not initialized", kita coba init manual
                // Catatan: Ini cara darurat. Idealnya init di Application.
                // Tapi karena 'context' tidak tersedia di Object, kita biarkan crash
                // namun pesannya akan lebih jelas atau bisa ditangani di Activity.
                throw e
            }
        }

    // --- 1. SEEDING DATA ---
    suspend fun seedInitialData() {
        try {
            val check = db.collection("cities").limit(1).get().await()
            if (check.isEmpty) {
                uploadCities()
                uploadBuses()
                uploadPromos()
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error seeding", e)
        }
    }

    private suspend fun uploadCities() {
        val cities = listOf(
            // --- KOTA DI SUMATERA ---
            CityEntity("", "Medan (Sumatera Utara)"),
            CityEntity("", "Banda Aceh (Aceh)"),
            CityEntity("", "Padang (Sumatera Barat)"),
            CityEntity("", "Pekanbaru (Riau)"),
            CityEntity("", "Jambi (Jambi)"),
            CityEntity("", "Palembang (Sumatera Selatan)"),
            CityEntity("", "Bengkulu (Bengkulu)"),
            CityEntity("", "Bandar Lampung (Lampung)"),
            CityEntity("", "Pangkal Pinang (Bangka Belitung)"),
            CityEntity("", "Tanjungbalai (Sumatera Utara)"),
            CityEntity("", "Lubuklinggau (Sumatera Selatan)"),
            CityEntity("", "Lhokseumawe (Aceh)"),
            CityEntity("", "Sibolga (Sumatera Utara)"),
            CityEntity("", "Bukittinggi (Sumatera Barat)"),
            CityEntity("", "Pematangsiantar (Sumatera Utara)"),
            CityEntity("", "Tebing Tinggi (Sumatera Utara)"),
            CityEntity("", "Solok (Sumatera Barat)"),
            CityEntity("", "Pariaman (Sumatera Barat)"),
            CityEntity("", "Prabumulih (Sumatera Selatan)"),
            CityEntity("", "Dumai (Riau)"),
            CityEntity("", "Tanjung Pinang (Kepulauan Riau)"),
            CityEntity("", "Gunungsitoli (Sumatera Utara)"),

            // --- KOTA DI JAWA ---
            CityEntity("", "Jakarta (DKI Jakarta)"),
            CityEntity("", "Bandung (Jawa Barat)"),
            CityEntity("", "Bogor (Jawa Barat)"),
            CityEntity("", "Bekasi (Jawa Barat)"),
            CityEntity("", "Depok (Jawa Barat)"),
            CityEntity("", "Semarang (Jawa Tengah)"),
            CityEntity("", "Surakarta (Jawa Tengah)"),
            CityEntity("", "Yogyakarta (DI Yogyakarta)"),
            CityEntity("", "Surabaya (Jawa Timur)"),
            CityEntity("", "Malang (Jawa Timur)"),
            CityEntity("", "Kediri (Jawa Timur)"),
            CityEntity("", "Madiun (Jawa Timur)"),
            CityEntity("", "Magelang (Jawa Tengah)"),
            CityEntity("", "Salatiga (Jawa Tengah)"),
            CityEntity("", "Cirebon (Jawa Barat)"),
            CityEntity("", "Tasikmalaya (Jawa Barat)"),
            CityEntity("", "Banjar (Jawa Barat)"),
            CityEntity("", "Cimahi (Jawa Barat)"),
            CityEntity("", "Sukabumi (Jawa Barat)"),
            CityEntity("", "Probolinggo (Jawa Timur)"),
            CityEntity("", "Mojokerto (Jawa Timur)"),
            CityEntity("", "Batu (Jawa Timur)"),
            CityEntity("", "Blitar (Jawa Timur)"),
            CityEntity("", "Pasuruan (Jawa Timur)"),

            )

        cities.forEach {
            try {
                db.collection("cities").add(it).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Log.d("Firestore", "Berhasil upload ${cities.size} kota.")
    }

    private suspend fun uploadPromos() {
        val promos = listOf(
            PromoModel(
                "",
                "Diskon Pengguna Baru",
                "Khusus pembelian pertama",
                "NEWUSER30",
                "30%",
                "31 Des 2025",
                "Rp 100.000",
                R.drawable.ic_notifications
            ),
            PromoModel(
                "",
                "Weekend Hemat",
                "Diskon perjalanan akhir pekan",
                "WEEKEND20",
                "20%",
                "30 Nov 2025",
                "Rp 150.000",
                R.drawable.ic_notifications
            )
        )
        promos.forEach { db.collection("promos").add(it) }
    }

    private suspend fun uploadBuses() {
        val buses = listOf(
            // =================================================================
            // 1. RUTE SUMATERA BARAT (PADANG/BUKITTINGGI) <-> PULAU JAWA
            // =================================================================
            BusModel("", "NPM", "Sutan Class", 475000.0, "09:00", "15:00", "30 Jam", 18, 4.8, listOf("Leg Rest", "Snack", "Toilet", "Smoking Area"), "Padang (Sumatera Barat)", "Jakarta (DKI Jakarta)"),
            BusModel("", "NPM", "Executive", 375000.0, "10:00", "17:00", "31 Jam", 30, 4.6, listOf("AC", "Toilet", "Selimut"), "Padang (Sumatera Barat)", "Bandung (Jawa Barat)"),
            BusModel("", "ANS", "Royal Class", 550000.0, "09:30", "14:30", "29 Jam", 22, 4.7, listOf("Leg Rest", "Wifi", "Bantal", "Makan"), "Padang (Sumatera Barat)", "Jakarta (DKI Jakarta)"),
            BusModel("", "ANS", "Super Executive", 450000.0, "11:00", "18:00", "31 Jam", 28, 4.5, listOf("AC", "Toilet", "Charging Point"), "Bukittinggi (Sumatera Barat)", "Bandung (Jawa Barat)"),
            BusModel("", "MPM", "Premium Class", 425000.0, "10:00", "16:00", "30 Jam", 24, 4.6, listOf("Air Suspension", "Snack", "Makan"), "Padang (Sumatera Barat)", "Jakarta (DKI Jakarta)"),
            BusModel("", "Gumarang Jaya", "Maestro", 350000.0, "13:00", "20:00", "31 Jam", 32, 4.4, listOf("AC", "Musik", "Toilet"), "Bukittinggi (Sumatera Barat)", "Jakarta (DKI Jakarta)"),
            BusModel("", "Transport Express", "Kudo Gadang", 375000.0, "11:30", "18:30", "31 Jam", 28, 4.5, listOf("AC", "Toilet", "Selimut"), "Pariaman (Sumatera Barat)", "Bogor (Jawa Barat)"),
            BusModel("", "Pangeran", "Luxury", 650000.0, "08:30", "12:30", "28 Jam", 12, 4.9, listOf("Leg Rest Jumbo", "AVOD", "Makan Prasmanan"), "Bukittinggi (Sumatera Barat)", "Jakarta (DKI Jakarta)"),
            BusModel("", "Sembodo", "Suite Combi", 750000.0, "09:00", "11:00", "26 Jam", 12, 4.9, listOf("Sleeper Seat", "TV Personal", "Makan"), "Padang (Sumatera Barat)", "Jakarta (DKI Jakarta)"),

            // =================================================================
            // 2. RUTE UTARA (ACEH/MEDAN/PEKANBARU) <-> PULAU JAWA
            // =================================================================
            BusModel("", "Sempati Star", "Double Decker", 850000.0, "14:00", "08:00", "42 Jam", 6, 4.9, listOf("Sleeper", "Private Cabin", "Minibar"), "Medan (Sumatera Utara)", "Jakarta (DKI Jakarta)"),
            BusModel("", "Sempati Star", "Super VIP", 600000.0, "15:00", "10:00", "43 Jam", 20, 4.7, listOf("Leg Rest", "Wifi"), "Medan (Sumatera Utara)", "Palembang (Sumatera Selatan)"),
            BusModel("", "ALS", "Super Executive", 550000.0, "13:00", "07:00", "48 Jam", 24, 4.6, listOf("Toilet", "Smoking Area", "AC"), "Medan (Sumatera Utara)", "Malang (Jawa Timur)"),
            BusModel("", "ALS", "Patas AC", 450000.0, "10:00", "05:00", "48 Jam", 35, 4.3, listOf("AC", "Toilet"), "Medan (Sumatera Utara)", "Solo (Jawa Tengah)"),
            BusModel("", "Putra Pelangi", "Perkasa", 700000.0, "10:00", "06:00", "50 Jam", 18, 4.7, listOf("Leg Rest", "Bantal", "Selimut"), "Banda Aceh (Aceh)", "Jakarta (DKI Jakarta)"),
            BusModel("", "Makmur", "VIP", 350000.0, "17:00", "05:00", "12 Jam", 28, 4.5, listOf("AC", "Toilet"), "Pekanbaru (Riau)", "Medan (Sumatera Utara)"),
            BusModel("", "SAN", "Executive", 500000.0, "10:00", "10:00", "24 Jam", 30, 4.6, listOf("Air Suspension", "USB Charger"), "Pekanbaru (Riau)", "Jakarta (DKI Jakarta)"),

            // =================================================================
            // 3. RUTE SELATAN (PALEMBANG/LAMPUNG/BENGKULU) <-> JAWA
            // =================================================================
            BusModel("", "EPA Star", "Shuttle", 350000.0, "19:00", "06:00", "11 Jam", 10, 4.7, listOf("Captain Seat", "Snack"), "Palembang (Sumatera Selatan)", "Bandung (Jawa Barat)"),
            BusModel("", "Kramat Djati", "VIP", 280000.0, "16:00", "04:00", "12 Jam", 32, 4.4, listOf("AC", "Toilet"), "Palembang (Sumatera Selatan)", "Jakarta (DKI Jakarta)"),
            BusModel("", "Puspa Jaya", "Executive", 320000.0, "17:00", "08:00", "15 Jam", 28, 4.5, listOf("Leg Rest", "Makan"), "Bandar Lampung (Lampung)", "Yogyakarta (DI Yogyakarta)"),
            BusModel("", "SAN", "Premium", 480000.0, "12:00", "08:00", "20 Jam", 24, 4.7, listOf("AC", "Toilet", "Bantal"), "Bengkulu (Bengkulu)", "Jakarta (DKI Jakarta)"),
            BusModel("", "Damri", "Royal Class", 350000.0, "20:00", "05:00", "9 Jam", 22, 4.6, listOf("Leg Rest", "Wifi"), "Bandar Lampung (Lampung)", "Jakarta (DKI Jakarta)"),

            // =================================================================
            // 4. TRANS JAWA (JAKARTA/JABODETABEK <-> JATENG/JATIM)
            // =================================================================
            BusModel("", "Sinar Jaya", "Suite Class", 450000.0, "17:30", "04:30", "11 Jam", 20, 4.9, listOf("Sleeper Seat", "AVOD", "Selimut"), "Jakarta (DKI Jakarta)", "Surabaya (Jawa Timur)"),
            BusModel("", "Sinar Jaya", "Executive", 210000.0, "18:00", "02:00", "8 Jam", 32, 4.6, listOf("AC", "Leg Rest"), "Bekasi (Jawa Barat)", "Semarang (Jawa Tengah)"),
            BusModel("", "Rosalia Indah", "First Class", 550000.0, "16:00", "01:00", "9 Jam", 2, 5.0, listOf("Double Decker", "Pramugari", "Private Room"), "Jakarta (DKI Jakarta)", "Surakarta (Jawa Tengah)"),
            BusModel("", "Rosalia Indah", "Super Top", 350000.0, "15:00", "23:00", "8 Jam", 20, 4.8, listOf("Leg Rest Jumbo", "Makan"), "Bogor (Jawa Barat)", "Yogyakarta (DI Yogyakarta)"),
            BusModel("", "Harapan Jaya", "Sleeper", 520000.0, "14:00", "03:00", "13 Jam", 20, 4.9, listOf("Sleeper", "Massage Chair", "Coffee Maker"), "Jakarta (DKI Jakarta)", "Blitar (Jawa Timur)"),
            BusModel("", "Gunung Harta", "Green Platinum", 420000.0, "13:00", "04:00", "15 Jam", 28, 4.8, listOf("Air Suspension", "Snack Berat"), "Jakarta (DKI Jakarta)", "Malang (Jawa Timur)"),
            BusModel("", "Juragan 99", "Sultan Class", 600000.0, "15:00", "05:00", "14 Jam", 15, 5.0, listOf("Private Cabin", "Earphone", "Makan Mewah"), "Jakarta (DKI Jakarta)", "Malang (Jawa Timur)"),
            BusModel("", "PO Haryanto", "Executive", 280000.0, "18:30", "04:30", "10 Jam", 30, 4.7, listOf("Audio System", "Selimut", "Toilet"), "Jakarta (DKI Jakarta)", "Kudus (Jawa Tengah)"), // Kudus belum ada di list city, ganti ke Semarang/Solo
            BusModel("", "Agra Mas", "Big Top", 300000.0, "17:00", "04:00", "11 Jam", 20, 4.6, listOf("Leg Rest", "Snack"), "Bogor (Jawa Barat)", "Solo (Jawa Tengah)"),
            BusModel("", "27 Trans", "President Class", 480000.0, "14:00", "05:00", "15 Jam", 18, 4.8, listOf("Air Suspension", "Leg Rest", "Makan"), "Malang (Jawa Timur)", "Bandung (Jawa Barat)"),

            // =================================================================
            // 5. RUTE PENDEK & LAINNYA
            // =================================================================
            BusModel("", "Primajasa", "Executive", 120000.0, "06:00", "09:00", "3 Jam", 40, 4.5, listOf("AC", "Smoking Area"), "Bandung (Jawa Barat)", "Jakarta (DKI Jakarta)"),
            BusModel("", "CitiTrans", "Shuttle", 160000.0, "05:00", "08:00", "3 Jam", 8, 4.8, listOf("Captain Seat", "Point to Point"), "Bandung (Jawa Barat)", "Jakarta (DKI Jakarta)"),
            BusModel("", "Budiman", "First Class", 150000.0, "07:00", "14:00", "7 Jam", 30, 4.6, listOf("AC", "Wifi"), "Tasikmalaya (Jawa Barat)", "Jakarta (DKI Jakarta)"),
            BusModel("", "Sugeng Rahayu", "Patas", 120000.0, "08:00", "13:00", "5 Jam", 40, 4.3, listOf("AC", "Musik"), "Surabaya (Jawa Timur)", "Yogyakarta (DI Yogyakarta)"),
            BusModel("", "Eka", "Cepat", 130000.0, "09:00", "15:00", "6 Jam", 40, 4.4, listOf("AC", "Makan"), "Surabaya (Jawa Timur)", "Magelang (Jawa Tengah)"),
            BusModel("", "Damri", "Pioneer", 300000.0, "08:00", "18:00", "10 Jam", 25, 4.5, listOf("AC", "Logistik"), "Pontianak (Kalimantan Barat)", "Singkawang (Kalimantan Barat)") // Asumsi rute kalimantan
        )

        buses.forEach {
            try {
                db.collection("buses").add(it).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Log.d("Firestore", "Berhasil upload ${buses.size} bus baru.")
    }

    // --- 2. GET DATA ---

    suspend fun getAllCities(): List<String> {
        return try {
            val snapshot = db.collection("cities").get().await()
            snapshot.toObjects(CityEntity::class.java).map { it.name }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllPromos(): List<PromoModel> {
        return try {
            val snapshot = db.collection("promos").get().await()
            snapshot.toObjects(PromoModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUniqueOperators(): List<String> {
        return try {
            val snapshot = db.collection("buses").get().await()
            val buses = snapshot.toObjects(BusModel::class.java)
            buses.map { it.operatorName }.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchBuses(origin: String, dest: String): List<BusModel> {
        return try {
            val snapshot = db.collection("buses")
                .whereEqualTo("origin", origin)
                .whereEqualTo("destination", dest)
                .get().await()
            snapshot.toObjects(BusModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllBuses(): List<BusModel> {
        return try {
            val snapshot = db.collection("buses").get().await()
            snapshot.toObjects(BusModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- 3. AUTH (LOGIN & REGISTER) ---

    suspend fun loginUser(email: String, pass: String): UserEntity? {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", pass)
                .limit(1).get().await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                val user = doc.toObject(UserEntity::class.java)
                user?.id = doc.id
                user
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun registerUser(user: UserEntity): Boolean {
        return try {
            // Cek Duplikasi Email
            val check = db.collection("users")
                .whereEqualTo("email", user.email)
                .get()
                .await()

            if (!check.isEmpty) {
                Log.e("Firestore", "Register Gagal: Email ${user.email} sudah ada.")
                return false
            }

            // Simpan User Baru
            val ref = db.collection("users").add(user).await()

            // Update ID di dalam dokumen
            db.collection("users").document(ref.id).update("id", ref.id).await()

            Log.d("Firestore", "Register Berhasil! ID: ${ref.id}")
            true
        } catch (e: Exception) {
            Log.e("Firestore", "Register Error: ${e.message}", e)
            false
        }
    }

    suspend fun getUserById(userId: String): UserEntity? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(UserEntity::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(user: UserEntity): Boolean {
        return try {
            if (user.id.isNotEmpty()) {
                db.collection("users").document(user.id).set(user).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 4. BOOKING & TICKET ---

    suspend fun saveTicket(ticket: TicketHistoryModel): Boolean {
        return try {
            db.collection("tickets").document(ticket.bookingId).set(ticket).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getMyTickets(userId: String): List<TicketHistoryModel> {
        return try {
            // HAPUS .orderBy DARI SINI AGAR TIDAK BUTUH INDEX FIREBASE
            val snapshot = db.collection("tickets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Urutkan manual menggunakan Kotlin (Lebih aman untuk development)
            val list = snapshot.toObjects(TicketHistoryModel::class.java)
            list.sortedByDescending { it.timestamp }

        } catch (e: Exception) {
            Log.e("Firestore", "Gagal ambil tiket: ${e.message}")
            emptyList()
        }
    }
}