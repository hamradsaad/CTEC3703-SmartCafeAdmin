package com.example.smartcafeadmin

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
import kotlin.math.roundToInt

// Review customer feedback and star ratings (CAFE-08)
class FeedbackActivity : AppCompatActivity() {

    private lateinit var repo: FeedbackRepository
    private lateinit var adapter: FeedbackAdapter
    private lateinit var textEmpty: TextView
    private var filter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthGuard.isSignedIn(this)) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repo = FeedbackRepository(this)
        repo.addSampleFeedbackIfEmpty()

        textEmpty = findViewById(R.id.textEmpty)
        adapter = FeedbackAdapter()
        val recycler = findViewById<RecyclerView>(R.id.recyclerFeedback)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<Button>(R.id.buttonBack).setOnClickListener { finish() }

        setupFilterChips()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) showFeedback()
    }

    private fun setupFilterChips() {
        val group = findViewById<ChipGroup>(R.id.chipGroupFilter)
        for (name in listOf("All", "5 ★", "4 ★", "3 ★", "Low (1–2 ★)")) {
            val chip = Chip(this)
            chip.id = View.generateViewId()
            chip.text = name
            chip.tag = name
            chip.isCheckable = true
            chip.isCheckedIconVisible = false
            group.addView(chip)
            if (name == filter) chip.isChecked = true
        }
        group.setOnCheckedStateChangeListener { g, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { g.findViewById<Chip>(it) }
            filter = chip?.tag as? String ?: "All"
            showFeedback()
        }
    }

    private fun showFeedback() {
        val list = when (filter) {
            "5 ★" -> repo.getFeedback(5, 5)
            "4 ★" -> repo.getFeedback(4, 4)
            "3 ★" -> repo.getFeedback(3, 3)
            "Low (1–2 ★)" -> repo.getFeedback(1, 2)
            else -> repo.getFeedback()
        }
        adapter.submit(list)
        textEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

        val count = repo.getCount()
        val average = repo.getAverageRating()
        findViewById<TextView>(R.id.textAverage).text = if (count == 0) "–" else "%.1f".format(average)
        findViewById<TextView>(R.id.textAverageStars).text = FeedbackRules.stars(average.roundToInt())
        findViewById<TextView>(R.id.textCount).text = "from $count review${if (count == 1) "" else "s"}"
    }
}