package com.contoh.eticketbus2025.ui.ticket

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.TicketHistoryModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter untuk menampilkan daftar riwayat tiket dalam RecyclerView.
 * Adapter ini bertanggung jawab untuk mengikat data dari `TicketHistoryModel` ke tampilan item (layout `item_ticket_history.xml`).
 *
 * @param ticketList Daftar objek `TicketHistoryModel` yang akan ditampilkan.
 */
class TicketHistoryAdapter(
    private val ticketList: List<TicketHistoryModel>
) : RecyclerView.Adapter<TicketHistoryAdapter.ViewHolder>() {

    /**
     * ViewHolder menyimpan referensi ke view (tampilan) dari setiap item dalam daftar.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOperator: TextView = view.findViewById(R.id.tvOperatorName)
        val tvClass: TextView = view.findViewById(R.id.tvClass)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvRoute: TextView = view.findViewById(R.id.tvRoute)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvId: TextView = view.findViewById(R.id.tvBookingId)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
    }

    /**
     * Dipanggil saat RecyclerView membutuhkan ViewHolder baru.
     * Method ini membuat (inflates) layout item dan mengembalikannya dalam sebuah ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_ticket_history, parent, false)
        return ViewHolder(view)
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     * Method ini mengisi data dari model (`ticket`) ke dalam view yang ada di `holder`.
     * @param holder ViewHolder yang akan diperbarui untuk mewakili item pada posisi yang diberikan.
     * @param position Posisi item dalam dataset adapter.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = ticketList[position]
        val context = holder.itemView.context

        holder.tvOperator.text = ticket.operatorName

        // LOGIKA TAMPILAN PULANG-PERGI
        if (ticket.isRoundTrip) { // Cek apakah tiket ini untuk perjalanan pulang-pergi
            // Jika PP, tampilkan panah dua arah dan label PP
            holder.tvRoute.text = "${ticket.origin} ⇄ ${ticket.destination}"
            holder.tvClass.text = "${ticket.busClass} (Pulang-Pergi)"
        } else {
            // Jika Sekali Jalan, panah satu arah
            holder.tvRoute.text = "${ticket.origin} → ${ticket.destination}"
            holder.tvClass.text = ticket.busClass
        }

        holder.tvDate.text = "${ticket.date} • ${ticket.time}"
        holder.tvId.text = "ID: ${ticket.bookingId}"

        // Format harga ke dalam format mata uang Rupiah (Rp)
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        holder.tvPrice.text = formatRp.format(ticket.price)

        // Styling Status Chip
        holder.tvStatus.text = ticket.status // Mengatur teks status (misal: "Aktif", "Kadaluarsa")
        if (ticket.status == "Aktif") {
            holder.tvStatus.setTextColor(Color.parseColor("#10B981")) // Hijau
            holder.tvStatus.background.setTint(Color.parseColor("#2010B981"))
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white_dim)) // Abu
            holder.tvStatus.background.setTint(ContextCompat.getColor(context, R.color.white_glass))
        }

        // Menambahkan listener klik pada seluruh item view (card)
        holder.itemView.setOnClickListener {
            // Membuat Intent untuk membuka ETicketDetailActivity
            val intent = Intent(context, ETicketDetailActivity::class.java)
            // Kirim objek TicketHistoryModel lengkap (berisi data pergi & pulang)
            // Objek ini akan digunakan di activity detail untuk menampilkan informasi lengkap tiket.
            intent.putExtra("TICKET_HISTORY", ticket)
            context.startActivity(intent)
        }
    }

    /**
     * Mengembalikan jumlah total item dalam dataset yang dipegang oleh adapter.
     */
    override fun getItemCount() = ticketList.size
}