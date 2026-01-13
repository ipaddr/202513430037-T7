package com.contoh.eticketbus2025.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.data.model.PromoModel
import com.contoh.eticketbus2025.R

/**
 * Adapter untuk RecyclerView yang menampilkan daftar promo.
 * @param promoList Daftar data promo yang akan ditampilkan.
 */
class PromoAdapter(
    private val promoList: List<PromoModel>
) : RecyclerView.Adapter<PromoAdapter.ViewHolder>() {

    /**
     * ViewHolder untuk menampung view dari setiap item promo.
     * Kelas ini menginisialisasi semua komponen UI yang ada di layout item_promo.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Inisialisasi komponen UI dari layout item_promo.xml
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvDesc)
        val tvCode: TextView = view.findViewById(R.id.tvCode)
        val tvDiscount: TextView = view.findViewById(R.id.tvDiscount)
        val tvMinPurchase: TextView = view.findViewById(R.id.tvMinPurchase)
        val tvValidUntil: TextView = view.findViewById(R.id.tvValidUntil)
        val imgIcon: ImageView = view.findViewById(R.id.imgIcon)
        val btnCopy: Button = view.findViewById(R.id.btnCopy)
    }

    /**
     * Dipanggil saat RecyclerView membutuhkan ViewHolder baru untuk dibuat.
     * Method ini membuat dan menginisialisasi ViewHolder beserta View-nya.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Membuat view baru dari layout item_promo.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_promo, parent, false)
        return ViewHolder(view)
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     * Method ini mengisi data dari model promo ke dalam komponen UI di ViewHolder.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val promo = promoList[position] // Mendapatkan data promo pada posisi saat ini
        val context = holder.itemView.context

        holder.tvTitle.text = promo.title
        holder.tvDesc.text = promo.description
        holder.tvCode.text = promo.code
        holder.tvDiscount.text = promo.discount
        holder.tvMinPurchase.text = promo.minPurchase
        holder.tvValidUntil.text = promo.validUntil
        holder.imgIcon.setImageResource(promo.iconRes)

        // Fitur Salin Kode
        // Menambahkan listener klik pada tombol salin
        holder.btnCopy.setOnClickListener {
            // Mendapatkan layanan clipboard dari sistem
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // Membuat objek ClipData yang berisi kode promo
            val clip = ClipData.newPlainText("Promo Code", promo.code)
            // Menyimpan ClipData ke clipboard
            clipboard.setPrimaryClip(clip)
            // Menampilkan pesan singkat (Toast) bahwa kode berhasil disalin
            Toast.makeText(context, "Kode ${promo.code} disalin!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Mengembalikan jumlah total item dalam daftar promo.
     */
    override fun getItemCount() = promoList.size
}