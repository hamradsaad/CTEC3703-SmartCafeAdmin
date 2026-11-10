package com.example.smartcafeadmin

data class OrderItem(
    val itemName: String,
    val quantity: Int,
    val price: Double,
    val id: Int = 0,
    val orderId: Int = 0
)