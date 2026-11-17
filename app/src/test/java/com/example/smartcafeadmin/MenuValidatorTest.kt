package com.example.smartcafeadmin

import com.example.smartcafeadmin.logic.MenuValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MenuValidatorTest {

    @Test
    fun validItemPasses() {
        assertNull(MenuValidator.validate("Latte", "Hot Drinks", "3.50"))
    }

    @Test
    fun blankNameIsRejected() {
        assertEquals("Please enter an item name", MenuValidator.validate("  ", "Hot Drinks", "3.50"))
    }

    @Test
    fun nonNumberPriceIsRejected() {
        assertNotNull(MenuValidator.validate("Latte", "Hot Drinks", "abc"))
        assertNotNull(MenuValidator.validate("Latte", "Hot Drinks", ""))
    }

    @Test
    fun zeroAndNegativePricesAreRejected() {
        assertEquals("Price must be more than £0", MenuValidator.validate("Latte", "Hot Drinks", "0"))
        assertEquals("Price must be more than £0", MenuValidator.validate("Latte", "Hot Drinks", "-2"))
    }

    @Test
    fun priceBoundaries() {
        assertNull(MenuValidator.validate("Latte", "Hot Drinks", "0.01"))       // lowest allowed
        assertNull(MenuValidator.validate("Latte", "Hot Drinks", "100"))        // highest allowed
        assertNotNull(MenuValidator.validate("Latte", "Hot Drinks", "100.01"))  // just over
    }
}