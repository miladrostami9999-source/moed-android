package com.miladrostami.moed

import com.miladrostami.moed.data.Installment
import com.miladrostami.moed.domain.PersianDate
import org.junit.Assert.*
import org.junit.Test

class ReportMathTest {
    private fun day(y: Int, m: Int, d: Int) = PersianDate(y, m, d).toMillis()
    private fun bill(amount: Long, due: Long, paid: Long?) =
        Installment(paymentItemId = 1, sequence = 1, amount = amount, dueAt = due, paidAt = paid)

    @Test fun monthWindowCoversExactlyTheWholeMonth() {
        val now = day(1405, 8, 15) + 12 * 3_600_000L
        val w = reportWindow(ReportRange.MONTH, now)
        assertEquals(PersianDate(1405, 8, 1).toMillis(), w.start)
        assertEquals(PersianDate(1405, 9, 1).toMillis(), w.end)
        assertEquals(PersianDate(1405, 7, 1).toMillis(), w.previousStart)
    }

    @Test fun anchorPicksAnyMonthIncludingOtherYears() {
        val now = day(1405, 8, 15)
        val past = reportWindow(ReportRange.MONTH, now, PersianDate(1405, 5, 20))
        assertEquals(PersianDate(1405, 5, 1).toMillis(), past.start)
        assertEquals(PersianDate(1405, 6, 1).toMillis(), past.end)
        val future = reportWindow(ReportRange.MONTH, now, PersianDate(1406, 1, 9))
        assertEquals(PersianDate(1406, 1, 1).toMillis(), future.start)
        assertEquals(PersianDate(1406, 2, 1).toMillis(), future.end)
        val january = reportWindow(ReportRange.MONTH, now, PersianDate(1405, 1, 3))
        assertEquals(PersianDate(1404, 12, 1).toMillis(), january.previousStart)
    }

    @Test fun quarterWindowCrossesPersianYear() {
        val w = reportWindow(ReportRange.QUARTER, day(1405, 1, 15))
        assertEquals(PersianDate(1404, 11, 1).toMillis(), w.start)
        assertEquals(PersianDate(1405, 2, 1).toMillis(), w.end)
        assertEquals(PersianDate(1404, 8, 1).toMillis(), w.previousStart)
    }

    @Test fun yearWindowIsTheWholePersianYear() {
        val w = reportWindow(ReportRange.YEAR, day(1405, 8, 15))
        assertEquals(PersianDate(1405, 1, 1).toMillis(), w.start)
        assertEquals(PersianDate(1406, 1, 1).toMillis(), w.end)
        assertEquals(PersianDate(1404, 1, 1).toMillis(), w.previousStart)
    }

    @Test fun weekWindowIncludesTodayWhateverTheTimeOfDay() {
        val now = day(1405, 8, 15) - 3 * 3_600_000L // 09:00, before the 12:00 due time
        val w = reportWindow(ReportRange.WEEK, now)
        assertTrue(day(1405, 8, 15) in w.start until w.end) // a bill due today counts
        assertTrue(day(1405, 8, 9) in w.start until w.end)
        assertFalse(day(1405, 8, 8) in w.start until w.end)
        assertFalse(day(1405, 8, 16) in w.start until w.end)
    }

    @Test fun billPaidTodayBelongsToItsOwnMonthNotToday() {
        val today = day(1405, 8, 15)
        val lastMonthBill = bill(100, due = day(1405, 7, 20), paid = today)
        val thisMonth = reportWindow(ReportRange.MONTH, today)
        val lastMonth = reportWindow(ReportRange.MONTH, today, PersianDate(1405, 7, 1))
        assertFalse(lastMonthBill.paidDueIn(thisMonth.start, thisMonth.end))
        assertTrue(lastMonthBill.paidDueIn(lastMonth.start, lastMonth.end))
        assertEquals(0L, reportBuckets(listOf(lastMonthBill), thisMonth).sumOf { it.amount })
        assertEquals(100L, reportBuckets(listOf(lastMonthBill), lastMonth).sumOf { it.amount })
    }

    @Test fun billPaidAfterTheDeadlineStillCountsInItsDueMonth() {
        val w = reportWindow(ReportRange.MONTH, day(1405, 8, 15))
        val late = bill(300, due = day(1405, 8, 5), paid = day(1405, 9, 10))
        assertTrue(late.paidDueIn(w.start, w.end))
        assertEquals(300L, reportBuckets(listOf(late), w).sumOf { it.amount })
    }

    @Test fun billPaidEarlyCountsInTheMonthItIsDue() {
        val early = bill(400, due = day(1405, 9, 10), paid = day(1405, 8, 15))
        val nov = reportWindow(ReportRange.MONTH, day(1405, 8, 15))
        val dec = reportWindow(ReportRange.MONTH, day(1405, 8, 15), PersianDate(1405, 9, 1))
        assertFalse(early.paidDueIn(nov.start, nov.end))
        assertTrue(early.paidDueIn(dec.start, dec.end))
    }

    @Test fun unpaidAndOutOfWindowBillsAreIgnoredAndBoundsAreHalfOpen() {
        val w = reportWindow(ReportRange.MONTH, day(1405, 8, 15))
        val items = listOf(
            bill(100, w.start, w.start),            // first instant: counted, first bucket
            bill(200, w.end - 1, w.end + 50),       // last instant: counted, last bucket
            bill(500, day(1405, 8, 10), null),      // unpaid
            bill(600, w.start - 1, w.start),        // previous month
            bill(700, w.end, w.end)                 // next month starts exactly at end
        )
        val buckets = reportBuckets(items, w)
        assertEquals(300L, buckets.sumOf { it.amount })
        assertEquals(100L, buckets.first().amount)
        assertEquals(200L, buckets.last().amount)
    }

    @Test fun emptyChartIsSafe() {
        val now = day(1405, 8, 1)
        assertTrue(reportBuckets(emptyList(), reportWindow(ReportRange.MONTH, now)).all { it.amount == 0L })
    }

    @Test fun weeklyChartUsesSevenDistinctDaysAndCountsDueDates() {
        val now = day(1405, 8, 15) + 6 * 3_600_000L
        val w = reportWindow(ReportRange.WEEK, now)
        val buckets = reportBuckets(listOf(bill(50, day(1405, 8, 15), now)), w)
        assertEquals(7, buckets.size)
        assertEquals(7, buckets.map { it.label }.distinct().size)
        assertEquals(50L, buckets.last().amount)
    }
}
