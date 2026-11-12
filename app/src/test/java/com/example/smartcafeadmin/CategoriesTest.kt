package com.example.smartcafeadmin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoriesTest {

    @Test
    fun allCategoriesArePresent() {
        assertEquals(
            listOf("Hot Drinks", "Cold Drinks", "Food", "Snacks", "Desserts"),
            Categories.ALL
        )
    }

    @Test
    fun categoriesHaveNoDuplicates() {
        assertEquals(Categories.ALL.size, Categories.ALL.toSet().size)
    }

    @Test
    fun knownCategoriesGetTheirEmoji() {
        assertEquals("☕", Categories.emojiFor("Hot Drinks"))
        assertEquals("🧃", Categories.emojiFor("Cold Drinks"))
        assertEquals("🥪", Categories.emojiFor("Food"))
        assertEquals("🍪", Categories.emojiFor("Snacks"))
        assertEquals("🍰", Categories.emojiFor("Desserts"))
    }

    @Test
    fun unknownCategoryGetsDefaultEmoji() {
        assertEquals("🍽️", Categories.emojiFor("Breakfast"))
        assertEquals("🍽️", Categories.emojiFor(""))
    }

    @Test
    fun everyCategoryHasItsOwnEmoji() {
        assertTrue(Categories.ALL.none { Categories.emojiFor(it) == "🍽️" })
    }
}