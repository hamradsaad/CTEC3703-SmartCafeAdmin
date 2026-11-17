package com.example.smartcafeadmin.logic

import kotlin.random.Random

// Rating rules and the simulated customer feedback (no Android code, so it can be unit tested)
object FeedbackRules {

    const val MAX_COMMENT_LENGTH = 300

    fun isValidRating(rating: Int): Boolean = rating in 1..5

    // Returns an error message, or null if the feedback is valid
    fun validate(rating: Int, comment: String): String? = when {
        !isValidRating(rating) -> "Rating must be between 1 and 5"
        comment.length > MAX_COMMENT_LENGTH -> "Comment is too long"
        else -> null
    }

    // 4 -> "★★★★☆"
    fun stars(rating: Int): String {
        val r = rating.coerceIn(0, 5)
        return "★".repeat(r) + "☆".repeat(5 - r)
    }

    // ---------- Simulated customers (fictional) ----------

    private val comments = mapOf(
        5 to listOf("Amazing coffee, will be back!", "Lovely staff and really quick service", "Best latte in town", "Perfect, exactly how I like it"),
        4 to listOf("Really good, just a short wait", "Great drink, nice atmosphere", "Tasty food, slightly pricey"),
        3 to listOf("It was okay", "Decent but a bit slow today", "Drink was fine, nothing special"),
        2 to listOf("Order took too long", "Drink was lukewarm", "They forgot my extra shot"),
        1 to listOf("Wrong order and cold food", "Very slow and unfriendly service")
    )

    // Mostly positive, like a real café
    fun simulatedRating(random: Random = Random): Int =
        listOf(5, 5, 5, 4, 4, 4, 3, 3, 2, 1).random(random)

    fun simulatedComment(rating: Int, random: Random = Random): String =
        comments[rating]?.random(random) ?: ""
}