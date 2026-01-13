package com.contoh.eticketbus2025.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.database.AppDatabase
import com.contoh.eticketbus2025.data.model.BusModel
import com.contoh.eticketbus2025.data.model.CityEntity
import com.contoh.eticketbus2025.data.model.NotificationEntity
import com.contoh.eticketbus2025.ui.profile.ProfileActivity
import com.contoh.eticketbus2025.ui.search.BusListActivity
import com.contoh.eticketbus2025.ui.ticket.MyTicketActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    /**
     * Daftar kota yang akan ditampilkan di pilihan keberangkatan dan tujuan.
     * Data ini diambil dari database secara dinamis.
     */
    private var cityList: List<String> = listOf()

    // --- State Variables ---
    /** [isRoundTrip] adalah flag untuk menandai apakah pengguna memilih perjalanan "Pulang-Pergi" (true) atau "Sekali Jalan" (false). */
    private var isRoundTrip = false
    /** [ticketCount] menyimpan jumlah tiket yang dipilih oleh pengguna. */
    private var ticketCount = 1

    // --- Utility Variables ---
    /** [indonesianLocale] digunakan untuk memformat tanggal ke dalam Bahasa Indonesia. */
    private val indonesianLocale = Locale("id", "ID")
    /** [dateFormatter] adalah objek untuk memformat objek [Date] menjadi string dengan format "Hari, tanggal Bulan tahun". */
    private val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy", indonesianLocale)

    /**
     * Fungsi [onCreate] dipanggil saat Activity pertama kali dibuat.
     * Ini adalah tempat untuk melakukan inisialisasi awal.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Mengunci tema aplikasi ke mode terang (Light Mode) untuk konsistensi UI.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        // 2. Memeriksa database dan memuat data awal yang diperlukan.
        checkAndSeedData()
        loadCitiesFromDB()

        // 3. Menyiapkan semua komponen User Interface (UI) dan listener-nya.
        // Ini termasuk tombol, input field, navigasi, dll.
        setupTripTypeToggle()
        setupCitySelection()
        setupDateSelection()
        setupPassengerCounter()
        setupSwapButton()
        setupSearchButton()
        setupBottomNav()

        // Menambahkan listener klik untuk tombol notifikasi.
        // Ketika diklik, akan membuka NotificationActivity.
        findViewById<View>(R.id.btnNotification).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
    }

    /**
     * Fungsi [onResume] dipanggil setiap kali Activity kembali ke foreground (menjadi terlihat oleh pengguna).
     * Cocok untuk me-refresh data yang mungkin berubah saat pengguna berada di layar lain.
     */
    override fun onResume() {
        super.onResume()
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (bottomNav.selectedItemId != R.id.nav_home) {
            bottomNav.menu.findItem(R.id.nav_home).isChecked = true
        }
        // Memuat ulang data yang mungkin diperbarui, seperti notifikasi dan daftar operator.
        checkUnreadNotifications()
        loadOperatorsFromDB()
    }

    // =========================================================================
    // LOGIKA DATABASE
    // =========================================================================

    /**
     * [checkAndSeedData] memeriksa apakah database (khususnya tabel bus) kosong.
     * Jika kosong, fungsi ini akan memicu proses "seeding" (mengisi data awal)
     * yang didefinisikan dalam [AppDatabase.Callback]. Setelah itu, data yang relevan dimuat ulang.
     */
    private fun checkAndSeedData() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            if (db.appDao().getAllBuses().isEmpty()) {
                // Trigger callback seeding
                db.appDao().getAllBuses()
                kotlinx.coroutines.delay(500) // Memberi jeda singkat agar proses seeding selesai.

                withContext(Dispatchers.Main) { // Kembali ke thread utama untuk update UI.
                    loadOperatorsFromDB()
                    loadCitiesFromDB() // Refresh kota setelah seeding
                    checkUnreadNotifications()
                }
            }
        }
    }

    /**
     * [loadCitiesFromDB] mengambil daftar semua kota dari database secara asynchronous.
     * Setelah data didapat, hasilnya (nama kota) disimpan ke dalam variabel [cityList] di thread utama.
     */
    private fun loadCitiesFromDB() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val cities = db.appDao().getAllCities()
            withContext(Dispatchers.Main) {
                cityList = cities.map { it.name } // Mengubah list CityEntity menjadi list String.
            }
        }
    }

    /**
     * [loadOperatorsFromDB] mengambil daftar nama operator bus yang unik dari database.
     * Data ini kemudian ditampilkan dalam [RecyclerView] horizontal di bagian atas layar.
     */
    private fun loadOperatorsFromDB() {
        val rvOperators = findViewById<RecyclerView>(R.id.rvOperators)
        rvOperators.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val operators = try {
                db.appDao().getUniqueOperators()
            } catch (e: Exception) { // Fallback jika terjadi error.
                listOf("NPM", "ANS")
            }
            withContext(Dispatchers.Main) {
                if (operators.isNotEmpty()) rvOperators.adapter = OperatorAdapter(operators)
            }
        }
    }

    /**
     * [checkUnreadNotifications] menghitung jumlah notifikasi yang belum dibaca dari database.
     * Jika ada notifikasi yang belum dibaca, sebuah badge (lencana) dengan jumlahnya akan ditampilkan
     * di atas ikon notifikasi.
     */
    private fun checkUnreadNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val unreadCount = db.appDao().getUnreadNotificationCount()
                withContext(Dispatchers.Main) {
                    val badge = findViewById<TextView>(R.id.tvNotificationBadge)
                    if (unreadCount > 0) {
                        badge.visibility = View.VISIBLE
                        badge.text = if (unreadCount > 9) "9+" else unreadCount.toString()
                    } else {
                        badge.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // =========================================================================
    // LOGIKA UI
    // =========================================================================

    /**
     * [setupCitySelection] mengatur listener untuk input field kota asal ([etOrigin]) dan tujuan ([etDestination]).
     * Saat salah satu input field diklik, akan muncul bottom sheet ([showCityBottomSheet])
     * yang menampilkan daftar kota untuk dipilih.
     */
    private fun setupCitySelection() {
        val etOrigin = findViewById<EditText>(R.id.etOrigin)
        val etDestination = findViewById<EditText>(R.id.etDestination)

        etOrigin.setOnClickListener {
            if (cityList.isNotEmpty()) { // Cek apakah data kota sudah dimuat.
                showCityBottomSheet("Pilih Kota Keberangkatan") { etOrigin.setText(it) }
            } else { // Jika belum, tampilkan pesan dan coba muat ulang.
                Toast.makeText(this, "Memuat data kota...", Toast.LENGTH_SHORT).show()
                loadCitiesFromDB()
            }
        }

        etDestination.setOnClickListener {
            if (cityList.isNotEmpty()) { // Sama seperti etOrigin.
                showCityBottomSheet("Pilih Kota Tujuan") { etDestination.setText(it) }
            } else {
                Toast.makeText(this, "Memuat data kota...", Toast.LENGTH_SHORT).show()
                loadCitiesFromDB()
            }
        }
    }

    /**
     * [setupDateSelection] mengatur listener untuk input field tanggal keberangkatan dan tanggal pulang.
     * Saat diklik, akan memunculkan bottom sheet kalender ([showDateBottomSheet]) untuk memilih tanggal.
     */
    private fun setupDateSelection() {
        val etDateDeparture = findViewById<TextView>(R.id.etDateDeparture)
        val etDateReturn = findViewById<TextView>(R.id.etDateReturn)

        etDateDeparture.setOnClickListener {
            showDateBottomSheet("Tanggal Keberangkatan") {
                etDateDeparture.text = it // Mengupdate teks tanggal setelah dipilih.
            }
        }

        etDateReturn.setOnClickListener {
            showDateBottomSheet("Tanggal Kepulangan") {
                etDateReturn.text = it
            }
        }
    }

    /**
     * [showDateBottomSheet] menampilkan sebuah dialog dari bawah layar (BottomSheetDialog)
     * yang berisi [CalendarView] untuk pemilihan tanggal.
     * @param title Judul yang akan ditampilkan di atas kalender.
     * @param onDateSelected Callback yang akan dipanggil saat pengguna menekan tombol "Pilih", membawa tanggal yang dipilih dalam format string.
     */
    private fun showDateBottomSheet(title: String, onDateSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_date, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvDateTitle).text = title
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        calendarView.minDate = System.currentTimeMillis() // Tanggal masa lalu tidak bisa dipilih.
        var selectedDateString = dateFormatter.format(Date()) // Default tanggal terpilih adalah hari ini.

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance(); calendar.set(year, month, dayOfMonth)
            selectedDateString = dateFormatter.format(calendar.time) // Update tanggal terpilih saat pengguna mengubah kalender.
        }
        view.findViewById<Button>(R.id.btnSelectDate)
            .setOnClickListener { onDateSelected(selectedDateString); dialog.dismiss() } // Panggil callback dan tutup dialog.
        dialog.show()
    }

    private fun setupSwapButton() {
        val btnSwap = findViewById<ImageButton>(R.id.btnSwap)
        val etOrigin = findViewById<EditText>(R.id.etOrigin)
        val etDestination = findViewById<EditText>(R.id.etDestination)
        btnSwap.setOnClickListener {
            val temp =
                etOrigin.text.toString(); etOrigin.setText(etDestination.text.toString()); etDestination.setText(
            temp
        )
            btnSwap.animate().rotationBy(180f).setDuration(300).start()
        }
    }
    /**
     * [setupPassengerCounter] mengatur fungsi untuk tombol tambah (+) dan kurang (-)
     * dalam pemilihan jumlah tiket penumpang.
     */
    private fun setupPassengerCounter() {
        val btnMinus = findViewById<ImageButton>(R.id.btnMinus)
        val btnPlus = findViewById<ImageButton>(R.id.btnPlus)
        val tvCount = findViewById<TextView>(R.id.tvPassengerCount)

        btnMinus.setOnClickListener {
            if (ticketCount > 1) { // Jumlah tiket minimal adalah 1.
                ticketCount--; tvCount.text = "$ticketCount Tiket"
            }
        }
        btnPlus.setOnClickListener {
            if (ticketCount < 10) { // Jumlah tiket maksimal adalah 10.
                ticketCount++; tvCount.text = "$ticketCount Tiket"
            }
        }
    }

    /**
     * [setupSearchButton] menyiapkan tombol "Cari Tiket".
     * Tombol ini akan mengumpulkan semua data input (asal, tujuan, tanggal, dll.), melakukan validasi dasar,
     * lalu mengirim data tersebut ke [BusListActivity] untuk menampilkan hasil pencarian.
     */
    private fun setupSearchButton() {
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            val origin = findViewById<EditText>(R.id.etOrigin).text.toString()
            val dest = findViewById<EditText>(R.id.etDestination).text.toString()
            val dateDepart = findViewById<TextView>(R.id.etDateDeparture).text.toString()
            val dateReturn = findViewById<TextView>(R.id.etDateReturn).text.toString()
            val passengers = findViewById<TextView>(R.id.tvPassengerCount).text.toString()

            // Validasi input: memastikan field yang wajib diisi tidak kosong.
            if (origin.isEmpty() || dest.isEmpty() || dateDepart.contains("Pilih")) {
                Toast.makeText(this, "Mohon lengkapi data perjalanan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isRoundTrip && dateReturn.contains("Pilih")) { // Jika pulang-pergi, tanggal pulang wajib diisi.
                Toast.makeText(this, "Mohon pilih tanggal pulang", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Membuat Intent untuk memulai BusListActivity dan menyertakan data pencarian.
            val intent = Intent(this, BusListActivity::class.java)
            intent.putExtra("ORIGIN", origin); intent.putExtra("DESTINATION", dest)
            intent.putExtra("DATE", dateDepart); intent.putExtra("DATE_RETURN", dateReturn)
            intent.putExtra("PASSENGERS", passengers); intent.putExtra("IS_ROUND_TRIP", isRoundTrip)
            startActivity(intent)
        }
    }

    /**
     * [showCityBottomSheet] menampilkan dialog dari bawah layar yang berisi daftar kota
     * beserta field pencarian untuk memfilter daftar tersebut.
     * @param title Judul yang akan ditampilkan di atas daftar kota.
     * @param onCitySelected Callback yang dipanggil saat sebuah kota dipilih dari daftar, membawa nama kota yang dipilih.
     */
    private fun showCityBottomSheet(title: String, onCitySelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_city, null)
        dialog.setContentView(view)
        view.findViewById<TextView>(R.id.tvSheetTitle).text = title
        val etSearch = view.findViewById<EditText>(R.id.etSearchCity)
        val rvCities = view.findViewById<RecyclerView>(R.id.rvCities)

        // Menggunakan cityList yang sudah dimuat dari DB sebagai sumber data untuk adapter.
        val adapter = CityAdapter(cityList) { city -> onCitySelected(city); dialog.dismiss() }
        rvCities.layoutManager = LinearLayoutManager(this); rvCities.adapter = adapter

        // Menambahkan listener untuk memfilter daftar kota saat pengguna mengetik di field pencarian.
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString()) // Memanggil fungsi filter di adapter.
            }
        })
        dialog.show()
    }

    /**
     * [setupBottomNav] mengatur logika untuk navigasi bawah (BottomNavigationView).
     * Fungsi ini menangani perpindahan antar Activity (Home, MyTicket, Promo, Account) saat item navigasi diklik.
     */
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_myticket -> {
                    startActivity(Intent(this, MyTicketActivity::class.java))
                    overridePendingTransition(0, 0) // Menghilangkan animasi transisi antar activity.
                    true
                }

                R.id.nav_promo -> {
                    startActivity(Intent(this, PromoActivity::class.java))
                    overridePendingTransition(0, 0) // Menghilangkan animasi transisi antar activity.
                    true
                }

                R.id.nav_account -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0) // Menghilangkan animasi transisi antar activity.
                    true
                }

                else -> false
            }
        }
    }

    /**
     * [setupTripTypeToggle] mengatur logika untuk tombol "Sekali Jalan" dan "Pulang-Pergi".
     * Fungsi ini mengubah tampilan tombol (tab) dan menampilkan/menyembunyikan field input
     * tanggal pulang sesuai dengan pilihan pengguna.
     */
    private fun setupTripTypeToggle() {
        val tabOneWay = findViewById<TextView>(R.id.tabOneWay)
        val tabRoundTrip = findViewById<TextView>(R.id.tabRoundTrip)
        val containerReturn = findViewById<LinearLayout>(R.id.containerReturnDate)
        val colorWhite = ContextCompat.getColor(this, R.color.white)
        val colorWhiteDim = ContextCompat.getColor(this, R.color.white_dim)

        // Fungsi helper untuk memperbarui UI tab berdasarkan pilihan.
        fun updateTabs(isRound: Boolean) {
            if (isRound) {
                tabRoundTrip.setBackgroundResource(R.drawable.bg_tab_active)
                tabRoundTrip.setTextColor(colorWhite)
                tabOneWay.setBackgroundResource(R.drawable.bg_tab_inactive)
                tabOneWay.setTextColor(colorWhiteDim)
                containerReturn.visibility = View.VISIBLE
                containerReturn.alpha = 0f // Animasi fade-in.
                containerReturn.animate().alpha(1f).setDuration(300).start()
            } else {
                tabOneWay.setBackgroundResource(R.drawable.bg_tab_active)
                tabOneWay.setTextColor(colorWhite)
                tabRoundTrip.setBackgroundResource(R.drawable.bg_tab_inactive)
                tabRoundTrip.setTextColor(colorWhiteDim)
                containerReturn.visibility = View.GONE
            }
        }
        // Menetapkan listener klik untuk setiap tab.
        tabOneWay.setOnClickListener { isRoundTrip = false; updateTabs(false) }
        tabRoundTrip.setOnClickListener { isRoundTrip = true; updateTabs(true) }
    }

    /**
     * [CityAdapter] adalah adapter untuk [RecyclerView] yang menampilkan daftar kota.
     * Adapter ini juga memiliki fungsi [filter] untuk menyaring daftar berdasarkan input pencarian.
     * @param originalList Daftar lengkap semua kota.
     * @param onClick Callback yang akan dieksekusi ketika sebuah item kota diklik.
     */
    // --- ADAPTER CLASSES ---
    class CityAdapter(
        private val originalList: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {
        private var filteredList = originalList.toMutableList()

        inner class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvCityName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
            return CityViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
            )
        }

        override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
            holder.tvName.text =
                filteredList[position]; holder.itemView.setOnClickListener { onClick(filteredList[position]) }
        }

        override fun getItemCount() = filteredList.size

        /**
         * [filter] memperbarui [filteredList] berdasarkan query pencarian.
         * Jika query kosong, tampilkan semua kota. Jika tidak, tampilkan kota yang namanya mengandung query.
         */
        fun filter(query: String) {
            filteredList =
                if (query.isEmpty()) originalList.toMutableList() else originalList.filter {
                    it.contains(
                        query,
                        ignoreCase = true
                    )
                }.toMutableList(); notifyDataSetChanged()
        }
    }

    /**
     * [OperatorAdapter] adalah adapter untuk [RecyclerView] yang menampilkan daftar operator bus
     * di halaman utama.
     * @param operators Daftar nama-nama operator bus.
     */
    class OperatorAdapter(private val operators: List<String>) :
        RecyclerView.Adapter<OperatorAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvOperatorName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_operator_home, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val opName = operators[position]
            holder.tvName.text = opName
            val context = holder.itemView.context
            val bgTint = when (opName.uppercase()) {
                // Mengatur warna latar belakang yang berbeda untuk setiap nama operator.
                "NPM" -> 0xFF2563EB.toInt()
                "ANS" -> 0xFFDC2626.toInt()
                "MPM" -> 0xFF16A34A.toInt()
                "ALS" -> 0xFFCA8A04.toInt()
                else -> 0xFF1E293B.toInt()
            }
            holder.tvName.background.setTint(bgTint)
        }

        override fun getItemCount() = operators.size
    }
}