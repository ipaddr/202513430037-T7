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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.PromoModel
import com.contoh.eticketbus2025.data.source.FirestoreHelper // <-- MENGGUNAKAN FIREBASE
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

    private var cityList: List<String> = listOf()
    private var isRoundTrip = false
    private var ticketCount = 1
    private val indonesianLocale = Locale("id", "ID")
    private val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy", indonesianLocale)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 1. Inisialisasi Data Cloud (Cek & Upload jika kosong)
        initializeCloudData()

        // 2. Setup UI
        setupTripTypeToggle()
        setupCitySelection()
        setupDateSelection()
        setupPassengerCounter()
        setupSwapButton()
        setupSearchButton()
        setupBottomNav()

        findViewById<View>(R.id.btnNotification).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (bottomNav.selectedItemId != R.id.nav_home) {
            bottomNav.menu.findItem(R.id.nav_home).isChecked = true
        }

        // Refresh Data dari Cloud setiap kali halaman muncul
        loadDataFromCloud()

        // Notifikasi sementara di-skip karena belum ada di FirestoreHelper
        // checkUnreadNotifications()
    }

    // =========================================================================
    // LOGIKA FIREBASE / CLOUD
    // =========================================================================

    private fun initializeCloudData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Upload data dummy ke Firestore jika koleksi masih kosong
            FirestoreHelper.seedInitialData()

            // Setelah seeding selesai, muat data ke UI
            loadDataFromCloud()
        }
    }

    private fun loadDataFromCloud() {
        CoroutineScope(Dispatchers.IO).launch {
            // Ambil data secara parallel dari Firestore
            val cities = FirestoreHelper.getAllCities()
            val operators = FirestoreHelper.getUniqueOperators()
            val promos = FirestoreHelper.getAllPromos()

            // Update UI di Main Thread
            withContext(Dispatchers.Main) {
                // 1. Update Kota
                cityList = cities

                // 2. Update Operator RecyclerView
                if (operators.isNotEmpty()) {
                    val rvOperators = findViewById<RecyclerView>(R.id.rvOperators)
                    rvOperators.layoutManager = LinearLayoutManager(
                        this@MainActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    rvOperators.adapter = OperatorAdapter(operators)
                }

                // 3. Update Promo RecyclerView (Horizontal List)
                if (promos.isNotEmpty()) {
                    val rvPromos = findViewById<RecyclerView>(R.id.rvHomePromos)
                    rvPromos.layoutManager = LinearLayoutManager(
                        this@MainActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    rvPromos.adapter = HomePromoAdapter(promos) {
                        startActivity(Intent(this@MainActivity, PromoActivity::class.java))
                    }
                }
            }
        }
    }

    // =========================================================================
    // LOGIKA UI (Tidak Berubah)
    // =========================================================================

    private fun setupCitySelection() {
        val etOrigin = findViewById<EditText>(R.id.etOrigin)
        val etDestination = findViewById<EditText>(R.id.etDestination)

        etOrigin.setOnClickListener {
            if (cityList.isNotEmpty()) showCityBottomSheet("Pilih Kota Keberangkatan") {
                etOrigin.setText(
                    it
                )
            }
            else {
                Toast.makeText(this, "Sedang memuat data dari server...", Toast.LENGTH_SHORT).show()
                loadDataFromCloud() // Coba load lagi
            }
        }

        etDestination.setOnClickListener {
            if (cityList.isNotEmpty()) showCityBottomSheet("Pilih Kota Tujuan") {
                etDestination.setText(
                    it
                )
            }
            else {
                Toast.makeText(this, "Sedang memuat data dari server...", Toast.LENGTH_SHORT).show()
                loadDataFromCloud()
            }
        }
    }

    private fun showCityBottomSheet(title: String, onCitySelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_city, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvSheetTitle).text = title
        val etSearch = view.findViewById<EditText>(R.id.etSearchCity)
        val rvCities = view.findViewById<RecyclerView>(R.id.rvCities)

        val adapter = CityAdapter(cityList) { city -> onCitySelected(city); dialog.dismiss() }
        rvCities.layoutManager = LinearLayoutManager(this)
        rvCities.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
        })
        dialog.show()
    }

    private fun setupDateSelection() {
        val etDateDeparture = findViewById<TextView>(R.id.etDateDeparture)
        val etDateReturn = findViewById<TextView>(R.id.etDateReturn)
        etDateDeparture.setOnClickListener {
            showDateBottomSheet("Tanggal Keberangkatan") { date ->
                etDateDeparture.text = date
            }
        }
        etDateReturn.setOnClickListener {
            showDateBottomSheet("Tanggal Kepulangan") { date ->
                etDateReturn.text = date
            }
        }
    }

    private fun showDateBottomSheet(title: String, onDateSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_date, null)
        dialog.setContentView(view)
        view.findViewById<TextView>(R.id.tvDateTitle).text = title
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val btnSelect = view.findViewById<Button>(R.id.btnSelectDate)
        calendarView.minDate = System.currentTimeMillis()
        var selectedDateString = dateFormatter.format(Date())
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance(); calendar.set(
            year,
            month,
            dayOfMonth
        ); selectedDateString = dateFormatter.format(calendar.time)
        }
        btnSelect.setOnClickListener { onDateSelected(selectedDateString); dialog.dismiss() }
        dialog.show()
    }

    private fun setupSwapButton() {
        val btnSwap = findViewById<ImageButton>(R.id.btnSwap)
        val etOrigin = findViewById<EditText>(R.id.etOrigin)
        val etDestination = findViewById<EditText>(R.id.etDestination)
        btnSwap.setOnClickListener {
            val temp = etOrigin.text.toString(); etOrigin.setText(
            etDestination.text.toString()
        ); etDestination.setText(temp); btnSwap.animate().rotationBy(180f).setDuration(300).start()
        }
    }

    private fun setupPassengerCounter() {
        val btnMinus = findViewById<ImageButton>(R.id.btnMinus)
        val btnPlus = findViewById<ImageButton>(R.id.btnPlus)
        val tvCount = findViewById<TextView>(R.id.tvPassengerCount)
        btnMinus.setOnClickListener {
            if (ticketCount > 1) {
                ticketCount--; tvCount.text = "$ticketCount Tiket"
            }
        }
        btnPlus.setOnClickListener {
            if (ticketCount < 10) {
                ticketCount++; tvCount.text = "$ticketCount Tiket"
            }
        }
    }

    private fun setupSearchButton() {
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            val origin = findViewById<EditText>(R.id.etOrigin).text.toString()
            val dest = findViewById<EditText>(R.id.etDestination).text.toString()
            val dateDepart = findViewById<TextView>(R.id.etDateDeparture).text.toString()
            val dateReturn = findViewById<TextView>(R.id.etDateReturn).text.toString()
            val passengers = findViewById<TextView>(R.id.tvPassengerCount).text.toString()

            if (origin.isEmpty() || dest.isEmpty() || dateDepart.contains("Pilih")) {
                Toast.makeText(this, "Mohon lengkapi data perjalanan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isRoundTrip && dateReturn.contains("Pilih")) {
                Toast.makeText(this, "Mohon pilih tanggal pulang", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, BusListActivity::class.java)
            intent.putExtra("ORIGIN", origin)
            intent.putExtra("DESTINATION", dest)
            intent.putExtra("DATE", dateDepart)
            intent.putExtra("DATE_RETURN", dateReturn)
            intent.putExtra("PASSENGERS", passengers)
            intent.putExtra("IS_ROUND_TRIP", isRoundTrip)
            startActivity(intent)
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_myticket -> {
                    startActivity(
                        Intent(
                            this,
                            MyTicketActivity::class.java
                        )
                    ); overridePendingTransition(0, 0); true
                }

                R.id.nav_promo -> {
                    startActivity(
                        Intent(
                            this,
                            PromoActivity::class.java
                        )
                    ); overridePendingTransition(0, 0); true
                }

                R.id.nav_account -> {
                    startActivity(
                        Intent(
                            this,
                            ProfileActivity::class.java
                        )
                    ); overridePendingTransition(0, 0); true
                }

                else -> false
            }
        }
    }

    private fun setupTripTypeToggle() {
        val tabOneWay = findViewById<TextView>(R.id.tabOneWay)
        val tabRoundTrip = findViewById<TextView>(R.id.tabRoundTrip)
        val containerReturn = findViewById<LinearLayout>(R.id.containerReturnDate)
        val colorWhite = ContextCompat.getColor(this, R.color.white)
        val colorWhiteDim = ContextCompat.getColor(this, R.color.white_dim)
        fun updateTabs(isRound: Boolean) {
            if (isRound) {
                tabRoundTrip.setBackgroundResource(R.drawable.bg_tab_active); tabRoundTrip.setTextColor(
                    colorWhite
                )
                tabOneWay.setBackgroundResource(R.drawable.bg_tab_inactive); tabOneWay.setTextColor(
                    colorWhiteDim
                )
                containerReturn.visibility = View.VISIBLE; containerReturn.alpha =
                    0f; containerReturn.animate().alpha(1f).setDuration(300).start()
            } else {
                tabOneWay.setBackgroundResource(R.drawable.bg_tab_active); tabOneWay.setTextColor(
                    colorWhite
                )
                tabRoundTrip.setBackgroundResource(R.drawable.bg_tab_inactive); tabRoundTrip.setTextColor(
                    colorWhiteDim
                )
                containerReturn.visibility = View.GONE
            }
        }
        tabOneWay.setOnClickListener { isRoundTrip = false; updateTabs(false) }
        tabRoundTrip.setOnClickListener { isRoundTrip = true; updateTabs(true) }
    }

    // =========================================================================
    // ADAPTERS
    // =========================================================================

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

            // 1. Siapkan Palette Warna yang bagus (Pastikan warnanya agak gelap biar tulisan putih terbaca)
            val colors = listOf(
                0xFFE53935.toInt(), // Merah
                0xFFD81B60.toInt(), // Pink Tua
                0xFF8E24AA.toInt(), // Ungu
                0xFF5E35B1.toInt(), // Ungu Deep
                0xFF3949AB.toInt(), // Indigo
                0xFF1E88E5.toInt(), // Biru
                0xFF039BE5.toInt(), // Biru Muda
                0xFF00897B.toInt(), // Teal
                0xFF43A047.toInt(), // Hijau
                0xFF7CB342.toInt(), // Hijau Muda
                0xFFFB8C00.toInt(), // Orange
                0xFFF4511E.toInt(), // Deep Orange
                0xFF6D4C41.toInt(), // Coklat
                0xFF546E7A.toInt()  // Blue Grey
            )

            // 2. LOGIKA KONSISTENSI (Deterministic)
            // opName.hashCode() mengubah string "NPM" menjadi angka unik (misal: 76403)
            // Modulo (%) memastikan angkanya tidak melebihi jumlah warna di list.
            val index = kotlin.math.abs(opName.hashCode()) % colors.size

            // Hasilnya: "NPM" akan SELALU menunjuk ke index warna yang sama.
            val consistentColor = colors[index]

            // 3. Pasang warna
            holder.tvName.background.setTint(consistentColor)

            // (Opsional) Listener klik jika ada
            // holder.itemView.setOnClickListener { ... }
        }

        override fun getItemCount() = operators.size
    }

    class HomePromoAdapter(
        private val promos: List<PromoModel>,
        private val onClick: (PromoModel) -> Unit
    ) : RecyclerView.Adapter<HomePromoAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDiscount: TextView = view.findViewById(R.id.tvPromoDiscount)
            val tvTitle: TextView = view.findViewById(R.id.tvPromoTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_promo_home, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val promo = promos[position]
            holder.tvDiscount.text = "Diskon ${promo.discount}"
            holder.tvTitle.text = promo.title
            holder.itemView.setOnClickListener { onClick(promo) }
        }

        override fun getItemCount() = promos.size
    }
}