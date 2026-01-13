package com.contoh.eticketbus2025.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.database.AppDatabase
import com.contoh.eticketbus2025.data.model.BusModel
import com.contoh.eticketbus2025.ui.booking.SeatSelectionActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BusListActivity : AppCompatActivity() {

    // --- VARIABEL UNTUK MENGELOLA STATE HALAMAN ---
    // Menyimpan status apakah perjalanan pulang-pergi atau tidak.
    private var isRoundTrip = false
    // Menyimpan data bus keberangkatan yang telah dipilih oleh pengguna.
    // Jika nilainya bukan null, berarti pengguna sedang dalam tahap memilih bus kepulangan (Step 2).
    private var selectedDepartBus: BusModel? =
        null

    // --- VARIABEL UNTUK MENGELOLA STATE FILTER ---
    // Menyimpan daftar bus mentah (belum difilter/diurutkan) yang sedang aktif ditampilkan.
    // Bisa berisi daftar bus keberangkatan atau kepulangan.
    private var activeRawList: List<BusModel> =
        listOf()
    // Menyimpan tipe pengurutan yang sedang aktif (misal: berdasarkan harga termurah).
    private var selectedSortType = "PRICE_ASC"
    // Menyimpan filter kelas bus yang sedang aktif (misal: hanya kelas Executive).
    private var selectedClassFilter = "ALL"

    // Variabel untuk menyimpan data dari Intent agar mudah diakses di seluruh activity.
    private var originCity = ""
    private var destCity = ""

    /**
     * Fungsi yang dipanggil saat Activity pertama kali dibuat.
     * Bertugas untuk inisialisasi data, setup UI, dan memuat data awal.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_list)

        // 1. Inisialisasi Data dari Intent yang dikirim oleh SearchActivity.
        // Mengambil data kota asal, tujuan, tanggal, jumlah penumpang, dan status pulang-pergi.
        originCity = intent.getStringExtra("ORIGIN") ?: "Padang"
        destCity = intent.getStringExtra("DESTINATION") ?: "Jakarta"
        val dateDepart = intent.getStringExtra("DATE") ?: "-"
        val dateReturn = intent.getStringExtra("DATE_RETURN") ?: "-"
        val passengers = intent.getStringExtra("PASSENGERS") ?: "-"
        isRoundTrip = intent.getBooleanExtra("IS_ROUND_TRIP", false)

        // 2. Setup UI Bagian Atas (Header Summary).
        // Menampilkan ringkasan informasi perjalanan.
        updateHeaderRoute(originCity, destCity)
        findViewById<TextView>(R.id.tvSummaryDateDepart).text = dateDepart
        findViewById<TextView>(R.id.tvSummaryPassengers).text = passengers

        // Mengatur visibilitas baris tanggal pulang.
        // Baris ini hanya akan muncul jika perjalanan adalah pulang-pergi.
        val rowReturn = findViewById<LinearLayout>(R.id.rowReturnDate)
        if (isRoundTrip) {
            rowReturn.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvSummaryDateReturn).text = dateReturn
        } else {
            rowReturn.visibility = View.GONE
        }

        // 3. Menangani Navigasi Tombol Kembali (Back).
        // Baik tombol back di ActionBar maupun tombol fisik/gestur di device akan memanggil fungsi `handleBackNavigation`.
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { handleBackNavigation() }

        // Menambahkan callback kustom untuk tombol kembali.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        })

        // 4. Setup Tombol Filter.
        // Saat tombol filter diklik, akan menampilkan BottomSheet untuk opsi filter.
        findViewById<ImageButton>(R.id.btnFilter).setOnClickListener {
            showFilterBottomSheet()
        }

        // 5. Setup RecyclerView untuk menampilkan daftar bus.
        val rvBus = findViewById<RecyclerView>(R.id.rvBusList)
        rvBus.layoutManager = LinearLayoutManager(this)

        // 6. Memuat data awal, yaitu daftar bus keberangkatan (Step 1).
        loadBusFromDB(isDepartStep = true)
    }

    // =================================================================
    // LOGIKA DATABASE (MENGGANTIKAN DATA STATIS)
    // =================================================================
    /**
     * Memuat daftar bus dari database Room berdasarkan rute.
     * @param isDepartStep Boolean untuk menentukan apakah ini tahap pemilihan bus keberangkatan (true) atau kepulangan (false).
     */
    private fun loadBusFromDB(isDepartStep: Boolean) {
        val tvTitle = findViewById<TextView>(R.id.tvPageTitle)

        // Menentukan rute pencarian.
        // Jika tahap keberangkatan, rutenya adalah originCity -> destCity.
        // Jika tahap kepulangan, rutenya dibalik: destCity -> originCity.
        val searchOrigin = if (isDepartStep) originCity else destCity
        val searchDest = if (isDepartStep) destCity else originCity

        // Memperbarui judul halaman sesuai dengan tahap pemilihan.
        tvTitle.text = if (isDepartStep) "Pilih Bus Keberangkatan" else "Pilih Bus Kepulangan"

        // Menjalankan query database di background thread menggunakan Coroutine agar tidak memblokir UI.
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)

            // Melakukan query ke database untuk mencari bus yang sesuai dengan rute.
            // Jika tidak ada bus yang ditemukan untuk rute spesifik, sebagai fallback, ambil semua bus (untuk keperluan demo).
            val buses = db.appDao().searchBuses(searchOrigin, searchDest).ifEmpty {
                // Fallback: Jika rute spesifik tidak ada, tampilkan semua bus (untuk demo)
                db.appDao().getAllBuses()
            }

            // Kembali ke UI Thread untuk update layar
            withContext(Dispatchers.Main) {
                // Menampilkan pesan jika tidak ada bus yang tersedia.
                if (buses.isEmpty()) {
                    Toast.makeText(
                        this@BusListActivity,
                        "Tidak ada bus tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // Menyimpan hasil query ke variabel `activeRawList`.
                activeRawList = buses
                // Menerapkan filter dan pengurutan default, lalu menampilkan hasilnya di RecyclerView.
                applyFilterAndSort()
            }
        }
    }

    /**
     * Memperbarui teks rute di header (misal: "Padang → Jakarta").
     */
    private fun updateHeaderRoute(from: String, to: String) {
        findViewById<TextView>(R.id.tvSummaryRoute).text = "$from → $to"
    }

    // =================================================================
    // LOGIKA NAVIGASI MUNDUR
    // =================================================================
    /**
     * Mengelola logika kustom saat tombol kembali ditekan.
     */
    private fun handleBackNavigation() {
        // Jika ini adalah perjalanan PP dan pengguna sudah memilih bus pergi (sedang di step 2),
        // maka tombol kembali akan mengembalikan ke step 1 (pemilihan bus pergi).
        if (isRoundTrip && selectedDepartBus != null) {
            // 1. Reset state: Hapus bus pergi yang sudah dipilih.
            selectedDepartBus = null

            // 2. Update UI: Kembalikan teks header ke rute awal dan sembunyikan kartu bus pilihan.
            updateHeaderRoute(originCity, destCity)
            findViewById<View>(R.id.cardSelectedDepart).visibility = View.GONE

            // 3. Muat ulang data bus keberangkatan dari database.
            loadBusFromDB(isDepartStep = true)

            // Animasi UI Mundur (Geser Kanan)
            val rvBus = findViewById<RecyclerView>(R.id.rvBusList)
            rvBus.animate().translationX(rvBus.width.toFloat()).alpha(0f).setDuration(200)
                .withEndAction {
                    rvBus.translationX = -rvBus.width.toFloat()
                    rvBus.animate().translationX(0f).alpha(1f).setDuration(200).start()
                }.start()

            Toast.makeText(this, "Kembali ke bus keberangkatan", Toast.LENGTH_SHORT).show()
        } else {
            // Jika tidak, tutup activity dan kembali ke layar sebelumnya (SearchActivity).
            finish()
        }
    }

    // =================================================================
    // LOGIKA SELEKSI BUS
    // =================================================================
    /**
     * Dijalankan ketika pengguna memilih salah satu item bus dari RecyclerView.
     * @param bus Objek BusModel dari bus yang dipilih.
     */
    private fun handleBusSelection(bus: BusModel) {
        val rvBus = findViewById<RecyclerView>(R.id.rvBusList)
        // Logika berbeda untuk perjalanan Pulang-Pergi (Round Trip) dan Sekali Jalan (One Way).

        if (isRoundTrip) {
            if (selectedDepartBus == null) {
                // --- STEP 1 SELESAI: Simpan Bus Pergi ---
                selectedDepartBus = bus

                // Animasi Slide Keluar (Ke Kiri)
                rvBus.animate().translationX(-rvBus.width.toFloat()).alpha(0f).setDuration(300)
                    .withEndAction {
                        // UPDATE UI UNTUK STEP 2 (PULANG)
                        updateHeaderRoute(destCity, originCity) // Balik rute di header.
                        showSelectedDepartCard(bus)

                        // LOAD DATA PULANG DARI DB
                        loadBusFromDB(isDepartStep = false)

                        // Reset Filter
                        selectedSortType = "PRICE_ASC"
                        selectedClassFilter = "ALL"

                        // Animasi Slide Masuk
                        rvBus.translationX = rvBus.width.toFloat()
                        rvBus.animate().translationX(0f).alpha(1f).setDuration(300).start()
                    }.start()

            } else {
                // --- STEP 2 SELESAI: User Pilih Bus Pulang ---
                // Bus pergi (`selectedDepartBus`) dan bus pulang (`bus`) sudah terpilih.
                // Lanjutkan ke proses selanjutnya.
                proceedToNextBatch(selectedDepartBus!!, bus)
            }
        } else {
            // --- SEKALI JALAN ---
            // Langsung lanjutkan ke proses selanjutnya hanya dengan bus pergi.
            proceedToNextBatch(bus, null)
        }
    }

    // Menampilkan kartu kecil di atas daftar bus, berisi info bus keberangkatan yang telah dipilih.
    private fun showSelectedDepartCard(bus: BusModel) {
        val cardSelected = findViewById<View>(R.id.cardSelectedDepart)
        val tvName = findViewById<TextView>(R.id.tvSelectedDepartName)
        val tvTime = findViewById<TextView>(R.id.tvSelectedDepartTime)

        tvName.text = "${bus.operatorName} - ${bus.busClass}"
        tvTime.text = bus.departTime

        cardSelected.alpha = 0f
        cardSelected.visibility = View.VISIBLE
        cardSelected.animate().alpha(1f).setDuration(300).start()
    }

    /**
     * Menyiapkan data dan berpindah ke activity selanjutnya (SeatSelectionActivity).
     * @param departBus Bus keberangkatan yang dipilih.
     * @param returnBus Bus kepulangan yang dipilih (bisa null jika sekali jalan).
     */
    private fun proceedToNextBatch(departBus: BusModel, returnBus: BusModel?) {
        val intent = Intent(this, SeatSelectionActivity::class.java)

        // Mengirim data bus yang dipilih sebagai Parcelable Extra.
        intent.putExtra("BUS_DATA", departBus)
        if (returnBus != null) {
            // BUS_NEXT_DATA digunakan untuk bus pulang.
            intent.putExtra("BUS_NEXT_DATA", returnBus)
        }

        // Mengirim kembali data pencarian awal agar bisa digunakan di activity selanjutnya.
        intent.putExtra("PASSENGERS", this.intent.getStringExtra("PASSENGERS"))
        intent.putExtra("ORIGIN", originCity)
        intent.putExtra("DESTINATION", destCity)
        intent.putExtra("DATE", this.intent.getStringExtra("DATE"))
        intent.putExtra("DATE_RETURN", this.intent.getStringExtra("DATE_RETURN"))
        intent.putExtra("IS_ROUND_TRIP", isRoundTrip)

        startActivity(intent)
    }

    // =================================================================
    // LOGIKA FILTER & SORTIR
    // =================================================================
    /**
     * Menerapkan logika filter dan sortir ke `activeRawList`, lalu memperbarui RecyclerView.
     */
    private fun applyFilterAndSort() {
        // 1. Proses Filter berdasarkan Kelas Bus.
        // Jika filter "ALL", gunakan semua data mentah. Jika tidak, filter daftar berdasarkan kelas yang dipilih.
        var processedList = if (selectedClassFilter == "ALL") {
            activeRawList
        } else {
            activeRawList.filter { it.busClass.contains(selectedClassFilter, ignoreCase = true) }
        }

        // 2. Proses Pengurutan (Sorting).
        // Urutkan daftar yang sudah difilter berdasarkan tipe sortiran yang aktif.
        processedList = when (selectedSortType) {
            "PRICE_ASC" -> processedList.sortedBy { it.price }
            "TIME_ASC" -> processedList.sortedBy { it.departTime }
            "TIME_DESC" -> processedList.sortedByDescending { it.departTime }
            else -> processedList
        }

        // 3. Tampilkan hasil akhir ke RecyclerView.
        updateRecyclerView(processedList)
    }

    /**
     * Memperbarui data di RecyclerView dengan daftar bus yang baru.
     * @param data Daftar bus yang sudah diproses (filter & sortir).
     */
    private fun updateRecyclerView(data: List<BusModel>) {
        val rvBus = findViewById<RecyclerView>(R.id.rvBusList)

        // Membuat instance baru dari BusAdapter dengan data yang telah diperbarui.
        // Lambda `{ bus -> handleBusSelection(bus) }` adalah aksi yang akan dijalankan
        // ketika salah satu item di adapter di-klik.
        val adapter = BusAdapter(data) { bus ->
            handleBusSelection(bus)
        }
        rvBus.adapter = adapter
    }

    // Menampilkan dialog BottomSheet yang berisi opsi filter dan sortir.
    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_filter, null)
        dialog.setContentView(view)

        val chipGroupSort = view.findViewById<ChipGroup>(R.id.chipGroupSort)
        val chipGroupClass = view.findViewById<ChipGroup>(R.id.chipGroupClass)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilter)
        val btnReset = view.findViewById<TextView>(R.id.btnResetFilter)

        // TODO: Atur state awal chip sesuai dengan `selectedSortType` dan `selectedClassFilter` saat ini.

        // Aksi untuk tombol reset: mengembalikan pilihan chip ke default.
        btnReset.setOnClickListener {
            chipGroupSort.check(R.id.chipSortPriceAsc)
            chipGroupClass.check(R.id.chipClassAll)
        }

        btnApply.setOnClickListener {
            // Mengambil pilihan dari ChipGroup Sortir dan menyimpannya di state.
            selectedSortType = when (chipGroupSort.checkedChipId) {
                R.id.chipSortTimeAsc -> "TIME_ASC"
                R.id.chipSortTimeDesc -> "TIME_DESC"
                else -> "PRICE_ASC"
            }
            // Mengambil pilihan dari ChipGroup Kelas dan menyimpannya di state.
            selectedClassFilter = when (chipGroupClass.checkedChipId) {
                R.id.chipClassExecutive -> "Executive"
                R.id.chipClassRoyal -> "Royal" // Sesuaikan dengan data di DB
                R.id.chipClassSutan -> "Sutan"
                else -> "ALL"
            }
            applyFilterAndSort()
            // Tutup dialog setelah filter diterapkan.
            dialog.dismiss()
        }
        // Tampilkan dialog.
        dialog.show()
    }
}