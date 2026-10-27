package com.miladrostami.moed.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class PersianDateTest {
    @Test fun nowruzConversionIsCorrect() {
        val calendar = Calendar.getInstance().apply { clear(); set(2025, Calendar.MARCH, 21, 12, 0) }
        assertEquals(PersianDate(1404, 1, 1), PersianDate.from(calendar.timeInMillis))
        assertEquals(calendar.get(Calendar.YEAR), Calendar.getInstance().apply { timeInMillis = PersianDate(1404,1,1).toMillis() }.get(Calendar.YEAR))
    }

    @Test fun addingMonthClampsEsfandDay() {
        assertEquals(PersianDate(1403, 12, 30), addPersianMonths(PersianDate(1403, 11, 30), 1))
        assertEquals(PersianDate(1404, 12, 29), addPersianMonths(PersianDate(1404, 11, 30), 1))
    }
}
