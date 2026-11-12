package com.example.smartcafeadmin

// Fixed list of menu categories (CR-02)
object Categories {
    val ALL = listOf("Hot Drinks", "Cold Drinks", "Food", "Snacks", "Desserts")

    // Shown when the item has no picture yet
    fun emojiFor(category: String): String = when (category) {
        "Hot Drinks" -> "☕"
        "Cold Drinks" -> "🧃"
        "Food" -> "🥪"
        "Snacks" -> "🍪"
        "Desserts" -> "🍰"
        else -> "🍽️"
    }
}