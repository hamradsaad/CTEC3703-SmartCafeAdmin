package com.example.smartcafeadmin

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "SmartCafe.db", null, 3) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

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
        upgradeToVersion3(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createOrdersTable(db)
        }
        if (oldVersion < 3) {
            upgradeToVersion3(db)
            // Old sample orders have no item rows, so clear them.
            // New sample orders are added the next time the app opens.
            db.execSQL("DELETE FROM Orders")
        }
    }

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

    // Version 3: menu descriptions, order notes, and the OrderItems table
    private fun upgradeToVersion3(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE MenuItems ADD COLUMN description TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE Orders ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
        db.execSQL(
            "CREATE TABLE OrderItems (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "orderId INTEGER NOT NULL, " +
                    "itemName TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "FOREIGN KEY(orderId) REFERENCES Orders(id) ON DELETE CASCADE)"
        )
    }

    // ---------- Menu items ----------

    private fun menuItemFrom(c: Cursor) = MenuItem(
        id = c.getInt(c.getColumnIndexOrThrow("id")),
        name = c.getString(c.getColumnIndexOrThrow("name")),
        category = c.getString(c.getColumnIndexOrThrow("category")),
        price = c.getDouble(c.getColumnIndexOrThrow("price")),
        available = c.getInt(c.getColumnIndexOrThrow("available")) == 1,
        description = c.getString(c.getColumnIndexOrThrow("description"))
    )

    fun addMenuItem(name: String, category: String, price: Double, available: Boolean, description: String = ""): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("category", category)
        values.put("price", price)
        values.put("available", if (available) 1 else 0)
        values.put("description", description)
        val result = db.insert("MenuItems", null, values)
        db.close()
        return result
    }

    fun getAllMenuItems(): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM MenuItems", null)
        while (cursor.moveToNext()) {
            items.add(menuItemFrom(cursor))
        }
        cursor.close()
        db.close()
        return items
    }

    fun getAvailableMenuItems(): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM MenuItems WHERE available = 1", null)
        while (cursor.moveToNext()) {
            items.add(menuItemFrom(cursor))
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
        values.put("description", item.description)
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

    fun addSampleMenuIfEmpty() {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM MenuItems", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()

        if (count == 0) {
            addMenuItem("Latte", "Hot Drinks", 3.50, true, "Espresso with steamed milk")
            addMenuItem("Cappuccino", "Hot Drinks", 3.40, true, "Espresso with foamed milk")
            addMenuItem("Hot Chocolate", "Hot Drinks", 3.00, true, "Rich chocolate with steamed milk")
            addMenuItem("White Chocolate Matcha", "Hot Drinks", 4.80, true, "Matcha latte with white chocolate")
            addMenuItem("Iced Coffee", "Cold Drinks", 2.80, true, "Chilled coffee over ice")
            addMenuItem("Orange Juice", "Cold Drinks", 3.00, true, "Freshly squeezed")
            addMenuItem("Chicken Panini", "Food", 6.50, true, "Toasted panini with chicken and cheese")
            addMenuItem("Msemman", "Food", 3.00, true, "Moroccan flatbread")
            addMenuItem("Tiramisu", "Desserts", 6.50, true, "Coffee-soaked sponge with mascarpone")
            addMenuItem("Banana Pudding", "Desserts", 5.95, true, "Layered banana, custard and biscuits")
        }
    }

    // ---------- Orders ----------

    private fun orderFrom(c: Cursor) = Order(
        id = c.getInt(c.getColumnIndexOrThrow("id")),
        customerName = c.getString(c.getColumnIndexOrThrow("customerName")),
        items = c.getString(c.getColumnIndexOrThrow("items")),
        total = c.getDouble(c.getColumnIndexOrThrow("total")),
        status = c.getString(c.getColumnIndexOrThrow("status")),
        orderDate = c.getString(c.getColumnIndexOrThrow("orderDate")),
        notes = c.getString(c.getColumnIndexOrThrow("notes"))
    )

    // Saves the order AND one OrderItems row per item, all in one transaction
    fun addOrder(customerName: String, lines: List<OrderItem>, status: String, orderDate: String, notes: String = ""): Long {
        val itemsText = lines.joinToString(", ") { "${it.quantity}x ${it.itemName}" }
        val total = Math.round(lines.sumOf { it.quantity * it.price } * 100) / 100.0

        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues()
            values.put("customerName", customerName)
            values.put("items", itemsText)
            values.put("total", total)
            values.put("status", status)
            values.put("orderDate", orderDate)
            values.put("notes", notes)
            val orderId = db.insert("Orders", null, values)
            if (orderId == -1L) return -1

            for (line in lines) {
                val lineValues = ContentValues()
                lineValues.put("orderId", orderId)
                lineValues.put("itemName", line.itemName)
                lineValues.put("quantity", line.quantity)
                lineValues.put("price", line.price)
                if (db.insert("OrderItems", null, lineValues) == -1L) return -1
            }

            db.setTransactionSuccessful()
            return orderId
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getAllOrders(): List<Order> {
        val orders = mutableListOf<Order>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Orders ORDER BY id DESC", null)
        while (cursor.moveToNext()) {
            orders.add(orderFrom(cursor))
        }
        cursor.close()
        db.close()
        return orders
    }

    fun getOrdersByStatus(status: String): List<Order> {
        val orders = mutableListOf<Order>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Orders WHERE status = ? ORDER BY id DESC", arrayOf(status))
        while (cursor.moveToNext()) {
            orders.add(orderFrom(cursor))
        }
        cursor.close()
        db.close()
        return orders
    }

    fun getOrderItems(orderId: Int): List<OrderItem> {
        val lines = mutableListOf<OrderItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM OrderItems WHERE orderId = ?", arrayOf(orderId.toString()))
        while (cursor.moveToNext()) {
            lines.add(
                OrderItem(
                    itemName = cursor.getString(cursor.getColumnIndexOrThrow("itemName")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    orderId = cursor.getInt(cursor.getColumnIndexOrThrow("orderId"))
                )
            )
        }
        cursor.close()
        db.close()
        return lines
    }

    fun updateOrderStatus(id: Int, status: String): Int {
        val db = writableDatabase
        val values = ContentValues()
        values.put("status", status)
        val rows = db.update("Orders", values, "id = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    fun updateOrderNotes(id: Int, notes: String): Int {
        val db = writableDatabase
        val values = ContentValues()
        values.put("notes", notes)
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
            addOrder("Aisha", listOf(OrderItem("Latte", 2, 3.50), OrderItem("Msemman", 1, 3.00)), "Collected", "2026-11-01")
            addOrder("Tom", listOf(OrderItem("Chicken Panini", 1, 6.50), OrderItem("Orange Juice", 1, 3.00)), "Collected", "2026-11-01")
            addOrder("Priya", listOf(OrderItem("White Chocolate Matcha", 1, 4.80), OrderItem("Tiramisu", 1, 6.50)), "Ready", "2026-11-02")
            addOrder("James", listOf(OrderItem("Cappuccino", 2, 3.40), OrderItem("Banana Pudding", 2, 5.95)), "Preparing", "2026-11-03", "Extra shot in one cappuccino")
            addOrder("Sara", listOf(OrderItem("Hot Chocolate", 1, 3.00), OrderItem("Msemman", 1, 3.00)), "Pending", "2026-11-03")
            addOrder("Ben", listOf(OrderItem("Iced Coffee", 1, 2.80), OrderItem("Chicken Panini", 1, 6.50)), "Pending", "2026-11-03")
        }
    }
}