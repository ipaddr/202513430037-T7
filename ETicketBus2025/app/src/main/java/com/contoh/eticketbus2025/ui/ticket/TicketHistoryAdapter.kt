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

class TicketHistoryAdapter(
    private val ticketList: List<TicketHistoryModel>
) : RecyclerView.Adapter<TicketHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOperator: TextView = view.findViewById(R.id.tvOperatorName)
        val tvClass: TextView = view.findViewById(R.id.tvClass)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvRoute: TextView = view.findViewById(R.id.tvRoute)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvId: TextView = view.findViewById(R.id.tvBookingId)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = ticketList[position]
        val context = holder.itemView.context

        holder.tvOperator.text = ticket.operatorName

        // PERBAIKAN: Gunakan .roundTrip
        if (ticket.roundTrip) {
            holder.tvRoute.text = "${ticket.origin} ⇄ ${ticket.destination}"
            holder.tvClass.text = "${ticket.busClass} (PP)"
        } else {
            holder.tvRoute.text = "${ticket.origin} → ${ticket.destination}"
            holder.tvClass.text = ticket.busClass
        }

        holder.tvDate.text = "${ticket.date} • ${ticket.time}"
        holder.tvId.text = "ID: ${ticket.bookingId}"

        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        holder.tvPrice.text = formatRp.format(ticket.price)

        holder.tvStatus.text = ticket.status
        if (ticket.status.equals("Aktif", ignoreCase = true)) {
            holder.tvStatus.setTextColor(Color.parseColor("#10B981"))
            holder.tvStatus.background.setTint(Color.parseColor("#2010B981"))
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white_dim))
            holder.tvStatus.background.setTint(ContextCompat.getColor(context, R.color.white_glass))
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ETicketDetailActivity::class.java)
            intent.putExtra("TICKET_DATA", ticket)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = ticketList.size
}