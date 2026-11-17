package com.example.smartcafeadmin.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcafeadmin.R
import com.example.smartcafeadmin.model.Order
import java.text.SimpleDateFormat
import java.util.Locale

// Shows each order as a card with a coloured status badge
class OrderAdapter(private val onClick: (Order) -> Unit) :
    RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private var orders: List<Order> = emptyList()

    fun submit(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textOrderId: TextView = view.findViewById(R.id.textOrderId)
        val textCustomer: TextView = view.findViewById(R.id.textCustomer)
        val textStatus: TextView = view.findViewById(R.id.textStatus)
        val textItems: TextView = view.findViewById(R.id.textItems)
        val textNote: TextView = view.findViewById(R.id.textNote)
        val textDate: TextView = view.findViewById(R.id.textDate)
        val textTotal: TextView = view.findViewById(R.id.textTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]

        holder.textOrderId.text = "#${order.id}"
        holder.textCustomer.text = order.customerName
        holder.textItems.text = order.items.replace(", ", "\n")   // one item per line
        holder.textDate.text = prettyDate(order.orderDate)
        holder.textTotal.text = "£${"%.2f".format(order.total)}"

        if (order.notes.isEmpty()) {
            holder.textNote.visibility = View.GONE
        } else {
            holder.textNote.text = "📝 ${order.notes}"
            holder.textNote.visibility = View.VISIBLE
        }

        // Coloured status badge
        val (background, text) = statusColours(order.status)
        holder.textStatus.text = order.status
        holder.textStatus.setTextColor(text)
        holder.textStatus.backgroundTintList = ColorStateList.valueOf(background)

        // Collected orders are finished, so fade them slightly
        holder.itemView.alpha = if (order.status == "Collected") 0.7f else 1f

        holder.itemView.setOnClickListener { onClick(order) }
    }

    private fun statusColours(status: String): Pair<Int, Int> = when (status) {
        "Pending" -> Pair(Color.parseColor("#FFF1D6"), Color.parseColor("#8A5A00"))
        "Preparing" -> Pair(Color.parseColor("#E3EEFF"), Color.parseColor("#1E4FA3"))
        "Ready" -> Pair(Color.parseColor("#DDF3E4"), Color.parseColor("#1B6E3A"))
        else -> Pair(Color.parseColor("#ECE7E3"), Color.parseColor("#6B5E55"))
    }

    // "2026-11-03" -> "3 Nov 2026"
    private fun prettyDate(date: String): String = try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(date)
        SimpleDateFormat("d MMM yyyy", Locale.UK).format(parsed!!)
    } catch (e: Exception) {
        date
    }
}