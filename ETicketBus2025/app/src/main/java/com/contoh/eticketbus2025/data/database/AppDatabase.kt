package com.contoh.eticketbus2025.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.contoh.eticketbus2025.data.model.BookingEntity
import com.contoh.eticketbus2025.data.model.BusModel
import com.contoh.eticketbus2025.data.model.CityEntity // <-- PENTING: Import Ini
import com.contoh.eticketbus2025.data.model.NotificationEntity
import com.contoh.eticketbus2025.data.model.TicketHistoryModel
import com.contoh.eticketbus2025.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// UPDATE: Version naik jadi 4, tambah CityEntity
@Database(
    entities = [
        BusModel::class,
        TicketHistoryModel::class,
        UserEntity::class,
        BookingEntity::class,
        NotificationEntity::class,
        CityEntity::class // <-- Tambahkan Tabel Kota
    ],
    version = 4 // <-- Naikkan Versi
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eticket_db_v1"
                )
                    .fallbackToDestructiveMigration() // Reset DB jika versi berubah
                    .addCallback(DatabaseCallback())  // Isi data awal
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateData(database.appDao())
                    }
                }
            }

            suspend fun populateData(dao: AppDao) {
                // 1. DATA KOTA (SEEDING)
                val cities = listOf(
                    CityEntity(name = "Terminal Bingkuang (Padang)"),
                    CityEntity(name = "Terminal Aur Kuning (Bukittinggi)"),
                    CityEntity(name = "Terminal Bareh Solok (Solok)"),
                    CityEntity(name = "Terminal Koto Nan Ampek (Payakumbuh)"),
                    CityEntity(name = "Terminal Jati (Pariaman)"),
                    CityEntity(name = "Terminal Kiliran Jao (Sijunjung)"),
                    CityEntity(name = "Terminal Simpang Empat (Pasaman Barat)"),
                    CityEntity(name = "Terminal Piliang (Batusangkar)"),
                    CityEntity(name = "Pool NPM Padang"),
                    CityEntity(name = "Pool ANS Padang"),
                    CityEntity(name = "Pool MPM Padang"),
                    CityEntity(name = "Terminal Pulogebang (Jakarta)"),
                    CityEntity(name = "Terminal Kalideres (Jakarta)"),
                    CityEntity(name = "Terminal Bungurasih (Surabaya)"),
                    CityEntity(name = "Terminal Leuwi Panjang (Bandung)"),
                    CityEntity(name = "Terminal Amplas (Medan)"),
                    CityEntity(name = "Terminal AKAP (Pekanbaru)")
                )
                dao.insertAllCities(cities)

                // 2. DATA BUS
                val buses = listOf(
                    // PERGI
                    BusModel(0, "NPM", "Sutan Class", 450000.0, "08:00", "14:00", "6 Jam", 12, 4.8, listOf("AC", "Snack", "Toilet", "Wifi"), "Padang", "Jakarta"),
                    BusModel(0, "ANS", "Royal Class", 425000.0, "09:00", "15:30", "6.5 Jam", 8, 4.7, listOf("AC", "Wifi"), "Padang", "Jakarta"),
                    BusModel(0, "MPM", "Premium", 400000.0, "10:00", "17:00", "7 Jam", 20, 4.6, listOf("AC", "Toilet"), "Padang", "Jakarta"),
                    BusModel(0, "Sembodo", "Suite Combi", 600000.0, "09:30", "15:00", "5.5 Jam", 6, 4.9, listOf("Sleeper Seat", "AVOD", "Wifi", "Makan Prasmanan"), "Padang", "Jakarta"),
                    BusModel(0, "Palala", "Panorama Class", 500000.0, "08:30", "15:00", "6.5 Jam", 10, 4.8, listOf("AC", "Leg Rest Jumbo", "Coffee Maker", "Toilet"), "Padang", "Jakarta"),
                    BusModel(0, "Gumarang Jaya", "Executive", 350000.0, "13:00", "20:00", "7 Jam", 15, 4.5, listOf("AC", "Snack"), "Padang", "Jakarta"),

                    // PULANG
                    BusModel(0, "NPM", "Sutan Class", 450000.0, "19:00", "05:00", "10 Jam", 15, 4.8, listOf("AC", "Snack"), "Jakarta", "Padang"),
                    BusModel(0, "ANS", "Royal Class", 425000.0, "20:00", "06:30", "10.5 Jam", 10, 4.7, listOf("AC", "Wifi"), "Jakarta", "Padang"),
                    BusModel(0, "Sembodo", "Suite Combi", 600000.0, "10:00", "16:00", "6 Jam", 5, 4.9, listOf("Sleeper", "AVOD"), "Jakarta", "Padang")
                )
                dao.insertAllBuses(buses)

                // 3. NOTIFIKASI
                val notifs = listOf(
                    NotificationEntity(title = "Selamat Datang!", message = "Terima kasih telah menggunakan E-Ticket Bus Sumbar.", type = "INFO", date = "Baru Saja"),
                    NotificationEntity(title = "Diskon Pengguna Baru", message = "Gunakan kode NEWUSER30 untuk mendapatkan potongan 30%.", type = "PROMO", date = "1 Jam yang lalu")
                )
                dao.insertAllNotifications(notifs)
            }
        }
    }
}