package com.example.smartcafeadmin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Shows each menu item as a card (CR-01)
class MenuAdapter(private val onClick: (MenuItem) -> Unit) :
    RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    private var items: List<MenuItem> = emptyList()

    fun submit(newItems: List<MenuItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textEmoji: TextView = view.findViewById(R.id.textEmoji)
        val imageItem: ImageView = view.findViewById(R.id.imageItem)
        val textName: TextView = view.findViewById(R.id.textName)
        val textCategoryPrice: TextView = view.findViewById(R.id.textCategoryPrice)
        val textDescription: TextView = view.findViewById(R.id.textDescription)
        val textAvailability: TextView = view.findViewById(R.id.textAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.textName.text = item.name
        holder.textCategoryPrice.text = "${item.category}  •  £${"%.2f".format(item.price)}"
        holder.textDescription.text = item.description
        holder.textDescription.visibility = if (item.description.isEmpty()) View.GONE else View.VISIBLE
        holder.textEmoji.text = Categories.emojiFor(item.category)

        val bitmap = ImageStorage.loadThumbnail(item.imagePath, 200)
        if (bitmap != null) {
            holder.imageItem.setImageBitmap(bitmap)
            holder.imageItem.visibility = View.VISIBLE
        } else {
            holder.imageItem.setImageDrawable(null)
            holder.imageItem.visibility = View.GONE
        }

        if (item.available) {
            holder.textAvailability.text = "Available"
            holder.textAvailability.setTextColor(Color.parseColor("#1B5E20"))
            holder.itemView.alpha = 1f
        } else {
            holder.textAvailability.text = "Sold out"
            holder.textAvailability.setTextColor(Color.parseColor("#B3261E"))
            holder.itemView.alpha = 0.6f
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }
}