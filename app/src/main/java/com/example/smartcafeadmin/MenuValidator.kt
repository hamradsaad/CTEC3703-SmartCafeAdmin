package com.example.smartcafeadmin

// Validation rules for menu items (no Android code, so it can be unit tested)
object MenuValidator {

    const val MAX_PRICE = 100.0

    // Returns an error message, or null if the input is valid
    fun validate(name: String, category: String, priceText: String): String? {
        if (name.isBlank()) return "Please enter an item name"
        if (category.isBlank()) return "Please choose a category"
        val price = priceText.toDoubleOrNull() ?: return "Please enter a valid price, e.g. 3.50"
        if (price <= 0) return "Price must be more than £0"
        if (price > MAX_PRICE) return "Price must be £100 or less"
        return null
    }
}