package com.example.smartcafeadmin.logic

import kotlin.math.abs
import kotlin.math.roundToInt

data class ItemSales(val name: String, val quantity: Int, val revenue: Double)
data class DaySales(val date: String, val orders: Int, val revenue: Double)

// Pure calculation logic (no Android code), so it can be unit tested
object AnalyticsCalculator {

    fun averageOrderValue(totalRevenue: Double, totalOrders: Int): Double {
        if (totalOrders <= 0) return 0.0
        return Math.round(totalRevenue / totalOrders * 100) / 100.0
    }

    fun bestSellers(sales: List<ItemSales>, count: Int = 3): List<ItemSales> =
        sales.filter { it.quantity > 0 }.sortedByDescending { it.quantity }.take(count)

    fun leastSellers(sales: List<ItemSales>, count: Int = 3): List<ItemSales> =
        sales.sortedBy { it.quantity }.take(count)

    fun busiestDay(days: List<DaySales>): DaySales? = days.maxByOrNull { it.orders }

    // Simple estimate: average number of orders over the last 3 trading days
    fun forecastNextDayOrders(days: List<DaySales>): Int {
        if (days.isEmpty()) return 0
        return days.takeLast(3).map { it.orders }.average().roundToInt()
    }

    // % change of the latest day's revenue compared with the average of earlier days
    fun revenueTrendPercent(days: List<DaySales>): Int? {
        if (days.size < 2) return null
        val previousAverage = days.dropLast(1).map { it.revenue }.average()
        if (previousAverage <= 0) return null
        return ((days.last().revenue - previousAverage) / previousAverage * 100).roundToInt()
    }

    fun insights(sales: List<ItemSales>, days: List<DaySales>, totalOrders: Int): List<String> {
        if (totalOrders == 0) return listOf("No orders yet. Insights will appear once orders are placed.")

        val result = mutableListOf<String>()

        bestSellers(sales, 1).firstOrNull()?.let {
            result.add("${it.name} is the best seller (${it.quantity} sold). Keep it in stock.")
        }

        val notOrdered = sales.filter { it.quantity == 0 }
        if (notOrdered.isNotEmpty()) {
            result.add("Not ordered yet: ${notOrdered.joinToString { it.name }}. Consider promoting or reviewing these items.")
        }

        busiestDay(days)?.let {
            result.add("Busiest day: ${it.date} with ${it.orders} orders.")
        }

        revenueTrendPercent(days)?.let { pct ->
            val direction = if (pct >= 0) "up" else "down"
            result.add("Latest day's revenue is $direction ${abs(pct)}% compared with the average of earlier days.")
        }

        result.add("Estimated orders for the next day: ${forecastNextDayOrders(days)} (average of the last 3 trading days).")
        return result
    }
}
