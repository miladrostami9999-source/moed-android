package com.miladrostami.moed

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomCategoriesTest {
    private val reserved = builtInCategories + CUSTOM_CATEGORY

    @Test fun newNameIsTrimmedAndAppended() {
        assertEquals(
            listOf(CustomCategory("باشگاه", "fitness")),
            upsertCustomCategory(emptyList(), "  باشگاه ", "fitness", reserved)
        )
    }

    @Test fun sameNameUpdatesOnlyTheIcon() {
        val saved = listOf(CustomCategory("Gym", "fitness"), CustomCategory("Pets", "pets"))
        assertEquals(
            listOf(CustomCategory("Gym", "star"), CustomCategory("Pets", "pets")),
            upsertCustomCategory(saved, "gym", "star", reserved)
        )
    }

    @Test fun builtInBlankAndPlaceholderNamesAreIgnored() {
        assertEquals(emptyList<CustomCategory>(), upsertCustomCategory(emptyList(), "قسط", "card", reserved))
        assertEquals(emptyList<CustomCategory>(), upsertCustomCategory(emptyList(), "آرایشی بهداشتی", "spa", reserved))
        assertEquals(emptyList<CustomCategory>(), upsertCustomCategory(emptyList(), "   ", "card", reserved))
        assertEquals(emptyList<CustomCategory>(), upsertCustomCategory(emptyList(), CUSTOM_CATEGORY, "card", reserved))
    }

    @Test fun newBuiltInCategoriesAreReserved() {
        listOf("آرایشی بهداشتی", "بیمه", "اشتراک").forEach { assert(it in builtInCategories) }
    }

    @Test fun missingCategoriesComeFromExistingPayments() {
        val saved = listOf(CustomCategory("Gym", "fitness"))
        val used = listOf(
            "Gym" to "card", "Cafe" to "cafe", "cafe" to "star",
            "قسط" to "card", "" to "card", "  " to "card"
        )
        assertEquals(listOf(CustomCategory("Cafe", "cafe")), missingCustomCategories(saved, used, reserved))
    }
}
