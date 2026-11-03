package com.example.smartcafeadmin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var listMenu: ListView

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

        val editName = findViewById<EditText>(R.id.editName)
        val editCategory = findViewById<EditText>(R.id.editCategory)
        val editPrice = findViewById<EditText>(R.id.editPrice)
        val checkAvailable = findViewById<CheckBox>(R.id.checkAvailable)
        val buttonAdd = findViewById<Button>(R.id.buttonAdd)
        listMenu = findViewById(R.id.listMenu)

        showMenu()

        buttonAdd.setOnClickListener {
            val name = editName.text.toString().trim()
            val category = editCategory.text.toString().trim()
            val price = editPrice.text.toString().toDoubleOrNull()

            if (name.isEmpty() || category.isEmpty() || price == null) {
                Toast.makeText(this, "Please enter a name, category and valid price", Toast.LENGTH_SHORT).show()
            } else {
                val result = db.addMenuItem(name, category, price, checkAvailable.isChecked)
                if (result == -1L) {
                    Toast.makeText(this, "Could not save item", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "$name added", Toast.LENGTH_SHORT).show()
                    editName.text.clear()
                    editCategory.text.clear()
                    editPrice.text.clear()
                    checkAvailable.isChecked = true
                    showMenu()
                }
            }
        }
    }

    private fun showMenu() {
        val items = db.getAllMenuItems()
        val lines = items.map { item ->
            val status = if (item.available) "Available" else "Sold out"
            "${item.name}  |  ${item.category}  |  £${"%.2f".format(item.price)}  |  $status"
        }
        listMenu.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lines)
    }
}