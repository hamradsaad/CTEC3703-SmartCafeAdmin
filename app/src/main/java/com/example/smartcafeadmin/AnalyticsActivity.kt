package com.example.smartcafeadmin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var repo: AnalyticsRepository
    private lateinit var textSummary: TextView
    private lateinit var textStatus: TextView
    private lateinit var textBestWorst: TextView
    private lateinit var textInsights: TextView
    private lateinit var chartRevenue: BarChartView
    private lateinit var chartItems: BarChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthGuard.isSignedIn(this)) return
        enableEdgeToEdge()
        setContentView(R.layout.activity_analytics)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repo = AnalyticsRepository(this)
        textSummary = findViewById(R.id.textSummary)
        textStatus = findViewById(R.id.textStatus)
        textBestWorst = findViewById(R.id.textBestWorst)
        textInsights = findViewById(R.id.textInsights)
        chartRevenue = findViewById(R.id.chartRevenue)
        chartItems = findViewById(R.id.chartItems)

        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            finish()
        }
    }

    // Reload every time the screen opens so new orders are included
    override fun onResume() {
        super.onResume()
        loadAnalytics()
    }

    private fun loadAnalytics() {
        val totalOrders = repo.getTotalOrders()
        val totalRevenue = repo.getTotalRevenue()
        val sales = repo.getItemSales()
        val days = repo.getDailySales()
        val statusCounts = repo.getStatusCounts()
        val average = AnalyticsCalculator.averageOrderValue(totalRevenue, totalOrders)

        textSummary.text = "Total orders: $totalOrders\n" +
                "Total revenue: £${"%.2f".format(totalRevenue)}\n" +
                "Average order value: £${"%.2f".format(average)}"

        textStatus.text = listOf("Pending", "Preparing", "Ready", "Collected")
            .joinToString("\n") { "$it: ${statusCounts[it] ?: 0}" }

        // Revenue per day (last 7 trading days)
        val lastDays = days.takeLast(7)
        chartRevenue.setData(
            lastDays.map { shortDate(it.date) },
            lastDays.map { it.revenue }
        ) { "£%.2f".format(it) }

        // Top 5 items by quantity sold
        val top = AnalyticsCalculator.bestSellers(sales, 5)
        chartItems.setData(
            top.map { shortName(it.name) },
            top.map { it.quantity.toDouble() }
        ) { it.toInt().toString() }

        val best = AnalyticsCalculator.bestSellers(sales, 3)
        val least = AnalyticsCalculator.leastSellers(sales, 3)
        val bestText = if (best.isEmpty()) "None yet" else best.joinToString("\n") {
            "• ${it.name}: ${it.quantity} sold (£${"%.2f".format(it.revenue)})"
        }
        val leastText = if (least.isEmpty()) "None yet" else least.joinToString("\n") {
            "• ${it.name}: ${it.quantity} sold"
        }
        textBestWorst.text = "Best sellers:\n$bestText\n\nLeast sellers:\n$leastText"

        textInsights.text = AnalyticsCalculator.insights(sales, days, totalOrders)
            .joinToString("\n\n") { "• $it" }
    }

    // "2026-11-03" becomes "03/11"
    private fun shortDate(date: String): String {
        val parts = date.split("-")
        return if (parts.size == 3) "${parts[2]}/${parts[1]}" else date
    }

    private fun shortName(name: String): String =
        if (name.length > 9) name.take(8) + "…" else name
}

