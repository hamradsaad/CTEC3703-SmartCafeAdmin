package com.example.smartcafeadmin.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartcafeadmin.R
import com.example.smartcafeadmin.data.DatabaseHelper
import com.example.smartcafeadmin.data.FeedbackRepository
import com.example.smartcafeadmin.logic.FeedbackRules
import com.example.smartcafeadmin.logic.OrderMessages
import com.example.smartcafeadmin.logic.OrderRules
import com.example.smartcafeadmin.model.Order
import com.example.smartcafeadmin.model.OrderItem
import com.google.android.material.materialswitch.MaterialSwitch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var feedbackRepo: FeedbackRepository
    private lateinit var listOrders: ListView
    private lateinit var spinnerFilter: Spinner
    private var orders: List<Order> = emptyList()

    private val filterOptions = arrayOf("All") + OrderRules.STATUSES.toTypedArray()

    // Fictional data used by the customer simulator
    private val customerNames = listOf("Aisha", "Tom", "Priya", "James", "Sara", "Ben", "Yusuf", "Chloe", "Omar", "Emily")
    private val sampleNotes = listOf("", "", "", "Extra shot", "No sugar", "Oat milk please", "Takeaway", "Extra hot")

    // Automated order generator: creates a new customer order every 20 seconds while switched on
    private val handler = Handler(Looper.getMainLooper())
    private var autoOrdersOn = false
    private val autoOrderRunnable = object : Runnable {
        override fun run() {
            simulateCustomerOrder()
            handler.postDelayed(this, AUTO_ORDER_INTERVAL_MS)
        }
    }

    // Notification permission (Android 13+). The app still works if it is refused.
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val message = if (granted) "Notifications on: you'll be alerted about order updates"
            else "Notifications off: updates will still show on this screen"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

    companion object {
        private const val AUTO_ORDER_INTERVAL_MS = 20_000L
        private var askedForNotificationsThisSession = false
    }

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
        feedbackRepo = FeedbackRepository(this)
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

        // Customer simulator (one order)
        findViewById<Button>(R.id.buttonSimulate).setOnClickListener {
            simulateCustomerOrder()
        }

        // Automated order generator switch
        findViewById<MaterialSwitch>(R.id.switchAutoOrders).setOnCheckedChangeListener { _, isChecked ->
            autoOrdersOn = isChecked
            handler.removeCallbacks(autoOrderRunnable)
            if (isChecked) {
                handler.postDelayed(autoOrderRunnable, AUTO_ORDER_INTERVAL_MS)
                Toast.makeText(this, "Auto orders ON: a new order every 20 seconds", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Auto orders OFF", Toast.LENGTH_SHORT).show()
            }
        }

        // Tap an order: show details, change status or edit note
        listOrders.setOnItemClickListener { _, _, position, _ ->
            showOrderDetails(orders[position])
        }

        askForNotificationPermission()
    }

    // Explain why first, then ask. Only asks once per session.
    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (NotificationHelper.hasPermission(this) || askedForNotificationsThisSession) return
        askedForNotificationsThisSession = true

        AlertDialog.Builder(this)
            .setTitle("Order notifications")
            .setMessage("Allow notifications so you're alerted when a new order arrives or an order's status changes. You can change this later in Settings.")
            .setPositiveButton("Continue") { _, _ ->
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Not now", null)
            .show()
    }

    // Restart the generator when returning to this screen
    override fun onResume() {
        super.onResume()
        if (autoOrdersOn) {
            handler.removeCallbacks(autoOrderRunnable)
            handler.postDelayed(autoOrderRunnable, AUTO_ORDER_INTERVAL_MS)
        }
    }

    // Stop the generator when leaving this screen
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoOrderRunnable)
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

        // Notify staff about the new order
        val itemsText = lines.joinToString(", ") { "${it.quantity}x ${it.itemName}" }
        NotificationHelper.show(
            this, orderId.toInt(),
            OrderMessages.newOrderTitle(orderId.toInt()),
            OrderMessages.newOrderText(name, itemsText)
        )

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

                    // Order status notification (CAFE-09)
                    NotificationHelper.show(
                        this, order.id,
                        OrderMessages.statusTitle(order.id),
                        OrderMessages.statusText(order.customerName, newStatus)
                    )

                    if (newStatus == "Collected") simulateCustomerFeedback(order)
                } else {
                    Toast.makeText(this, "Status change not allowed", Toast.LENGTH_SHORT).show()
                }
                showOrders()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Simulated customer: leaves a star rating and comment when their order is collected
    private fun simulateCustomerFeedback(order: Order) {
        val rating = FeedbackRules.simulatedRating()
        val comment = FeedbackRules.simulatedComment(rating)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.UK).format(Date())
        feedbackRepo.addFeedback(order.id, order.customerName, rating, comment, today)
        Toast.makeText(this, "${order.customerName} left ${FeedbackRules.stars(rating)} feedback", Toast.LENGTH_SHORT).show()
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