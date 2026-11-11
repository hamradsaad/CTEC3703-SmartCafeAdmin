package com.example.smartcafeadmin

// Business rules for order status changes (no Android code, so it can be unit tested)
object OrderRules {

    val STATUSES = listOf("Pending", "Preparing", "Ready", "Collected")

    // Orders can only move forward one step at a time
    fun allowedNextStatuses(current: String): List<String> = when (current) {
        "Pending" -> listOf("Preparing")
        "Preparing" -> listOf("Ready")
        "Ready" -> listOf("Collected")
        else -> emptyList()   // Collected (or unknown) cannot change
    }

    fun canChangeStatus(from: String, to: String): Boolean =
        to in allowedNextStatuses(from)
}