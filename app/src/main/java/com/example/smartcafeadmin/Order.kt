package com.example.smartcafeadmin

data class Order(
    val id: Int,
    val customerName: String,
    val items: String,
    val total: Double,
    val status: String,
    val orderDate: String
)