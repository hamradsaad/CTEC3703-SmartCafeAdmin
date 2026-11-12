package com.example.smartcafeadmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button
    private lateinit var progress: ProgressBar
    private lateinit var textError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonRegister = findViewById(R.id.buttonRegister)
        progress = findViewById(R.id.progress)
        textError = findViewById(R.id.textError)

        buttonLogin.setOnClickListener { login() }
        buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Already signed in from last time: re-check the role, then continue
        auth.currentUser?.let { checkRoleAndContinue(it.uid) }
    }

    private fun login() {
        val email = editEmail.text.toString().trim()
        val password = editPassword.text.toString()

        val error = AuthValidator.validateLogin(email, password)
        if (error != null) {
            showError(error)
            return
        }

        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    setLoading(false)
                    showError("Login failed. Please try again.")
                } else {
                    checkRoleAndContinue(uid)
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                showError(friendlyError(e))
            }
    }

    // Reads users/{uid} from Firestore and only lets "admin" accounts in
    private fun checkRoleAndContinue(uid: String) {
        setLoading(true)
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role")
                if (role == "admin") {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    auth.signOut()
                    setLoading(false)
                    showError("Access denied: only café admin accounts can use this app.")
                }
            }
            .addOnFailureListener {
                auth.signOut()
                setLoading(false)
                showError("Could not check your access. Check your internet connection and try again.")
            }
    }

    private fun friendlyError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> "Incorrect email or password"
        is FirebaseNetworkException -> "No internet connection. Please try again."
        else -> "Login failed: ${e.localizedMessage}"
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        buttonLogin.isEnabled = !loading
        buttonRegister.isEnabled = !loading
        if (loading) textError.visibility = View.GONE
    }

    private fun showError(message: String) {
        textError.text = message
        textError.visibility = View.VISIBLE
    }
}

