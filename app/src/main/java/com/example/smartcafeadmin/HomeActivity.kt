package com.example.smartcafeadmin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

// Home dashboard with large, clearly labelled buttons (CR-01)
class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthGuard.isSignedIn(this)) return

        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        findViewById<TextView>(R.id.textWelcome).text = "Signed in as $email"

        findViewById<Button>(R.id.buttonMenu).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<Button>(R.id.buttonOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }
        findViewById<Button>(R.id.buttonAnalytics).setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }
        findViewById<Button>(R.id.buttonFeedback).setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }
        findViewById<Button>(R.id.buttonTerms).setOnClickListener {
            PolicyActivity.open(this, PolicyActivity.TERMS)
        }
        findViewById<Button>(R.id.buttonPrivacy).setOnClickListener {
            PolicyActivity.open(this, PolicyActivity.PRIVACY)
        }
        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}