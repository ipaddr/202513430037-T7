package com.contoh.eticketbus2025.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R

/**
 * Adapter untuk menampilkan daftar operator bus dalam RecyclerView.
 * Adapter ini bertanggung jawab untuk mengambil data (dalam hal ini, daftar nama operator)
 * dan menampilkannya sebagai item individual dalam RecyclerView.
 *
 * @param operators List<String> yang berisi nama-nama operator bus.
 */
class OperatorAdapter(
    private val operators: List<String>
) : RecyclerView.Adapter<OperatorAdapter.ViewHolder>() {

    /**
     * ViewHolder memegang referensi ke view untuk setiap item dalam RecyclerView.
     * Ini meningkatkan performa dengan menghindari pemanggilan `findViewById()` yang berulang.
     *
     * @param view View untuk satu item operator.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // TextView untuk menampilkan nama operator.
        val tvName: TextView = view.findViewById(R.id.tvOperatorName)
    }

    /**
     * Dipanggil ketika RecyclerView membutuhkan ViewHolder baru untuk menampilkan item.
     * Di sini, kita meng-inflate layout XML (item_operator_home.xml) untuk satu item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_operator_home, parent, false)
        return ViewHolder(view)
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     * Metode ini mengisi data dari `operators` ke dalam view yang dipegang oleh `ViewHolder`.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val operatorName = operators[position]
        holder.tvName.text = operatorName

        // Opsional: Klik operator untuk filter (Nanti bisa dikembangkan)
        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Operator: $operatorName", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Mengembalikan jumlah total item dalam dataset yang dipegang oleh adapter.
     */
    override fun getItemCount() = operators.size
}