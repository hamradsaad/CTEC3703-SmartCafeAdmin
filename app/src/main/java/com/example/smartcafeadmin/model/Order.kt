package com.example.smartcafeadmin.model

data class Order(
    val id: Int,
    val customerName: String,
    val items: String,
    val total: Double,
    val status: String,
    val orderDate: String,
    val notes: String = ""
)