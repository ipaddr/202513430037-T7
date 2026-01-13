package com.contoh.eticketbus2025.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.BusModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter untuk menampilkan daftar bus dalam RecyclerView.
 *
 * @param busList Daftar objek [BusModel] yang akan ditampilkan.
 * @param onBookClicked Lambda function yang akan dipanggil ketika tombol "Pesan" pada salah satu item diklik.
 *                      Fungsi ini menerima objek [BusModel] dari item yang diklik.
 */
class BusAdapter(
    private val busList: List<BusModel>,
    private val onBookClicked: (BusModel) -> Unit
) : RecyclerView.Adapter<BusAdapter.BusViewHolder>() {

    /**
     * ViewHolder untuk setiap item dalam RecyclerView.
     * Kelas ini bertanggung jawab untuk menyimpan referensi ke view (tampilan) dari setiap item.
     */
    inner class BusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClass: TextView = view.findViewById(R.id.tvClass)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvDepart: TextView = view.findViewById(R.id.tvTimeDepart)
        val tvArrive: TextView = view.findViewById(R.id.tvTimeArrive)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvSeats: TextView = view.findViewById(R.id.tvSeats)
        val btnBook: Button = view.findViewById(R.id.btnBook)
        val tvOperator: TextView = view.findViewById(R.id.tvOperatorName)

        // Layout Fasilitas (LinearLayout)
        val layoutFacilities: LinearLayout = view.findViewById(R.id.layoutFacilities)
    }

    /**
     * Dipanggil ketika RecyclerView membutuhkan ViewHolder baru.
     * Fungsi ini meng-inflate layout XML untuk satu item dan mengembalikannya sebagai [BusViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        // Membuat view baru dari layout item_bus.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus, parent, false)
        return BusViewHolder(view)
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     * Fungsi ini mengambil data dari `busList` pada `position` yang diberikan dan mengikatnya ke view di dalam `holder`.
     */
    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        // Mendapatkan objek bus pada posisi saat ini
        val bus = busList[position]
        // Mendapatkan context dari item view untuk digunakan pada resource
        val context = holder.itemView.context

        // Mengisi data dari objek 'bus' ke dalam komponen UI di ViewHolder
        holder.tvOperator.text = bus.operatorName
        holder.tvClass.text = bus.busClass
        holder.tvDepart.text = bus.departTime
        holder.tvArrive.text = bus.arriveTime
        holder.tvDuration.text = bus.duration
        holder.tvRating.text = bus.rating.toString()
        holder.tvSeats.text = "Sisa ${bus.seatAvailable} Kursi"

        // Format harga ke dalam format mata uang Rupiah (contoh: Rp150.000)
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        holder.tvPrice.text = formatRp.format(bus.price)

        // --- LOGIKA FASILITAS (CHIPS) ---
        // Menghapus semua view fasilitas yang mungkin ada dari item sebelumnya (penting untuk daur ulang view)
        holder.layoutFacilities.removeAllViews()

        bus.facilities.forEach { facilityName ->
            val tv = TextView(context)
            tv.text = facilityName

            // Style Glassmorphism
            tv.textSize = 12f
            tv.setTextColor(ContextCompat.getColor(context, R.color.white_dim)) // Atur warna teks
            tv.setBackgroundResource(R.drawable.bg_facility_chip) // Atur background custom

            // Padding (Convert DP to PX manual sederhana)
            val scale = context.resources.displayMetrics.density // Dapatkan kepadatan layar
            val paddingH = (12 * scale + 0.5f).toInt() // Konversi 12dp ke pixel
            val paddingV = (6 * scale + 0.5f).toInt()  // Konversi 6dp ke pixel
            tv.setPadding(paddingH, paddingV, paddingH, paddingV) // Terapkan padding

            // Margin antar item
            val params = LinearLayout.LayoutParams( // Buat parameter layout baru
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val marginEnd = (8 * scale + 0.5f).toInt()
            params.marginEnd = marginEnd
            tv.layoutParams = params

            // Menambahkan TextView fasilitas yang sudah dibuat ke dalam LinearLayout
            holder.layoutFacilities.addView(tv)
        }

        // Menetapkan listener klik untuk tombol "Pesan"
        holder.btnBook.setOnClickListener {
            // Memanggil lambda 'onBookClicked' yang telah diberikan saat adapter dibuat,
            // sambil mengirimkan data bus dari item yang diklik.
            onBookClicked(bus)
        }
    }

    /**
     * Mengembalikan jumlah total item dalam dataset yang dipegang oleh adapter.
     */
    override fun getItemCount() = busList.size
}