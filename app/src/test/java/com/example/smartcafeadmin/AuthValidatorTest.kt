package com.example.smartcafeadmin

import com.example.smartcafeadmin.logic.AuthValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun validEmailIsAccepted() {
        assertTrue(AuthValidator.isValidEmail("staff@smartcafe.com"))
    }

    @Test
    fun invalidEmailsAreRejected() {
        assertFalse(AuthValidator.isValidEmail("staffsmartcafe.com"))   // no @
        assertFalse(AuthValidator.isValidEmail("staff@"))               // no domain
        assertFalse(AuthValidator.isValidEmail(""))                     // empty
    }

    @Test
    fun strongPasswordIsAccepted() {
        assertTrue(AuthValidator.isValidPassword("Cafe2026x"))
    }

    @Test
    fun weakPasswordsAreRejected() {
        assertFalse(AuthValidator.isValidPassword("Cafe12"))      // too short
        assertFalse(AuthValidator.isValidPassword("CafeCafe"))    // no number
        assertFalse(AuthValidator.isValidPassword("12345678"))    // no letter
    }

    @Test
    fun passwordBoundaryOfEightCharacters() {
        assertFalse(AuthValidator.isValidPassword("Cafe202"))     // 7 characters
        assertTrue(AuthValidator.isValidPassword("Cafe2026"))     // 8 characters
    }

    @Test
    fun emptyLoginFieldsGiveError() {
        assertEquals("Please enter your email and password", AuthValidator.validateLogin("", ""))
    }

    @Test
    fun validLoginGivesNoError() {
        assertNull(AuthValidator.validateLogin("staff@smartcafe.com", "anything"))
    }

    @Test
    fun mismatchedPasswordsGiveError() {
        assertEquals(
            "Passwords do not match",
            AuthValidator.validateRegistration("staff@smartcafe.com", "Cafe2026x", "Cafe2026y")
        )
    }
}