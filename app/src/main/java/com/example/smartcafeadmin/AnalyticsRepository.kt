package com.example.smartcafeadmin

import android.content.Context

// Runs the SQL queries used by the analytics dashboard
class AnalyticsRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun getTotalOrders(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Orders", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun getTotalRevenue(): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COALESCE(SUM(total), 0) FROM Orders", null)
        cursor.moveToFirst()
        val revenue = cursor.getDouble(0)
        cursor.close()
        db.close()
        return revenue
    }

    // Every menu item with how many have been sold (0 if never ordered)
    fun getItemSales(): List<ItemSales> {
        val sales = mutableListOf<ItemSales>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT m.name, COALESCE(SUM(oi.quantity), 0), COALESCE(SUM(oi.quantity * oi.price), 0) " +
                    "FROM MenuItems m LEFT JOIN OrderItems oi ON oi.itemName = m.name " +
                    "GROUP BY m.id, m.name ORDER BY 2 DESC",
            null
        )
        while (cursor.moveToNext()) {
            sales.add(ItemSales(cursor.getString(0), cursor.getInt(1), cursor.getDouble(2)))
        }
        cursor.close()
        db.close()
        return sales
    }

    fun getDailySales(): List<DaySales> {
        val days = mutableListOf<DaySales>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT orderDate, COUNT(*), SUM(total) FROM Orders GROUP BY orderDate ORDER BY orderDate",
            null
        )
        while (cursor.moveToNext()) {
            days.add(DaySales(cursor.getString(0), cursor.getInt(1), cursor.getDouble(2)))
        }
        cursor.close()
        db.close()
        return days
    }

    fun getStatusCounts(): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT status, COUNT(*) FROM Orders GROUP BY status", null)
        while (cursor.moveToNext()) {
            counts[cursor.getString(0)] = cursor.getInt(1)
        }
        cursor.close()
        db.close()
        return counts
    }
}