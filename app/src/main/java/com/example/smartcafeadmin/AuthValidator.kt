package com.example.smartcafeadmin

// Input validation for login and registration (no Android code, so it can be unit tested)
object AuthValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

    // At least 8 characters with at least one letter and one number
    fun isValidPassword(password: String): Boolean =
        password.length >= 8 && password.any { it.isLetter() } && password.any { it.isDigit() }

    // Returns an error message, or null if the input is valid
    fun validateLogin(email: String, password: String): String? = when {
        email.isEmpty() || password.isEmpty() -> "Please enter your email and password"
        !isValidEmail(email) -> "Please enter a valid email address"
        else -> null
    }

    fun validateRegistration(email: String, password: String, confirm: String): String? = when {
        email.isEmpty() || password.isEmpty() || confirm.isEmpty() -> "Please fill in all fields"
        !isValidEmail(email) -> "Please enter a valid email address"
        !isValidPassword(password) -> "Password must be at least 8 characters and include a letter and a number"
        password != confirm -> "Passwords do not match"
        else -> null
    }
}

