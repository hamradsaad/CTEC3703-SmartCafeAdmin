package com.example.smartcafeadmin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editConfirm: EditText
    private lateinit var buttonCreate: Button
    private lateinit var progress: ProgressBar
    private lateinit var textError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        editConfirm = findViewById(R.id.editConfirm)
        buttonCreate = findViewById(R.id.buttonCreate)
        progress = findViewById(R.id.progress)
        textError = findViewById(R.id.textError)

        buttonCreate.setOnClickListener { register() }
        findViewById<Button>(R.id.buttonBack).setOnClickListener { finish() }
    }

    private fun register() {
        val email = editEmail.text.toString().trim()
        val password = editPassword.text.toString()
        val confirm = editConfirm.text.toString()

        val error = AuthValidator.validateRegistration(email, password, confirm)
        if (error != null) {
            showError(error)
            return
        }

        setLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    setLoading(false)
                    showError("Registration failed. Please try again.")
                    return@addOnSuccessListener
                }

                // Every new account starts as "customer". Admin access is granted by the café manager.
                val profile = hashMapOf(
                    "email" to email,
                    "role" to "customer",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                firestore.collection("users").document(uid).set(profile)
                    .addOnSuccessListener {
                        auth.signOut()
                        setLoading(false)
                        AlertDialog.Builder(this)
                            .setTitle("Account created")
                            .setMessage("Your account has been created as a customer account. Only café admin accounts can use this app. Ask the café manager to grant admin access.")
                            .setPositiveButton("OK") { _, _ -> finish() }
                            .setCancelable(false)
                            .show()
                    }
                    .addOnFailureListener {
                        auth.signOut()
                        setLoading(false)
                        showError("Account created, but the profile could not be saved. Please try again later.")
                    }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                showError(friendlyError(e))
            }
    }

    private fun friendlyError(e: Exception): String = when (e) {
        is FirebaseAuthUserCollisionException -> "An account with this email already exists"
        is FirebaseAuthWeakPasswordException -> "Password is too weak"
        is FirebaseAuthInvalidCredentialsException -> "Please enter a valid email address"
        is FirebaseNetworkException -> "No internet connection. Please try again."
        else -> "Registration failed: ${e.localizedMessage}"
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        buttonCreate.isEnabled = !loading
        if (loading) textError.visibility = View.GONE
    }

    private fun showError(message: String) {
        textError.text = message
        textError.visibility = View.VISIBLE
    }
}

