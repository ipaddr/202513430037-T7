package com.contoh.eticketbus2025.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.NotificationEntity

/**
 * Adapter untuk menampilkan daftar notifikasi dalam sebuah RecyclerView.
 *
 * @property list Daftar data notifikasi yang akan ditampilkan.
 * @property onClick Lambda function yang akan dipanggil ketika sebuah item notifikasi di-klik.
 */
class NotificationAdapter(
    private val list: List<NotificationEntity>,
    private val onClick: (NotificationEntity) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    /**
     * ViewHolder menyimpan referensi ke view dari setiap item dalam RecyclerView.
     * Ini meningkatkan performa dengan menghindari pemanggilan `findViewById` yang berulang.
     * @param view Layout untuk satu item notifikasi.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Inisialisasi komponen UI dari layout item_notification.xml
        val title: TextView = view.findViewById(R.id.tvTitle)
        val message: TextView = view.findViewById(R.id.tvMessage)
        val date: TextView = view.findViewById(R.id.tvDate)
        val icon: ImageView = view.findViewById(R.id.imgIcon)
        val badge: View = view.findViewById(R.id.badgeUnread)
    }

    /**
     * Dipanggil ketika RecyclerView membutuhkan ViewHolder baru untuk dibuat.
     * Method ini membuat (inflates) layout `item_notification.xml` dan mengembalikannya
     * sebagai instance dari ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     * Method ini mengambil data dari `list` berdasarkan `position` dan mengikatnya
     * ke dalam view yang ada di dalam `holder`.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Mengambil item data notifikasi pada posisi saat ini
        val item = list[position]

        // Mengisi data dari item ke dalam komponen UI di ViewHolder
        holder.title.text = item.title
        holder.message.text = item.message
        holder.date.text = item.date

        // Menentukan ikon yang akan ditampilkan berdasarkan tipe notifikasi (PROMO, TRANSACTION, dll.)
        when(item.type) {
            "PROMO" -> holder.icon.setImageResource(android.R.drawable.ic_menu_my_calendar) // Ganti icon kado jika ada
            "TRANSACTION" -> holder.icon.setImageResource(android.R.drawable.ic_menu_save)
            else -> holder.icon.setImageResource(android.R.drawable.ic_popup_reminder)
        }

        // Mengatur tampilan berdasarkan status `isRead` (sudah dibaca atau belum)
        if (item.isRead) {
            // Jika sudah dibaca, sembunyikan badge "belum dibaca" dan buat teks judul sedikit transparan
            holder.badge.visibility = View.GONE
            holder.title.alpha = 0.7f
        } else {
            // Jika belum dibaca, tampilkan badge dan buat teks judul solid
            holder.badge.visibility = View.VISIBLE
            holder.title.alpha = 1.0f
        }

        // Menambahkan OnClickListener pada seluruh tampilan item.
        // Ketika item diklik, panggil lambda `onClick` dengan membawa data `item` yang sesuai.
        holder.itemView.setOnClickListener { onClick(item) }
    }

    /**
     * Mengembalikan jumlah total item dalam dataset yang dipegang oleh adapter.
     */
    override fun getItemCount() = list.size
}