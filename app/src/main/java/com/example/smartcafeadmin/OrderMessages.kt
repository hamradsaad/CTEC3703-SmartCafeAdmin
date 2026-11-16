package com.example.smartcafeadmin

// Notification wording (no Android code, so it can be unit tested)
object OrderMessages {

    fun statusTitle(orderId: Int): String = "Order #$orderId updated"

    fun statusText(customerName: String, status: String): String = when (status) {
        "Preparing" -> "$customerName's order is being prepared"
        "Ready" -> "$customerName's order is ready for collection"
        "Collected" -> "$customerName's order has been collected"
        else -> "$customerName's order is now $status"
    }

    fun newOrderTitle(orderId: Int): String = "New order #$orderId"

    fun newOrderText(customerName: String, items: String): String = "$customerName ordered $items"
}