package com.miladrostami.moed.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FrequencyTest {
    private fun dates(start: PersianDate, rule: String, count: Int) =
        scheduleDueDates(start, rule, count).map { PersianDate.from(it) }

    @Test fun quarterlyAddsThreeMonths() {
        assertEquals(
            listOf(PersianDate(1405, 1, 15), PersianDate(1405, 4, 15), PersianDate(1405, 7, 15), PersianDate(1405, 10, 15)),
            dates(PersianDate(1405, 1, 15), "quarterly", 4)
        )
    }

    @Test fun semiannualCrossesTheYearBoundary() {
        assertEquals(
            listOf(PersianDate(1405, 8, 10), PersianDate(1406, 2, 10), PersianDate(1406, 8, 10)),
            dates(PersianDate(1405, 8, 10), "semiannual", 3)
        )
    }

    @Test fun yearlyKeepsTheSameDate() {
        assertEquals(
            listOf(PersianDate(1405, 5, 20), PersianDate(1406, 5, 20), PersianDate(1407, 5, 20)),
            dates(PersianDate(1405, 5, 20), "yearly", 3)
        )
    }

    @Test fun monthlyStillAddsOneMonth() {
        assertEquals(
            listOf(PersianDate(1405, 11, 5), PersianDate(1405, 12, 5), PersianDate(1406, 1, 5)),
            dates(PersianDate(1405, 11, 5), "monthly", 3)
        )
    }

    @Test fun onceReturnsASingleDateWhateverTheCount() {
        assertEquals(listOf(PersianDate(1405, 3, 3)), dates(PersianDate(1405, 3, 3), "once", 5))
    }

    @Test fun monthEndIsClampedInShortMonths() {
        // 1404 is not a leap year, so Esfand has 29 days.
        assertEquals(
            listOf(PersianDate(1404, 6, 31), PersianDate(1404, 12, 29)),
            dates(PersianDate(1404, 6, 31), "semiannual", 2)
        )
    }

    @Test fun stepsAndDefaultsMatchTheRules() {
        assertEquals(listOf(0, 1, 3, 6, 12), repeatRules.map(::repeatStepMonths))
        assertEquals(listOf(1, 12, 4, 2, 3), repeatRules.map(::defaultInstallmentCount))
    }
}
