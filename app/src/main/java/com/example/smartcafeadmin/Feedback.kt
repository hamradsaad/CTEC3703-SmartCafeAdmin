package com.example.smartcafeadmin

data class Feedback(
    val id: Int,
    val orderId: Int,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val date: String
)