package com.contoh.eticketbus2025.ui.booking // <--- Package Baru

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.BusModel
import java.text.NumberFormat
import java.util.ArrayList
import java.util.Locale

/**
 * Activity ini bertanggung jawab untuk menampilkan layout kursi bus dan memungkinkan pengguna
 * untuk memilih kursi yang diinginkan. Activity ini menangani dua skenario:
 * 1. Perjalanan sekali jalan (pergi saja).
 * 2. Perjalanan pulang-pergi (pergi dan pulang).
 *
 * Data bus, rute, dan jumlah penumpang diterima melalui Intent.
 */
class SeatSelectionActivity : AppCompatActivity() {

    // --- DATA BUS ---
    // Menyimpan data bus yang sedang ditampilkan di layar (bisa bus pergi atau bus pulang).
    private lateinit var currentBus: BusModel
    // Menyimpan data bus untuk perjalanan selanjutnya (khususnya untuk perjalanan pulang). Jika null, berarti ini adalah tahap terakhir.
    private var nextBus: BusModel? = null
    // Menyimpan data bus dari perjalanan sebelumnya (khususnya saat memilih kursi pulang, ini adalah data bus pergi).
    private var previousBus: BusModel? = null

    // Menyimpan daftar kursi yang telah dipilih pada tahap sebelumnya (kursi pergi).
    private var previousSelectedSeats: ArrayList<String>? = null

    // --- STATE ---
    // Jumlah tiket/kursi yang harus dipilih, didapat dari intent.
    private var ticketCount = 1
    // Daftar kursi yang sedang dipilih oleh pengguna di layar saat ini.
    private var selectedSeatsList = listOf<String>()
    // Kota asal dan tujuan perjalanan.
    private var originCity = ""
    private var destCity = ""

    // Tanggal keberangkatan dan tanggal pulang (jika ada).
    private var dateDepart = ""
    private var dateReturn = ""

    // Flag untuk menandakan apakah activity ini sedang dalam mode pemilihan kursi pulang.
    private var isReturnPhase = false

    /**
     * Fungsi yang dipanggil saat Activity pertama kali dibuat.
     * Menginisialisasi UI dan data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_selection)

        // Memuat data dari Intent. Jika gagal, hentikan eksekusi dan tutup activity.
        if (!loadIntentData()) return

        // Menyiapkan tampilan informasi header (detail bus, rute, dll).
        setupHeaderInfo()
        // Menyiapkan grid layout untuk pemilihan kursi.
        setupSeatGrid()

        // Mengatur listener untuk tombol kembali.
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        // Mengatur listener untuk tombol checkout/lanjut.
        findViewById<Button>(R.id.btnCheckout).setOnClickListener { handleCheckout() }
    }

    /**
     * Memuat semua data yang diperlukan dari Intent yang dikirim dari activity sebelumnya.
     * Data ini mencakup detail bus, informasi penumpang, rute, dan tanggal.
     * @return Boolean `true` jika data berhasil dimuat, `false` jika terjadi kesalahan.
     */
    private fun loadIntentData(): Boolean {
        return try {
            // Mengambil data bus saat ini. Menggunakan 'DEPRECATION' untuk getSerializableExtra versi lama.
            @Suppress("DEPRECATION")
            currentBus = intent.getSerializableExtra("BUS_DATA") as BusModel

            // Mengambil data untuk bus selanjutnya (jika ada, untuk perjalanan pulang).
            nextBus = intent.getSerializableExtra("BUS_NEXT_DATA") as? BusModel

            // Mengambil data dari bus sebelumnya dan kursi yang sudah dipilih (jika ini adalah tahap pulang).
            previousBus = intent.getSerializableExtra("BUS_PREV_DATA") as? BusModel
            previousSelectedSeats = intent.getStringArrayListExtra("SEATS_PREV_DATA")

            // Menentukan apakah ini fase pemilihan kursi pulang berdasarkan keberadaan data bus sebelumnya.
            isReturnPhase = (previousBus != null)

            val passengersStr = intent.getStringExtra("PASSENGERS") ?: "1 Tiket"
            ticketCount = passengersStr.filter { it.isDigit() }.toIntOrNull() ?: 1

            originCity = intent.getStringExtra("ORIGIN") ?: "Padang"
            destCity = intent.getStringExtra("DESTINATION") ?: "Jakarta"

            dateDepart = intent.getStringExtra("DATE") ?: ""
            dateReturn = intent.getStringExtra("DATE_RETURN") ?: ""

            true
        } catch (e: Exception) {
            // Menangani error jika data dari intent tidak valid atau tidak ada.
            e.printStackTrace()
            Toast.makeText(this, "Gagal memuat data bus", Toast.LENGTH_SHORT).show()
            finish()
            false
        }
    }

    /**
     * Mengatur informasi yang ditampilkan di bagian atas layar.
     * Ini termasuk judul halaman, rute, detail bus (nama, kelas, rating, waktu),
     * dan daftar fasilitas bus.
     */
    private fun setupHeaderInfo() {
        val tvTitle = findViewById<TextView>(R.id.tvPageTitle)
        // Mengubah judul dan rute berdasarkan apakah ini perjalanan pergi atau pulang.
        if (isReturnPhase) {
            tvTitle.text = "Pilih Kursi Pulang"
            findViewById<TextView>(R.id.tvCityOrigin).text = destCity
            findViewById<TextView>(R.id.tvCityDest).text = originCity
        } else {
            tvTitle.text = "Pilih Kursi Pergi"
            findViewById<TextView>(R.id.tvCityOrigin).text = originCity
            findViewById<TextView>(R.id.tvCityDest).text = destCity
        }

        // Mengisi detail bus saat ini.
        findViewById<TextView>(R.id.tvOperatorName).text = currentBus.operatorName
        findViewById<TextView>(R.id.tvClass).text = currentBus.busClass
        findViewById<TextView>(R.id.tvRating).text = currentBus.rating.toString()
        findViewById<TextView>(R.id.tvTimeDepart).text = currentBus.departTime
        findViewById<TextView>(R.id.tvTimeArrive).text = currentBus.arriveTime

        updateSelectionTitle(0)
        // Mengupdate bottom bar dengan keadaan awal (belum ada kursi dipilih).
        updateBottomBar(emptyList())

        // Menampilkan daftar fasilitas bus secara dinamis.
        val layoutFacilities = findViewById<LinearLayout>(R.id.layoutFacilities)
        // Membersihkan view sebelumnya untuk menghindari duplikasi.
        layoutFacilities.removeAllViews()
        currentBus.facilities.forEach { facilityName ->
            // Membuat TextView untuk setiap fasilitas.
            val tv = TextView(this)
            tv.text = facilityName
            tv.setBackgroundResource(R.drawable.bg_facility_chip)
            tv.setTextColor(ContextCompat.getColor(this, R.color.white_dim))
            tv.textSize = 12f
            val paddingH = dpToPx(12)
            val paddingV = dpToPx(6)
            tv.setPadding(paddingH, paddingV, paddingH, paddingV)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = dpToPx(8)
            tv.layoutParams = params
            layoutFacilities.addView(tv)
        }
    }

    /**
     * Menginisialisasi RecyclerView yang berfungsi sebagai grid denah kursi.
     * Menggunakan SeatAdapter untuk menampilkan dan mengelola interaksi pada setiap kursi.
     */
    private fun setupSeatGrid() {
        val rvSeats = findViewById<RecyclerView>(R.id.rvSeats)
        // Menggunakan GridLayoutManager dengan 5 kolom untuk merepresentasikan denah bus.
        rvSeats.layoutManager = GridLayoutManager(this, 5)

        // Contoh data kursi yang sudah terisi (occupied).
        val occupied = if (isReturnPhase) listOf("1A", "1B", "5D") else listOf("2C", "3D", "5B")

        // Membuat instance SeatAdapter.
        val adapter = SeatAdapter(
            totalRows = 10,
            occupiedSeats = occupied,
            maxSelection = ticketCount,
            // Lambda function ini akan dipanggil setiap kali ada perubahan pada kursi yang dipilih.
        ) { selectedSeats ->
            // Menyimpan daftar kursi yang baru dipilih.
            selectedSeatsList = selectedSeats
            // Memperbarui UI di bottom bar (total harga, status tombol).
            updateBottomBar(selectedSeats)
            // Memperbarui judul yang menunjukkan jumlah kursi terpilih.
            updateSelectionTitle(selectedSeats.size)
        }
        rvSeats.adapter = adapter
    }

    /**
     * Memperbarui tampilan di bagian bawah layar (bottom bar).
     * Ini termasuk total harga, label harga, teks informasi kursi terpilih,
     * dan status tombol "Checkout".
     * @param seats Daftar kursi yang sedang dipilih.
     */
    private fun updateBottomBar(seats: List<String>) {
        val tvTotal = findViewById<TextView>(R.id.tvTotalPrice)
        val lblTotal = findViewById<TextView>(R.id.lblTotal)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)
        val tvInfo = findViewById<TextView>(R.id.tvSelectedSeatsInfo)

        // Menghitung harga total berdasarkan jumlah kursi yang dipilih dan harga per tiket.
        val currentLegPrice = seats.size * currentBus.price
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        tvTotal.text = formatRp.format(currentLegPrice)

        // Menyesuaikan label harga berdasarkan fase perjalanan.
        if (isReturnPhase) {
            lblTotal.text = "Total Harga (Pulang)"
        } else {
            if (nextBus != null) lblTotal.text = "Total Harga (Pergi)"
            else lblTotal.text = "Total Harga"
        }

        // Menampilkan nama kursi yang dipilih atau pesan default.
        if (seats.isEmpty()) tvInfo.text = "Silakan pilih kursi di peta"
        else tvInfo.text = "Kursi terpilih: ${seats.joinToString(", ")}"

        // Mengaktifkan atau menonaktifkan tombol checkout berdasarkan apakah
        // jumlah kursi yang dipilih sudah sesuai dengan jumlah tiket.
        if (seats.size == ticketCount) {
            btnCheckout.isEnabled = true
            btnCheckout.alpha = 1.0f
            if (nextBus != null) btnCheckout.text = "Lanjut Pilih Kursi Pulang"
            else btnCheckout.text = "Lanjut Pembayaran"
        } else {
            btnCheckout.isEnabled = false
            btnCheckout.alpha = 0.5f
            btnCheckout.text = "Pilih ${ticketCount - seats.size} Kursi Lagi"
        }
    }

    /**
     * Memperbarui judul di atas denah kursi untuk menunjukkan progres pemilihan.
     * Contoh: "Pilih Kursi (1/2)"
     * @param current Jumlah kursi yang saat ini telah dipilih.
     */
    private fun updateSelectionTitle(current: Int) {
        val tvTitle = findViewById<TextView>(R.id.tvSelectTitle)
        tvTitle.text = "Pilih Kursi ($current/$ticketCount)"
    }

    /**
     * Menangani logika ketika tombol "Checkout" atau "Lanjut" ditekan.
     * Ada dua skenario utama:
     * 1. Jika ini adalah perjalanan PP dan baru selesai memilih kursi pergi (`nextBus` tidak null),
     *    maka akan memulai ulang SeatSelectionActivity untuk memilih kursi pulang.
     * 2. Jika ini adalah perjalanan sekali jalan atau tahap pemilihan kursi pulang,
     *    maka akan melanjutkan ke PaymentActivity.
     */
    private fun handleCheckout() {
        // SKENARIO 1: Masih ada tahap selanjutnya (Pilih Kursi Pulang)
        if (nextBus != null) {
            // Membuat intent untuk membuka kembali activity ini untuk fase berikutnya.
            val intent = Intent(this, SeatSelectionActivity::class.java)

            // Mengoper data bus pulang sebagai 'BUS_DATA' untuk sesi berikutnya.
            intent.putExtra("BUS_DATA", nextBus)
            // Menyimpan data bus pergi dan kursi yang dipilih untuk digunakan nanti di pembayaran.
            intent.putExtra("BUS_PREV_DATA", currentBus)
            intent.putStringArrayListExtra("SEATS_PREV_DATA", ArrayList(selectedSeatsList))

            // Meneruskan data perjalanan yang sama.
            intent.putExtra("PASSENGERS", "$ticketCount Tiket")
            intent.putExtra("ORIGIN", originCity)
            intent.putExtra("DESTINATION", destCity)
            intent.putExtra("DATE", dateDepart)
            intent.putExtra("DATE_RETURN", dateReturn)

            // Menandakan bahwa tidak ada lagi tahap setelah ini.
            intent.putExtra("BUS_NEXT_DATA", null as BusModel?)

            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
        // SKENARIO 2: Tahap terakhir, lanjut ke pembayaran.
        else {
            val intent = Intent(this, PaymentActivity::class.java)

            // Jika ini adalah fase pulang, sertakan data perjalanan pergi dan pulang.
            if (isReturnPhase && previousBus != null) {
                intent.putExtra("BUS_DEPART", previousBus)
                intent.putStringArrayListExtra("SEATS_DEPART", previousSelectedSeats)

                intent.putExtra("BUS_RETURN", currentBus)
                intent.putStringArrayListExtra("SEATS_RETURN", ArrayList(selectedSeatsList))
            } else {
                // Jika ini perjalanan sekali jalan, hanya sertakan data perjalanan pergi.
                intent.putExtra("BUS_DEPART", currentBus)
                intent.putStringArrayListExtra("SEATS_DEPART", ArrayList(selectedSeatsList))
            }

            // Meneruskan informasi perjalanan ke activity pembayaran.
            intent.putExtra("ORIGIN", originCity) // Sebenarnya ini bisa diambil dari data bus, tapi diteruskan untuk konsistensi
            intent.putExtra("DESTINATION", destCity)
            intent.putExtra("DATE", dateDepart)
            intent.putExtra("DATE_RETURN", dateReturn)

            startActivity(intent)
        }
    }

    /**
     * Fungsi utilitas untuk mengonversi nilai dp (density-independent pixel) ke piksel (px).
     * Berguna untuk mengatur ukuran dan margin secara programatik.
     * @param dp Nilai dalam dp.
     * @return Nilai ekuivalen dalam piksel.
     */
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}