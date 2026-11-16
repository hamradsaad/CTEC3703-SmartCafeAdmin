package com.example.smartcafeadmin

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderMessagesTest {

    @Test
    fun readyMessageSaysReadyForCollection() {
        assertEquals("Aisha's order is ready for collection", OrderMessages.statusText("Aisha", "Ready"))
    }

    @Test
    fun preparingMessage() {
        assertEquals("Tom's order is being prepared", OrderMessages.statusText("Tom", "Preparing"))
    }

    @Test
    fun unknownStatusUsesGenericMessage() {
        assertEquals("Ben's order is now Delayed", OrderMessages.statusText("Ben", "Delayed"))
    }

    @Test
    fun newOrderTitleIncludesId() {
        assertEquals("New order #12", OrderMessages.newOrderTitle(12))
    }
}