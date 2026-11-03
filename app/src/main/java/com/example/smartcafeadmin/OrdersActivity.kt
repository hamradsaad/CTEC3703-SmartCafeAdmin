package com.example.smartcafeadmin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OrdersActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var listOrders: ListView
    private var orders: List<Order> = emptyList()
    private val statuses = arrayOf("Pending", "Preparing", "Ready", "Collected")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_orders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        listOrders = findViewById(R.id.listOrders)

        db.addSampleOrdersIfEmpty()
        showOrders()

        listOrders.setOnItemClickListener { _, _, position, _ ->
            val order = orders[position]
            AlertDialog.Builder(this)
                .setTitle("Order #${order.id} – ${order.customerName}")
                .setItems(statuses) { _, which ->
                    val newStatus = statuses[which]
                    db.updateOrderStatus(order.id, newStatus)
                    Toast.makeText(this, "Order #${order.id} is now $newStatus", Toast.LENGTH_SHORT).show()
                    showOrders()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showOrders() {
        orders = db.getAllOrders()
        val lines = orders.map { order ->
            "#${order.id}  ${order.customerName}  (${order.orderDate})\n" +
                    "${order.items}\n" +
                    "£${"%.2f".format(order.total)}  •  ${order.status}"
        }
        listOrders.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lines)
    }
}