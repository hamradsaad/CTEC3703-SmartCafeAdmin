package com.example.smartcafeadmin.model

data class Feedback(
    val id: Int,
    val orderId: Int,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val date: String
)