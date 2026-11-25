package com.miladrostami.moed

import com.miladrostami.moed.data.Installment
import com.miladrostami.moed.domain.PersianDate
import org.junit.Assert.*
import org.junit.Test

class ReportMathTest {
    private fun day(y: Int, m: Int, d: Int) = PersianDate(y, m, d).toMillis()
    private fun bill(amount: Long, due: Long, paid: Long?) =
        Installment(paymentItemId = 1, sequence = 1, amount = amount, dueAt = due, paidAt = paid)
    private fun entry(amount: Long, due: Long, title: String = "x") = ReportEntry(title, "قبض", amount, due)

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
    }

    @Test fun billPaidAfterTheDeadlineStillCountsInItsDueMonth() {
        val w = reportWindow(ReportRange.MONTH, day(1405, 8, 15))
        assertTrue(bill(300, due = day(1405, 8, 5), paid = day(1405, 9, 10)).paidDueIn(w.start, w.end))
    }

    @Test fun billPaidEarlyCountsInTheMonthItIsDue() {
        val early = bill(400, due = day(1405, 9, 10), paid = day(1405, 8, 15))
        val aban = reportWindow(ReportRange.MONTH, day(1405, 8, 15))
        val azar = reportWindow(ReportRange.MONTH, day(1405, 8, 15), PersianDate(1405, 9, 1))
        assertFalse(early.paidDueIn(aban.start, aban.end))
        assertTrue(early.paidDueIn(azar.start, azar.end))
    }

    @Test fun unpaidBillsNeverCount() {
        val w = reportWindow(ReportRange.MONTH, day(1405, 8, 15))
        assertFalse(bill(500, due = day(1405, 8, 10), paid = null).paidDueIn(w.start, w.end))
    }

    @Test fun monthlyReportHasOneBarPerDayOfThatMonth() {
        val now = day(1405, 8, 15)
        assertEquals(30, reportBars(emptyList(), reportWindow(ReportRange.MONTH, now)).size)            // Aban
        assertEquals(31, reportBars(emptyList(), reportWindow(ReportRange.MONTH, now, PersianDate(1405, 1, 1))).size)
        assertEquals(29, reportBars(emptyList(), reportWindow(ReportRange.MONTH, now, PersianDate(1404, 12, 1))).size)
        val bars = reportBars(emptyList(), reportWindow(ReportRange.MONTH, now))
        assertEquals(PersianDate(1405, 8, 1), bars.first().date)
        assertEquals(PersianDate(1405, 8, 30), bars.last().date)
        assertTrue(bars.all { it.daily && it.amount == 0L && it.entries.isEmpty() })
    }

    @Test fun eachBarCarriesTheDateAmountAndTheBillsOfThatDay() {
        val w = reportWindow(ReportRange.MONTH, day(1405, 8, 15))
        val bars = reportBars(
            listOf(
                entry(2_500_000, day(1405, 8, 16), "Rent"),
                entry(300_000, day(1405, 8, 16), "Water"),
                entry(90_000, day(1405, 8, 17), "Phone")
            ), w
        )
        val sixteenth = bars.single { it.date == PersianDate(1405, 8, 16) }
        assertEquals(2_800_000L, sixteenth.amount)
        assertEquals(listOf("Rent", "Water"), sixteenth.entries.map { it.title }) // largest first
        assertEquals(90_000L, bars.single { it.date == PersianDate(1405, 8, 17) }.amount)
        assertEquals(2_890_000L, bars.sumOf { it.amount })
        assertEquals(0L, bars.single { it.date == PersianDate(1405, 8, 15) }.amount)
    }

    @Test fun billsOutsideTheWindowAreIgnoredAndBoundsAreHalfOpen() {
        val w = reportWindow(ReportRange.MONTH, day(1405, 8, 15))
        val bars = reportBars(
            listOf(
                entry(100, w.start),            // first instant: counted, first bar
                entry(200, w.end - 86_400_000L), // last day of the month
                entry(600, w.start - 1),        // previous month
                entry(700, w.end)               // next month starts exactly at end
            ), w
        )
        assertEquals(100L, bars.first().amount)
        assertEquals(300L, bars.sumOf { it.amount })
    }

    @Test fun weeklyReportHasSevenDailyBarsEndingToday() {
        val now = day(1405, 8, 15) + 6 * 3_600_000L
        val bars = reportBars(listOf(entry(50, day(1405, 8, 15)), entry(70, day(1405, 8, 9))), reportWindow(ReportRange.WEEK, now))
        assertEquals(7, bars.size)
        assertEquals(7, bars.map { it.label }.distinct().size)
        assertEquals(PersianDate(1405, 8, 9), bars.first().date)
        assertEquals(PersianDate(1405, 8, 15), bars.last().date)
        assertEquals(70L, bars.first().amount)
        assertEquals(50L, bars.last().amount)
    }

    @Test fun quarterAndYearReportsHaveOneBarPerMonth() {
        val now = day(1405, 8, 15)
        val quarter = reportBars(listOf(entry(10, day(1405, 7, 3)), entry(20, day(1405, 7, 28)), entry(5, day(1405, 6, 30))), reportWindow(ReportRange.QUARTER, now))
        assertEquals(listOf(6, 7, 8), quarter.map { it.date.month })
        assertTrue(quarter.none { it.daily })
        assertEquals(listOf(5L, 30L, 0L), quarter.map { it.amount })
        val year = reportBars(emptyList(), reportWindow(ReportRange.YEAR, now))
        assertEquals((1..12).toList(), year.map { it.date.month })
        assertEquals(12, year.map { it.label }.distinct().size)
    }

    @Test fun billPaidTodayForLastMonthDoesNotAppearInThisMonthsBars() {
        val today = day(1405, 8, 15)
        val lastMonthEntry = entry(100, day(1405, 7, 20))
        assertEquals(0L, reportBars(listOf(lastMonthEntry), reportWindow(ReportRange.MONTH, today)).sumOf { it.amount })
        assertEquals(100L, reportBars(listOf(lastMonthEntry), reportWindow(ReportRange.MONTH, today, PersianDate(1405, 7, 1))).sumOf { it.amount })
    }

    @Test fun compactAmountsAreShortAndReadable() {
        assertEquals("950", compactAmount(950))
        assertEquals("1.5K", compactAmount(1_500))
        assertEquals("500K", compactAmount(500_000))
        assertEquals("2.5M", compactAmount(2_500_000))
        assertEquals("12M", compactAmount(12_000_000))
        assertEquals("3.2B", compactAmount(3_200_000_000))
    }
}
