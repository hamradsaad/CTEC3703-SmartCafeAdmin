package com.example.smartcafeadmin

import com.example.smartcafeadmin.logic.OrderRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderRulesTest {

    @Test
    fun pendingCanOnlyMoveToPreparing() {
        assertEquals(listOf("Preparing"), OrderRules.allowedNextStatuses("Pending"))
    }

    @Test
    fun forwardOneStepIsAllowed() {
        assertTrue(OrderRules.canChangeStatus("Pending", "Preparing"))
        assertTrue(OrderRules.canChangeStatus("Preparing", "Ready"))
        assertTrue(OrderRules.canChangeStatus("Ready", "Collected"))
    }

    @Test
    fun skippingAStepIsRejected() {
        assertFalse(OrderRules.canChangeStatus("Pending", "Ready"))
        assertFalse(OrderRules.canChangeStatus("Pending", "Collected"))
    }

    @Test
    fun goingBackwardsIsRejected() {
        assertFalse(OrderRules.canChangeStatus("Ready", "Pending"))
        assertFalse(OrderRules.canChangeStatus("Collected", "Ready"))
    }

    @Test
    fun collectedOrderCannotChange() {
        assertTrue(OrderRules.allowedNextStatuses("Collected").isEmpty())
    }

    @Test
    fun unknownStatusCannotChange() {
        assertTrue(OrderRules.allowedNextStatuses("Cancelled").isEmpty())
    }
}