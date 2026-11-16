package com.example.smartcafeadmin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Shows the Terms and Conditions or the Privacy Notice.
// No login needed, so people can read them before registering.
class PolicyActivity : AppCompatActivity() {

    companion object {
        const val TERMS = "terms"
        const val PRIVACY = "privacy"

        fun open(context: Context, type: String) {
            val intent = Intent(context, PolicyActivity::class.java)
            intent.putExtra("type", type)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_policy)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val isPrivacy = intent.getStringExtra("type") == PRIVACY
        findViewById<TextView>(R.id.textTitle).text = if (isPrivacy) "Privacy Notice" else "Terms and Conditions"
        findViewById<TextView>(R.id.textBody).text = if (isPrivacy) PolicyText.PRIVACY else PolicyText.TERMS
        findViewById<Button>(R.id.buttonBack).setOnClickListener { finish() }
    }
}