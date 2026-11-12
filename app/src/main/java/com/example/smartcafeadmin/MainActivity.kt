package com.example.smartcafeadmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

// Menu screen: cards with pictures and category filter (CR-01, CR-02, CR-03)
class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: MenuAdapter
    private lateinit var textEmpty: TextView
    private var selectedCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthGuard.isSignedIn(this)) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        db.addSampleMenuIfEmpty()
        SampleImages.attach(this, db)

        textEmpty = findViewById(R.id.textEmpty)

        // Tap a card to open its details page
        adapter = MenuAdapter { item ->
            val intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra("itemId", item.id)
            startActivity(intent)
        }
        val recycler = findViewById<RecyclerView>(R.id.recyclerMenu)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<Button>(R.id.buttonBack).setOnClickListener { finish() }

        // Add a new item
        findViewById<ExtendedFloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, EditItemActivity::class.java))
        }

        setupCategoryChips()
    }

    // Reload whenever we come back from another screen
    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) showMenu()
    }

    private fun setupCategoryChips() {
        val group = findViewById<ChipGroup>(R.id.chipGroupCategories)

        for (name in listOf("All") + Categories.ALL) {
            val chip = Chip(this)
            chip.id = View.generateViewId()
            chip.text = name
            chip.tag = name
            chip.isCheckable = true
            chip.isCheckedIconVisible = false
            group.addView(chip)
            if (name == selectedCategory) chip.isChecked = true
        }

        group.setOnCheckedStateChangeListener { g, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { g.findViewById<Chip>(it) }
            selectedCategory = chip?.tag as? String ?: "All"
            showMenu()
        }
    }

    private fun showMenu() {
        val items = if (selectedCategory == "All") db.getAllMenuItems()
        else db.getMenuItemsByCategory(selectedCategory)

        adapter.submit(items)
        textEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }
}