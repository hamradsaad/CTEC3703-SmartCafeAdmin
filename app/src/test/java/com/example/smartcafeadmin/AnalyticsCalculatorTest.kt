package com.example.smartcafeadmin

import com.example.smartcafeadmin.logic.AnalyticsCalculator
import com.example.smartcafeadmin.logic.DaySales
import com.example.smartcafeadmin.logic.ItemSales
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnalyticsCalculatorTest {

    @Test
    fun averageOrderValueIsCalculated() {
        assertEquals(7.5, AnalyticsCalculator.averageOrderValue(30.0, 4), 0.001)
    }

    @Test
    fun averageOrderValueWithNoOrdersIsZero() {
        assertEquals(0.0, AnalyticsCalculator.averageOrderValue(0.0, 0), 0.001)
    }

    @Test
    fun bestSellersAreSortedAndSkipUnsoldItems() {
        val sales = listOf(
            ItemSales("Latte", 5, 17.5),
            ItemSales("Tiramisu", 0, 0.0),
            ItemSales("Panini", 9, 58.5)
        )
        val best = AnalyticsCalculator.bestSellers(sales, 3)
        assertEquals(listOf("Panini", "Latte"), best.map { it.name })
    }

    @Test
    fun forecastUsesAverageOfLastThreeDays() {
        val days = listOf(
            DaySales("2026-11-01", 2, 10.0),
            DaySales("2026-11-02", 4, 20.0),
            DaySales("2026-11-03", 6, 30.0),
            DaySales("2026-11-04", 8, 40.0)
        )
        assertEquals(6, AnalyticsCalculator.forecastNextDayOrders(days))   // (4 + 6 + 8) / 3
    }

    @Test
    fun revenueTrendComparesLatestDayWithEarlierAverage() {
        val days = listOf(
            DaySales("2026-11-01", 1, 10.0),
            DaySales("2026-11-02", 1, 10.0),
            DaySales("2026-11-03", 1, 15.0)
        )
        assertEquals(50, AnalyticsCalculator.revenueTrendPercent(days))   // 15 vs average 10
    }

    @Test
    fun revenueTrendNeedsAtLeastTwoDays() {
        assertNull(AnalyticsCalculator.revenueTrendPercent(listOf(DaySales("2026-11-01", 1, 10.0))))
    }
}