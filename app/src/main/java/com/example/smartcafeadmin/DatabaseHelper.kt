package com.example.smartcafeadmin

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "SmartCafe.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE MenuItems (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "available INTEGER NOT NULL)"
        )
        createOrdersTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createOrdersTable(db)
        }
    }

    // NEW: the Orders table
    private fun createOrdersTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE Orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "customerName TEXT NOT NULL, " +
                    "items TEXT NOT NULL, " +
                    "total REAL NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "orderDate TEXT NOT NULL)"
        )
    }

    // ---------- Menu items ----------

    fun addMenuItem(name: String, category: String, price: Double, available: Boolean): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("category", category)
        values.put("price", price)
        values.put("available", if (available) 1 else 0)
        val result = db.insert("MenuItems", null, values)
        db.close()
        return result
    }

    fun getAllMenuItems(): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM MenuItems", null)
        while (cursor.moveToNext()) {
            val item = MenuItem(
                id = cursor.getInt(0),
                name = cursor.getString(1),
                category = cursor.getString(2),
                price = cursor.getDouble(3),
                available = cursor.getInt(4) == 1
            )
            items.add(item)
        }
        cursor.close()
        db.close()
        return items
    }

    fun updateMenuItem(item: MenuItem): Int {
        val db = writableDatabase
        val values = ContentValues()
        values.put("name", item.name)
        values.put("category", item.category)
        values.put("price", item.price)
        values.put("available", if (item.available) 1 else 0)
        val rows = db.update("MenuItems", values, "id = ?", arrayOf(item.id.toString()))
        db.close()
        return rows
    }

    fun deleteMenuItem(id: Int): Int {
        val db = writableDatabase
        val rows = db.delete("MenuItems", "id = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    // ---------- NEW: Orders ----------

    fun addOrder(customerName: String, items: String, total: Double, status: String, orderDate: String): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put("customerName", customerName)
        values.put("items", items)
        values.put("total", total)
        values.put("status", status)
        values.put("orderDate", orderDate)
        val result = db.insert("Orders", null, values)
        db.close()
        return result
    }

    fun getAllOrders(): List<Order> {
        val orders = mutableListOf<Order>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Orders ORDER BY id DESC", null)
        while (cursor.moveToNext()) {
            val order = Order(
                id = cursor.getInt(0),
                customerName = cursor.getString(1),
                items = cursor.getString(2),
                total = cursor.getDouble(3),
                status = cursor.getString(4),
                orderDate = cursor.getString(5)
            )
            orders.add(order)
        }
        cursor.close()
        db.close()
        return orders
    }

    fun updateOrderStatus(id: Int, status: String): Int {
        val db = writableDatabase
        val values = ContentValues()
        values.put("status", status)
        val rows = db.update("Orders", values, "id = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    fun addSampleOrdersIfEmpty() {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Orders", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()

        if (count == 0) {
            addOrder("Aisha", "2x Latte, 1x Msemman", 10.00, "Collected", "2026-11-01")
            addOrder("Tom", "1x Chicken Panini, 1x Orange Juice", 9.50, "Collected", "2026-11-01")
            addOrder("Priya", "1x White chocolate Matcha, 1x Tiramisu", 11.30, "Ready", "2026-11-02")
            addOrder("James", "2x Cappuccino, 2x Banana Pudding", 18.70, "Preparing", "2026-11-03")
            addOrder("Sara", "1x Hot Chocolate, 1x Msemman", 6.00, "Pending", "2026-11-03")
            addOrder("Ben", "1x Iced Coffee, 1x Chicken Panini", 9.30, "Pending", "2026-11-03")
        }
    }
}