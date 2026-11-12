package com.example.smartcafeadmin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsInsightsTest {

    private val sales = listOf(
        ItemSales("Latte", 5, 17.5),
        ItemSales("Tiramisu", 0, 0.0),
        ItemSales("Panini", 9, 58.5)
    )

    private val days = listOf(
        DaySales("2026-11-01", 3, 20.0),
        DaySales("2026-11-02", 7, 45.0),
        DaySales("2026-11-03", 2, 15.0)
    )

    @Test
    fun noOrdersGivesSingleMessage() {
        val result = AnalyticsCalculator.insights(sales, emptyList(), 0)
        assertEquals(listOf("No orders yet. Insights will appear once orders are placed."), result)
    }

    @Test
    fun insightsNameTheBestSeller() {
        val result = AnalyticsCalculator.insights(sales, days, 12)
        assertTrue(result.any { it.startsWith("Panini is the best seller") })
    }

    @Test
    fun insightsListItemsNeverOrdered() {
        val result = AnalyticsCalculator.insights(sales, days, 12)
        assertTrue(result.any { it.contains("Not ordered yet: Tiramisu") })
    }

    @Test
    fun busiestDayIsTheOneWithMostOrders() {
        assertEquals("2026-11-02", AnalyticsCalculator.busiestDay(days)?.date)
    }

    @Test
    fun busiestDayWithNoDataIsNull() {
        assertNull(AnalyticsCalculator.busiestDay(emptyList()))
    }

    @Test
    fun leastSellersStartWithLowestQuantity() {
        val least = AnalyticsCalculator.leastSellers(sales, 2)
        assertEquals(listOf("Tiramisu", "Latte"), least.map { it.name })
    }

    @Test
    fun forecastWithNoDaysIsZero() {
        assertEquals(0, AnalyticsCalculator.forecastNextDayOrders(emptyList()))
    }
}