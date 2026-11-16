package com.example.smartcafeadmin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Shows each piece of feedback as a card
class FeedbackAdapter : RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

    private var items: List<Feedback> = emptyList()

    fun submit(newItems: List<Feedback>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textStars: TextView = view.findViewById(R.id.textStars)
        val textDate: TextView = view.findViewById(R.id.textDate)
        val textName: TextView = view.findViewById(R.id.textName)
        val textComment: TextView = view.findViewById(R.id.textComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feedback, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val f = items[position]
        holder.textStars.text = FeedbackRules.stars(f.rating)
        holder.textDate.text = f.date
        holder.textName.text = "${f.customerName}  ·  Order #${f.orderId}"
        holder.textComment.text = if (f.comment.isEmpty()) "(no comment)" else "“${f.comment}”"
    }
}