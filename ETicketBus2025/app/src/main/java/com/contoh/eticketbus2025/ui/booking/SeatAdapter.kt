package com.contoh.eticketbus2025.ui.booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R

class SeatAdapter(
    private val totalRows: Int = 10, // Jumlah baris kursi, default 10
    private val occupiedSeats: List<String>, // Daftar kursi yang sudah terisi
    private val maxSelection: Int, // Jumlah maksimal kursi yang bisa dipilih
    private val onSelectionChanged: (List<String>) -> Unit // Fungsi callback saat pilihan kursi berubah
) : RecyclerView.Adapter<SeatAdapter.SeatViewHolder>() {

    // Menyimpan daftar kursi yang sedang dipilih oleh pengguna
    private val selectedSeats = mutableListOf<String>()
    // Menghasilkan dan menyimpan seluruh layout kursi (termasuk lorong) berdasarkan jumlah baris
    private val seats = generateLayout2_2(totalRows)

    // Konstanta untuk membedakan tipe item dalam RecyclerView (kursi atau lorong)
    // Walaupun tidak digunakan di onCreateViewHolder saat ini, ini adalah praktik yang baik
    private val TYPE_SEAT = 1
    private val TYPE_AISLE = 0

    /**
     * ViewHolder untuk setiap item kursi.
     * Menghubungkan view dari layout XML (item_seat.xml) ke kode.
     */
    inner class SeatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val seatView: TextView = view.findViewById(R.id.itemSeat)
    }

    /**
     * Membuat ViewHolder baru setiap kali RecyclerView membutuhkannya.
     * Ini akan meng-inflate layout 'item_seat.xml'.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_seat, parent, false)
        return SeatViewHolder(view)
    }

    /**
     * Menghubungkan data dengan ViewHolder untuk posisi tertentu.
     * Metode ini dipanggil oleh RecyclerView untuk menampilkan data pada posisi yang ditentukan.
     */
    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        // Mengambil nomor kursi atau penanda "AISLE" dari daftar `seats`
        val seatNumber = seats[position]
        val context = holder.itemView.context

        // --- LOGIKA UNTUK LORONG (AISLE) ---
        // Jika item ini adalah lorong, kita buat tidak terlihat dan tidak bisa diklik.
        if (seatNumber == "AISLE") {
            holder.seatView.visibility = View.INVISIBLE // Sembunyikan view
            holder.seatView.isClickable = false
            holder.seatView.text = ""
            holder.seatView.background = null
            return // Hentikan eksekusi lebih lanjut untuk item ini
        } else {
            // Jika bukan lorong, pastikan view terlihat dan tampilkan nomor kursinya.
            holder.seatView.visibility = View.VISIBLE
            holder.seatView.text = seatNumber
        }

        // --- LOGIKA UNTUK KURSI BIASA ---

        // Mengatur tampilan kursi berdasarkan statusnya (terisi, dipilih, atau tersedia).
        when {
            // Kasus 1: Kursi sudah terisi (dari data `occupiedSeats`)
            occupiedSeats.contains(seatNumber) -> {
                holder.seatView.setBackgroundResource(R.drawable.bg_seat_filled)
                holder.seatView.setTextColor(ContextCompat.getColor(context, R.color.white_dim))
                holder.seatView.isEnabled = false // Tidak bisa diklik
            }
            // Kasus 2: Kursi sedang dipilih oleh pengguna
            selectedSeats.contains(seatNumber) -> {
                holder.seatView.setBackgroundResource(R.drawable.bg_seat_selected)
                holder.seatView.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.seatView.isEnabled = true // Bisa diklik untuk membatalkan
            }
            // Kasus 3: Kursi tersedia
            else -> {
                holder.seatView.setBackgroundResource(R.drawable.bg_seat_available)
                holder.seatView.setTextColor(ContextCompat.getColor(context, R.color.black_text))
                holder.seatView.isEnabled = true // Bisa diklik untuk memilih
            }
        }

        // Menangani aksi klik pada kursi.
        holder.seatView.setOnClickListener {
            // Jika kursi sudah dipilih, batalkan pilihan (hapus dari daftar).
            if (selectedSeats.contains(seatNumber)) {
                selectedSeats.remove(seatNumber)
            } else {
                // Jika belum dipilih, periksa apakah masih bisa menambah pilihan.
                if (selectedSeats.size < maxSelection) {
                    // Jika belum mencapai batas maksimal, tambahkan kursi ke daftar pilihan.
                    selectedSeats.add(seatNumber)
                } else {
                    // Jika sudah mencapai batas, tampilkan pesan peringatan.
                    Toast.makeText(context, "Maksimal $maxSelection kursi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Hentikan aksi klik
                }
            }
            // Memberi tahu adapter bahwa item pada posisi ini telah berubah,
            // agar onBindViewHolder dipanggil lagi untuk menggambar ulang tampilannya.
            notifyItemChanged(position)
            // Memanggil fungsi callback untuk memberi tahu Activity/Fragment tentang perubahan daftar kursi yang dipilih.
            onSelectionChanged(selectedSeats)
        }
    }

    /**
     * Mengembalikan jumlah total item dalam set data yang dipegang oleh adapter.
     * Dalam kasus ini, jumlah total kursi ditambah jumlah lorong.
     */
    override fun getItemCount() = seats.size

    // === GENERATOR LAYOUT 2-2 DENGAN LORONG ===
    // Pola: [A] [B] [LORONG] [C] [D]
    private fun generateLayout2_2(rows: Int): List<String> {
        val list = mutableListOf<String>()
        for (i in 1..rows) {
            list.add("${i}A")
            list.add("${i}B")
            list.add("AISLE") // Lorong Tengah
            list.add("${i}C")
            list.add("${i}D")
        }
        // Tambahan: Baris paling belakang biasanya 5 kursi (A B C D E) tanpa lorong?
        // Untuk simpelnya kita buat seragam dulu 2-2.
        return list
    }
}