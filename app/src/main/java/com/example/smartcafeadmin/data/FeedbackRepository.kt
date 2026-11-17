package com.example.smartcafeadmin.data

import android.content.ContentValues
import android.content.Context
import com.example.smartcafeadmin.logic.FeedbackRules
import com.example.smartcafeadmin.model.Feedback

// Database queries for customer feedback (CAFE-08)
class FeedbackRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun addFeedback(orderId: Int, customerName: String, rating: Int, comment: String, date: String): Long {
        if (FeedbackRules.validate(rating, comment) != null) return -1

        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put("orderId", orderId)
        values.put("customerName", customerName)
        values.put("rating", rating)
        values.put("comment", comment)
        values.put("date", date)
        val id = db.insert("Feedback", null, values)
        db.close()
        return id
    }

    // Feedback with a rating between min and max, newest first
    fun getFeedback(minRating: Int = 1, maxRating: Int = 5): List<Feedback> {
        val list = mutableListOf<Feedback>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM Feedback WHERE rating BETWEEN ? AND ? ORDER BY id DESC",
            arrayOf(minRating.toString(), maxRating.toString())
        )
        while (cursor.moveToNext()) {
            list.add(
                Feedback(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    orderId = cursor.getInt(cursor.getColumnIndexOrThrow("orderId")),
                    customerName = cursor.getString(cursor.getColumnIndexOrThrow("customerName")),
                    rating = cursor.getInt(cursor.getColumnIndexOrThrow("rating")),
                    comment = cursor.getString(cursor.getColumnIndexOrThrow("comment")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                )
            )
        }
        cursor.close()
        db.close()
        return list
    }

    fun getAverageRating(): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COALESCE(AVG(rating), 0) FROM Feedback", null)
        cursor.moveToFirst()
        val avg = cursor.getDouble(0)
        cursor.close()
        db.close()
        return avg
    }

    fun getCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Feedback", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    // First run: give every already-collected order some simulated feedback
    fun addSampleFeedbackIfEmpty() {
        if (getCount() > 0) return

        val collected = mutableListOf<Triple<Int, String, String>>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, customerName, orderDate FROM Orders WHERE status = ?",
            arrayOf("Collected")
        )
        while (cursor.moveToNext()) {
            collected.add(Triple(cursor.getInt(0), cursor.getString(1), cursor.getString(2)))
        }
        cursor.close()
        db.close()

        for ((orderId, name, date) in collected) {
            val rating = FeedbackRules.simulatedRating()
            addFeedback(orderId, name, rating, FeedbackRules.simulatedComment(rating), date)
        }
    }
}