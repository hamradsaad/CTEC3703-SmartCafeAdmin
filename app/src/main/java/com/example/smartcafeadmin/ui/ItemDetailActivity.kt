package com.example.smartcafeadmin.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartcafeadmin.R
import com.example.smartcafeadmin.data.DatabaseHelper
import com.example.smartcafeadmin.data.ImageStorage
import com.example.smartcafeadmin.logic.Categories
import com.example.smartcafeadmin.model.MenuItem
import com.google.android.material.chip.Chip

// Item details: picture, name, price tag, category, availability and description (CR-01, CR-03)
class ItemDetailActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var itemId = -1
    private var item: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthGuard.isSignedIn(this)) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_item_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        itemId = intent.getIntExtra("itemId", -1)

        findViewById<Button>(R.id.buttonBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.buttonEdit).setOnClickListener {
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", itemId)
            startActivity(intent)
        }

        // Quick switch between Available and Sold out
        findViewById<Button>(R.id.buttonToggle).setOnClickListener {
            val current = item ?: return@setOnClickListener
            db.updateMenuItem(current.copy(available = !current.available))
            val message = if (current.available) "${current.name} marked as sold out" else "${current.name} is available again"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            loadItem()
        }
    }

    // Reload after coming back from the edit screen
    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) loadItem()
    }

    private fun loadItem() {
        val loaded = db.getMenuItem(itemId)
        if (loaded == null) {
            finish()   // item was deleted
            return
        }
        item = loaded

        findViewById<TextView>(R.id.textName).text = loaded.name
        findViewById<Chip>(R.id.chipPrice).text = "£${"%.2f".format(loaded.price)}"
        findViewById<Chip>(R.id.chipCategory).text = "${Categories.emojiFor(loaded.category)}  ${loaded.category}"
        findViewById<TextView>(R.id.textDescription).text =
            if (loaded.description.isEmpty()) "No description yet." else loaded.description

        val textAvailability = findViewById<TextView>(R.id.textAvailability)
        val buttonToggle = findViewById<Button>(R.id.buttonToggle)
        if (loaded.available) {
            textAvailability.text = "● Available to order"
            textAvailability.setTextColor(Color.parseColor("#1B5E20"))
            buttonToggle.text = "Mark as sold out"
        } else {
            textAvailability.text = "● Sold out"
            textAvailability.setTextColor(Color.parseColor("#B3261E"))
            buttonToggle.text = "Mark as available"
        }

        val textEmoji = findViewById<TextView>(R.id.textEmoji)
        val imageItem = findViewById<ImageView>(R.id.imageItem)
        textEmoji.text = Categories.emojiFor(loaded.category)
        val bitmap = ImageStorage.loadThumbnail(loaded.imagePath, 1000)
        if (bitmap != null) {
            imageItem.setImageBitmap(bitmap)
            imageItem.visibility = View.VISIBLE
        } else {
            imageItem.visibility = View.GONE
        }
    }
}