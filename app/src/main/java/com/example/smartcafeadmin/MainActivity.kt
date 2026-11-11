package com.example.smartcafeadmin

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var listMenu: ListView
    private lateinit var editName: EditText
    private lateinit var editCategory: EditText
    private lateinit var editPrice: EditText
    private lateinit var editDescription: EditText
    private lateinit var checkAvailable: CheckBox
    private lateinit var buttonAdd: Button

    private var menuItems: List<MenuItem> = emptyList()
    private var selectedItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        db.addSampleMenuIfEmpty()

        editName = findViewById(R.id.editName)
        editCategory = findViewById(R.id.editCategory)
        editPrice = findViewById(R.id.editPrice)
        editDescription = findViewById(R.id.editDescription)
        checkAvailable = findViewById(R.id.checkAvailable)
        buttonAdd = findViewById(R.id.buttonAdd)
        listMenu = findViewById(R.id.listMenu)

        showMenu()

        buttonAdd.setOnClickListener {
            saveItem()
        }

        val buttonOrders = findViewById<Button>(R.id.buttonOrders)
        buttonOrders.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        listMenu.setOnItemClickListener { _, _, position, _ ->
            val item = menuItems[position]
            selectedItem = item
            editName.setText(item.name)
            editCategory.setText(item.category)
            editPrice.setText(item.price.toString())
            editDescription.setText(item.description)
            checkAvailable.isChecked = item.available
            buttonAdd.text = "Update item"
        }

        listMenu.setOnItemLongClickListener { _, _, position, _ ->
            val item = menuItems[position]
            AlertDialog.Builder(this)
                .setTitle("Delete item")
                .setMessage("Delete ${item.name} from the menu?")
                .setPositiveButton("Delete") { _, _ ->
                    db.deleteMenuItem(item.id)
                    Toast.makeText(this, "${item.name} deleted", Toast.LENGTH_SHORT).show()
                    clearForm()
                    showMenu()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    private fun saveItem() {
        val name = editName.text.toString().trim()
        val category = editCategory.text.toString().trim()
        val price = editPrice.text.toString().toDoubleOrNull()
        val description = editDescription.text.toString().trim()

        if (name.isEmpty() || category.isEmpty() || price == null || price <= 0) {
            Toast.makeText(this, "Please enter a name, category and valid price", Toast.LENGTH_SHORT).show()
            return
        }

        val current = selectedItem
        if (current == null) {
            val result = db.addMenuItem(name, category, price, checkAvailable.isChecked, description)
            if (result == -1L) {
                Toast.makeText(this, "Could not save item", Toast.LENGTH_SHORT).show()
                return
            }
            Toast.makeText(this, "$name added", Toast.LENGTH_SHORT).show()
        } else {
            val updated = MenuItem(current.id, name, category, price, checkAvailable.isChecked, description)
            db.updateMenuItem(updated)
            Toast.makeText(this, "$name updated", Toast.LENGTH_SHORT).show()
        }

        clearForm()
        showMenu()
    }

    private fun clearForm() {
        editName.text.clear()
        editCategory.text.clear()
        editPrice.text.clear()
        editDescription.text.clear()
        checkAvailable.isChecked = true
        selectedItem = null
        buttonAdd.text = "Add item"
    }

    private fun showMenu() {
        menuItems = db.getAllMenuItems()
        val lines = menuItems.map { item ->
            val status = if (item.available) "Available" else "Sold out"
            var line = "${item.name}  |  ${item.category}  |  £${"%.2f".format(item.price)}  |  $status"
            if (item.description.isNotEmpty()) {
                line += "\n${item.description}"
            }
            line
        }
        listMenu.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lines)
    }
}