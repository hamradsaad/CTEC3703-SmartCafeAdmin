package com.example.smartcafeadmin.model

data class MenuItem(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val available: Boolean,
    val description: String = "",
    val imagePath: String = ""
)