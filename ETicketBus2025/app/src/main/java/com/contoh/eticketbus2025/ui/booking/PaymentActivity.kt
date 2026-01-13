package com.contoh.eticketbus2025.ui.booking

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.BusModel
import com.contoh.eticketbus2025.ui.ticket.TicketSuccessActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

/**
 * Activity untuk proses pembayaran tiket.
 * Halaman ini menampilkan ringkasan pesanan, opsi promo, pilihan metode pembayaran,
 * dan timer untuk batas waktu pembayaran.
 */
class PaymentActivity : AppCompatActivity() {

    // Properti untuk menyimpan data tiket yang dipesan
    private lateinit var busDepart: BusModel
    private var busReturn: BusModel? = null

    private var seatsDepart: ArrayList<String> = arrayListOf()
    private var seatsReturn: ArrayList<String> = arrayListOf()

    // Variabel untuk mengelola state harga
    private var baseTotalPrice = 0.0 // Harga sebelum diskon
    private var finalTotalPrice = 0.0 // Harga setelah diskon
    private var discountAmount = 0.0

    // Variabel untuk menyimpan state metode pembayaran yang dipilih
    private var selectedPaymentMethod = ""
    private var selectedPaymentDetail = ""

    /**
     * Fungsi yang dipanggil saat Activity dibuat.
     * Menginisialisasi UI, memuat data, dan mengatur listener.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Memuat data dari Intent, jika gagal, tutup activity
        if (!loadData()) return

        // Menyiapkan berbagai bagian UI
        setupSummaryUI()
        setupPromoLogic() // <-- FITUR BARU
        setupPaymentMethodLogic()
        startTimer()

        // Listener untuk tombol kembali
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Listener untuk tombol konfirmasi pembayaran
        findViewById<Button>(R.id.btnConfirmPayment).setOnClickListener {
            if (selectedPaymentMethod.isEmpty()) {
                Toast.makeText(this, "Pilih metode pembayaran dulu", Toast.LENGTH_SHORT).show()
            } else {
                // Jika metode pembayaran sudah dipilih, lanjutkan ke halaman sukses
                val intent = Intent(this, TicketSuccessActivity::class.java)
                // Memasukkan semua data yang relevan ke Intent untuk ditampilkan di halaman sukses
                intent.putExtra("BUS_DEPART", busDepart)
                intent.putExtra("BUS_RETURN", busReturn)
                intent.putStringArrayListExtra("SEATS_DEPART", seatsDepart)
                intent.putStringArrayListExtra("SEATS_RETURN", seatsReturn)
                intent.putExtra("ORIGIN", this.intent.getStringExtra("ORIGIN"))
                intent.putExtra("DESTINATION", this.intent.getStringExtra("DESTINATION"))
                intent.putExtra("DATE", this.intent.getStringExtra("DATE"))
                intent.putExtra("DATE_RETURN", this.intent.getStringExtra("DATE_RETURN"))
                // Membuat ID Booking unik berdasarkan waktu saat ini
                val bookingId = "ETB-${System.currentTimeMillis().toString().takeLast(6)}"
                intent.putExtra("BOOKING_ID", bookingId)
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * Memuat data pesanan (detail bus dan kursi) dari Intent.
     * Menggunakan @Suppress("DEPRECATION") karena getSerializableExtra(key) sudah usang di API 33+.
     * @return Boolean - true jika data berhasil dimuat, false jika gagal.
     */
    private fun loadData(): Boolean {
        return try {
            @Suppress("DEPRECATION")
            busDepart = intent.getSerializableExtra("BUS_DEPART") as BusModel
            seatsDepart = intent.getStringArrayListExtra("SEATS_DEPART") ?: arrayListOf()
            @Suppress("DEPRECATION")
            busReturn = intent.getSerializableExtra("BUS_RETURN") as? BusModel
            seatsReturn = intent.getStringArrayListExtra("SEATS_RETURN") ?: arrayListOf()
            true
        } catch (e: Exception) {
            // Jika ada error saat memuat data, tutup activity untuk mencegah crash
            finish(); false
        }
    }

    /**
     * Menyiapkan dan menampilkan ringkasan pesanan di UI.
     * Mencakup detail perjalanan pergi dan pulang (jika ada).
     */
    private fun setupSummaryUI() {
        val origin = intent.getStringExtra("ORIGIN") ?: "Padang"
        val dest = intent.getStringExtra("DESTINATION") ?: "Jakarta"
        val dateStr = intent.getStringExtra("DATE") ?: "22 Nov 2025"
        val dateReturnStr = intent.getStringExtra("DATE_RETURN") ?: "-"
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        // === Menampilkan data perjalanan PERGI ===
        findViewById<TextView>(R.id.tvBusNameFull).text =
            "${busDepart.operatorName} - ${busDepart.busClass}"
        findViewById<TextView>(R.id.tvOriginPay).text = origin
        findViewById<TextView>(R.id.tvDestPay).text = dest
        findViewById<TextView>(R.id.tvDatePay).text = dateStr
        findViewById<TextView>(R.id.tvTimePay).text = busDepart.departTime
        findViewById<TextView>(R.id.tvSeatList).text = seatsDepart.joinToString(", ")

        val countDepart = seatsDepart.size
        val priceDepartTotal = busDepart.price * countDepart

        findViewById<TextView>(R.id.tvPriceLabelDepart).text =
            "$countDepart Kursi x ${formatRp.format(busDepart.price)}"
        findViewById<TextView>(R.id.tvPriceTotalDepart).text = formatRp.format(priceDepartTotal)

        // Inisialisasi total harga awal dengan harga tiket pergi
        baseTotalPrice = priceDepartTotal

        // === Menampilkan data perjalanan PULANG (jika ada) ===
        val sectionReturn = findViewById<LinearLayout>(R.id.sectionReturn)
        if (busReturn != null) {
            sectionReturn.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvBusNameReturn).text =
                "${busReturn!!.operatorName} - ${busReturn!!.busClass}"
            findViewById<TextView>(R.id.tvOriginPayReturn).text = dest
            findViewById<TextView>(R.id.tvDestPayReturn).text = origin
            findViewById<TextView>(R.id.tvDatePayReturn).text = dateReturnStr
            findViewById<TextView>(R.id.tvTimePayReturn).text = busReturn!!.departTime
            findViewById<TextView>(R.id.tvSeatsReturn).text = seatsReturn.joinToString(", ")

            val countReturn = seatsReturn.size
            val priceReturnTotal = busReturn!!.price * countReturn

            findViewById<TextView>(R.id.tvPriceLabelReturn).text =
                "$countReturn Kursi x ${formatRp.format(busReturn!!.price)}"
            findViewById<TextView>(R.id.tvPriceTotalReturn).text = formatRp.format(priceReturnTotal)

            // Tambahkan harga tiket pulang ke total harga dasar
            baseTotalPrice += priceReturnTotal
        } else {
            sectionReturn.visibility = View.GONE
        }

        // Memperbarui UI total harga untuk pertama kali
        updateTotalUI()
    }

    /**
     * Menyiapkan logika untuk fitur promo.
     * Mengatur listener pada tombol "Terapkan" dan memvalidasi kode promo.
     */
    private fun setupPromoLogic() {
        val etPromo = findViewById<EditText>(R.id.etPromoCode)
        val btnApply = findViewById<Button>(R.id.btnApplyPromo)
        val layoutDiscount = findViewById<LinearLayout>(R.id.layoutDiscountInfo)
        val tvDiscount = findViewById<TextView>(R.id.tvDiscountAmount)

        btnApply.setOnClickListener {
            val code = etPromo.text.toString().uppercase().trim()

            if (code.isEmpty()) {
                Toast.makeText(this, "Masukkan kode promo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Reset nilai diskon setiap kali tombol ditekan
            discountAmount = 0.0
            var isSuccess = false

            // Logika pengecekan kode promo (saat ini hardcoded)
            when (code) {
                "NEWUSER30" -> {
                    discountAmount = baseTotalPrice * 0.30 // Diskon 30%
                    isSuccess = true
                }

                "WEEKEND20" -> {
                    discountAmount = baseTotalPrice * 0.20 // Diskon 20%
                    isSuccess = true
                }

                "CASHBACK50" -> {
                    discountAmount = 50000.0 // Potongan 50rb
                    isSuccess = true
                }

                "PREMIUM15" -> {
                    discountAmount = baseTotalPrice * 0.15 // Diskon 15%
                    isSuccess = true
                }

                else -> {
                    Toast.makeText(this, "Kode promo tidak valid", Toast.LENGTH_SHORT).show()
                    layoutDiscount.visibility = View.GONE
                }
            }

            if (isSuccess) {
                // Menerapkan batas maksimal diskon (misal Rp 100.000)
                if (discountAmount > 100000) discountAmount = 100000.0

                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                layoutDiscount.visibility = View.VISIBLE
                tvDiscount.text = "-${formatRp.format(discountAmount)}"

                Toast.makeText(this, "Promo berhasil diterapkan!", Toast.LENGTH_SHORT).show()

                // Sembunyikan keyboard setelah promo diterapkan
                val imm =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(etPromo.windowToken, 0)
            }

            updateTotalUI()
        }
    }

    /**
     * Memperbarui tampilan total harga akhir setelah ada perubahan (misal: promo diterapkan).
     */
    private fun updateTotalUI() {
        // Hitung ulang harga akhir
        finalTotalPrice = baseTotalPrice - discountAmount
        if (finalTotalPrice < 0) finalTotalPrice = 0.0 // Pastikan harga tidak negatif

        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        findViewById<TextView>(R.id.tvFinalPrice).text = formatRp.format(finalTotalPrice)

        // Jika detail pembayaran sedang ditampilkan, perbarui juga total tagihan di sana
        if (selectedPaymentMethod == "BANK" && selectedPaymentDetail.isNotEmpty()) {
            updateDetailViewBank(selectedPaymentDetail)
        } else if (selectedPaymentMethod == "EWALLET" && selectedPaymentDetail.isNotEmpty()) {
            updateDetailViewEwallet(selectedPaymentDetail)
        }
    }

    // ... (Sisa kode logika pembayaran tidak berubah) ...

    /**
     * Mengatur logika untuk pilihan metode pembayaran (QRIS, Transfer Bank, E-Wallet).
     * Mengatur listener untuk setiap opsi.
     */
    private fun setupPaymentMethodLogic() {
        val optQris = findViewById<LinearLayout>(R.id.optQris);
        val optTransfer = findViewById<LinearLayout>(R.id.optTransfer);
        val optEwallet = findViewById<LinearLayout>(R.id.optEwallet)
        val rbQris = findViewById<RadioButton>(R.id.rbQris);
        val rbBank = findViewById<RadioButton>(R.id.rbBank);
        val rbEwallet = findViewById<RadioButton>(R.id.rbEwallet)
        // Fungsi helper untuk mereset semua pilihan radio button
        fun resetSelection() {
            rbQris.isChecked = false; rbBank.isChecked = false; rbEwallet.isChecked = false
        }
        // Listener untuk opsi QRIS
        optQris.setOnClickListener {
            resetSelection(); rbQris.isChecked = true; selectedPaymentMethod =
            "QRIS"; updateDetailViewQRIS()
        }
        // Listener untuk opsi Transfer Bank, akan menampilkan bottom sheet pilihan bank
        optTransfer.setOnClickListener {
            resetSelection(); rbBank.isChecked = true; selectedPaymentMethod =
            "BANK"; showPaymentOptionSheet(
            "Pilih Bank",
            listOf("BCA", "Mandiri", "BNI", "BRI")
        )
        }
        // Listener untuk opsi E-Wallet, akan menampilkan bottom sheet pilihan e-wallet
        optEwallet.setOnClickListener {
            resetSelection(); rbEwallet.isChecked = true; selectedPaymentMethod =
            "EWALLET"; showPaymentOptionSheet(
            "Pilih E-Wallet",
            listOf("GoPay", "OVO", "Dana", "ShopeePay")
        )
        }
    }

    /**
     * Menampilkan detail pembayaran untuk metode QRIS.
     * Membuat view secara programatik.
     */
    private fun updateDetailViewQRIS() {
        val container =
            findViewById<LinearLayout>(R.id.paymentDetailArea); container.removeAllViews(); addDetailHeader(
            container,
            "Scan QR Code"
        )
        // Menambahkan gambar QR Code
        val imgQr =
            ImageView(this); imgQr.setImageResource(R.drawable.ic_qr_code); imgQr.setColorFilter(
            ContextCompat.getColor(this, R.color.black_text)
        ); imgQr.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        val params = LinearLayout.LayoutParams(500, 500); params.gravity =
            Gravity.CENTER; params.bottomMargin = 32; imgQr.layoutParams = params; imgQr.setPadding(
            24,
            24,
            24,
            24
        ); container.addView(imgQr)
        // Menambahkan teks informasi
        val tvInfo = TextView(this); tvInfo.text =
            "Scan kode di atas dengan aplikasi E-Wallet Anda."; tvInfo.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white_dim
            )
        ); tvInfo.gravity = Gravity.CENTER; tvInfo.textSize = 13f; container.addView(tvInfo)
    }

    /**
     * Menampilkan detail pembayaran untuk metode Transfer Bank.
     * @param bankName Nama bank yang dipilih.
     */
    private fun updateDetailViewBank(bankName: String) {
        selectedPaymentDetail = bankName // Simpan nama bank yang dipilih
        val container =
            findViewById<LinearLayout>(R.id.paymentDetailArea); container.removeAllViews()
        // Generate nomor Virtual Account acak
        val vaNumber = "8800" + Random.nextLong(1000000000, 9999999999);
        // Ambil total harga terbaru
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val totalStr = formatRp.format(finalTotalPrice)

        addDetailHeader(container, "Instruksi Transfer $bankName"); addDetailCard(
            container,
            "Bank Tujuan",
            bankName,
            false
        ); addDetailCard(container, "Nomor Virtual Account", vaNumber, true); addDetailCard(
            container,
            "Total Tagihan",
            totalStr,
            true
        )
    }

    /**
     * Menampilkan detail pembayaran untuk metode E-Wallet.
     * @param walletName Nama E-Wallet yang dipilih.
     */
    private fun updateDetailViewEwallet(walletName: String) {
        selectedPaymentDetail = walletName // Simpan nama e-wallet yang dipilih
        val container =
            findViewById<LinearLayout>(R.id.paymentDetailArea); container.removeAllViews()

        // Ambil total harga terbaru
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val totalStr = formatRp.format(finalTotalPrice)

        addDetailHeader(container, "Instruksi $walletName"); addDetailCard(
            container,
            "Metode",
            walletName,
            false
        ); addDetailCard(
            container,
            "Nomor Tujuan",
            "0812-3456-7890",
            true
        ); addDetailCard(container, "Total Tagihan", totalStr, false)
    }

    /**
     * Fungsi helper untuk membuat dan menambahkan baris detail pembayaran (seperti kartu)
     * ke dalam container secara programatik.
     * @param container LinearLayout tempat kartu akan ditambahkan.
     * @param label Teks label (misal: "Nomor Virtual Account").
     * @param value Teks nilai (misal: "8800123456789").
     * @param isCopyable Jika true, akan menambahkan tombol salin.
     */
    private fun addDetailCard(
        container: LinearLayout,
        label: String,
        value: String,
        isCopyable: Boolean
    ) {
        val row = LinearLayout(this); row.orientation = LinearLayout.HORIZONTAL; row.gravity =
            // Membuat layout baris
            Gravity.CENTER_VERTICAL; row.setBackgroundResource(R.drawable.bg_input_field); row.background.setTint(
            ContextCompat.getColor(this, R.color.white_glass)
        )
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ); params.bottomMargin = 16; row.layoutParams = params; row.setPadding(32, 24, 32, 24)
        // Membuat container untuk teks label dan value
        val textContainer = LinearLayout(this); textContainer.orientation = LinearLayout.VERTICAL;
        val textParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ); textContainer.layoutParams = textParams
        val tvLabel = TextView(this); tvLabel.text = label; tvLabel.textSize =
            11f; tvLabel.setTextColor(ContextCompat.getColor(this, R.color.white_dim))
        val tvValue = TextView(this); tvValue.text = value; tvValue.textSize =
            16f; tvValue.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        ); tvValue.setTypeface(null, android.graphics.Typeface.BOLD)
        textContainer.addView(tvLabel); textContainer.addView(tvValue); row.addView(textContainer)
        // Jika isCopyable, tambahkan ikon salin dengan listener
        if (isCopyable) {
            val imgCopy =
                ImageView(this); imgCopy.setImageResource(android.R.drawable.ic_menu_save); imgCopy.setColorFilter(
                ContextCompat.getColor(this, R.color.accent_orange)
            )
            val imgParams = LinearLayout.LayoutParams(48, 48); imgParams.marginStart =
                16; imgCopy.layoutParams = imgParams
            imgCopy.setOnClickListener {
                // Logika untuk menyalin teks ke clipboard
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager;
                val clip = ClipData.newPlainText(
                    "Pembayaran",
                    value
                ); clipboard.setPrimaryClip(clip); Toast.makeText(
                this,
                "Disalin: $value",
                Toast.LENGTH_SHORT
            ).show()
            }; row.addView(imgCopy)
        }
        container.addView(row)
    }

    /**
     * Fungsi helper untuk menambahkan judul di area detail pembayaran.
     * @param container LinearLayout tempat judul akan ditambahkan.
     * @param title Teks judul.
     */
    private fun addDetailHeader(container: LinearLayout, title: String) {
        val tvTitle = TextView(this); tvTitle.text = title; tvTitle.textSize =
            16f; tvTitle.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        ); tvTitle.setTypeface(null, android.graphics.Typeface.BOLD); tvTitle.gravity =
            Gravity.CENTER
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ); params.bottomMargin = 24; tvTitle.layoutParams = params; container.addView(tvTitle)
    }

    /**
     * Menampilkan BottomSheetDialog dengan daftar opsi pembayaran (Bank atau E-Wallet).
     * @param title Judul yang akan ditampilkan di BottomSheet.
     * @param options Daftar string opsi yang akan ditampilkan di RecyclerView.
     */
    private fun showPaymentOptionSheet(title: String, options: List<String>) {
        val dialog = BottomSheetDialog(this);
        val view = layoutInflater.inflate(
            R.layout.layout_bottom_sheet_payment_option,
            null
        ); dialog.setContentView(view)
        view.findViewById<TextView>(R.id.tvSheetTitle).text = title;
        val rv = view.findViewById<RecyclerView>(R.id.rvPaymentOptions)
        rv.layoutManager = LinearLayoutManager(this); rv.adapter =
            // Menggunakan adapter untuk menampilkan daftar opsi
            PaymentOptionAdapter(options) { selected ->
                selectedPaymentDetail =
                    selected; if (selectedPaymentMethod == "BANK") updateDetailViewBank(selected) else updateDetailViewEwallet(
                selected
            ); dialog.dismiss()
            }; dialog.show()
    }

    /**
     * Adapter untuk RecyclerView yang menampilkan daftar opsi pembayaran di BottomSheet.
     */
    class PaymentOptionAdapter(
        private val items: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<PaymentOptionAdapter.ViewHolder>() {

        // Mapping dari nama bank/e-wallet ke resource drawable ikonnya.
        // Memudahkan penggantian ikon secara dinamis.
        private val iconMap = mapOf(
            "BCA" to R.drawable.ic_payment_bca,
            "BNI" to R.drawable.ic_payment_bni,
            "BRI" to R.drawable.ic_payment_bri,
            "Mandiri" to R.drawable.ic_payment_mandiri,
            "GoPay" to R.drawable.ic_payment_gopay,
            "OVO" to R.drawable.ic_payment_ovo,
            "Dana" to R.drawable.ic_payment_dana,
            "ShopeePay" to R.drawable.ic_payment_shopeepay
        )

        /**
         * ViewHolder untuk setiap item di RecyclerView.
         */
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvOptionName)
            val imgLogo: ImageView = view.findViewById(R.id.imgOptionLogo)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_payment_option, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val itemName = items[position]
            holder.tvName.text = itemName
            // Mengatur ikon berdasarkan nama item menggunakan map, dengan ikon default jika tidak ditemukan
            holder.imgLogo.setImageResource(iconMap[itemName] ?: R.drawable.ic_account_balance)

            // Menetapkan listener klik pada item view
            holder.itemView.setOnClickListener { onClick(itemName) }
        }

        override fun getItemCount(): Int = items.size
    }

    /**
     * Memulai timer hitung mundur untuk batas waktu pembayaran (misal: 15 menit).
     */
    private fun startTimer() {
        // Mengambil TextView dari layout
        val tvTimer = findViewById<TextView>(R.id.tvTimer); object : CountDownTimer(900000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val m = millisUntilFinished / 1000 / 60;
                val s = millisUntilFinished / 1000 % 60; tvTimer.text =
                    String.format("%02d:%02d", m, s)
            };
            override fun onFinish() {
                tvTimer.text = "00:00"
            }
        }.start()
    }
}