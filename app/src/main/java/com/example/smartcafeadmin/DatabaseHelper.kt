package com.example.smartcafeadmin
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "SmartCafe.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE MenuItems (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "available INTEGER NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS MenuItems")
        onCreate(db)
    }
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


}