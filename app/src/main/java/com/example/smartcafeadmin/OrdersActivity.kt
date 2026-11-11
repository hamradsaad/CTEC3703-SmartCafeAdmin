package com.example.smartcafeadmin

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var listOrders: ListView
    private lateinit var spinnerFilter: Spinner
    private var orders: List<Order> = emptyList()

    private val filterOptions = arrayOf("All") + OrderRules.STATUSES.toTypedArray()

    // Fictional data used by the customer simulator
    private val customerNames = listOf("Aisha", "Tom", "Priya", "James", "Sara", "Ben", "Yusuf", "Chloe", "Omar", "Emily")
    private val sampleNotes = listOf("", "", "", "Extra shot", "No sugar", "Oat milk please", "Takeaway", "Extra hot")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthGuard.isSignedIn(this)) return
        enableEdgeToEdge()
        setContentView(R.layout.activity_orders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        listOrders = findViewById(R.id.listOrders)
        spinnerFilter = findViewById(R.id.spinnerFilter)

        db.addSampleOrdersIfEmpty()

        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            finish()
        }

        // Filter by status
        spinnerFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filterOptions)
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                showOrders()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Customer simulator
        findViewById<Button>(R.id.buttonSimulate).setOnClickListener {
            simulateCustomerOrder()
        }

        // Tap an order: show details, change status or edit note
        listOrders.setOnItemClickListener { _, _, position, _ ->
            showOrderDetails(orders[position])
        }
    }

    private fun showOrders() {
        val filter = spinnerFilter.selectedItem?.toString() ?: "All"
        orders = if (filter == "All") db.getAllOrders() else db.getOrdersByStatus(filter)

        val lines = orders.map { order ->
            var line = "#${order.id}  ${order.customerName}  (${order.orderDate})\n" +
                    "${order.items}\n" +
                    "£${"%.2f".format(order.total)}  •  ${order.status}"
            if (order.notes.isNotEmpty()) {
                line += "\nNote: ${order.notes}"
            }
            line
        }
        listOrders.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lines)

        if (orders.isEmpty()) {
            Toast.makeText(this, "No $filter orders", Toast.LENGTH_SHORT).show()
        }
    }

    private fun simulateCustomerOrder() {
        val available = db.getAvailableMenuItems()
        if (available.isEmpty()) {
            Toast.makeText(this, "No available menu items. Add items or mark them available first.", Toast.LENGTH_LONG).show()
            return
        }

        // Pick 1 to 3 different available items, each with quantity 1 or 2, at the current menu price
        val howMany = (1..minOf(3, available.size)).random()
        val chosen = available.shuffled().take(howMany)
        val lines = chosen.map { item -> OrderItem(item.name, (1..2).random(), item.price) }

        val name = customerNames.random()
        val note = sampleNotes.random()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.UK).format(Date())

        val orderId = db.addOrder(name, lines, "Pending", today, note)
        if (orderId == -1L) {
            Toast.makeText(this, "Could not create order", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "New order #$orderId from $name", Toast.LENGTH_SHORT).show()
        showOrders()
    }

    private fun showOrderDetails(order: Order) {
        val details = "Items: ${order.items}\n" +
                "Total: £${"%.2f".format(order.total)}\n" +
                "Status: ${order.status}\n" +
                "Date: ${order.orderDate}\n" +
                "Note: ${if (order.notes.isEmpty()) "(none)" else order.notes}"

        AlertDialog.Builder(this)
            .setTitle("Order #${order.id} – ${order.customerName}")
            .setMessage(details)
            .setPositiveButton("Change status") { _, _ -> changeStatus(order) }
            .setNeutralButton("Edit note") { _, _ -> editNote(order) }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun changeStatus(order: Order) {
        val options = OrderRules.allowedNextStatuses(order.status)

        if (options.isEmpty()) {
            Toast.makeText(this, "Order #${order.id} is ${order.status} and cannot be changed", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Order #${order.id}: ${order.status} → ?")
            .setItems(options.toTypedArray()) { _, which ->
                val newStatus = options[which]
                val rows = db.updateOrderStatus(order.id, newStatus)
                if (rows == 1) {
                    Toast.makeText(this, "Order #${order.id} is now $newStatus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Status change not allowed", Toast.LENGTH_SHORT).show()
                }
                showOrders()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editNote(order: Order) {
        val input = EditText(this)
        input.hint = "e.g. Extra shot (optional)"
        input.setText(order.notes)

        AlertDialog.Builder(this)
            .setTitle("Note for order #${order.id}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val note = input.text.toString().trim()
                db.updateOrderNotes(order.id, note)
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
                showOrders()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}