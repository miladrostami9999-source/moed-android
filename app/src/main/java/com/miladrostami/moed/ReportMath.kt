package com.miladrostami.moed

import com.miladrostami.moed.data.Installment
import com.miladrostami.moed.domain.*
import java.util.Calendar

enum class ReportRange { WEEK, MONTH, QUARTER, YEAR }

/** [start] inclusive, [end] exclusive. [previousStart] is where the comparison period begins (it ends at [start]). */
data class ReportWindow(val start: Long, val end: Long, val previousStart: Long, val range: ReportRange)
/** One paid bill, with what the chart needs to describe it. */
data class ReportEntry(val title: String, val category: String, val amount: Long, val dueAt: Long)

/**
 * One column of the activity chart: a day (7-day and monthly reports) or a month (3-month and yearly reports).
 * [date] is the day, or the first day of the month when [daily] is false.
 */
data class ReportBar(val date: PersianDate, val daily: Boolean, val label: String, val amount: Long, val entries: List<ReportEntry>)

private fun midnight(millis: Long, plusDays: Int): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    add(Calendar.DAY_OF_MONTH, plusDays)
}.timeInMillis

/**
 * Time window of a report.
 *
 * Reports group payments by the month the bill belongs to (its due date), never by the day it was ticked as
 * paid, so recording last month's bill today keeps it in last month.
 *
 * [anchor] chooses the month for MONTH, the last month of QUARTER and the year of YEAR.
 * WEEK always covers the last 7 days up to and including today.
 */
fun reportWindow(range: ReportRange, now: Long, anchor: PersianDate = PersianDate.from(now)): ReportWindow {
    val first = PersianDate(anchor.year, anchor.month, 1)
    return when (range) {
        ReportRange.WEEK -> ReportWindow(midnight(now, -6), midnight(now, 1), midnight(now, -13), range)
        ReportRange.MONTH -> ReportWindow(
            first.toMillis(), addPersianMonths(first, 1).toMillis(), addPersianMonths(first, -1).toMillis(), range
        )
        ReportRange.QUARTER -> ReportWindow(
            addPersianMonths(first, -2).toMillis(), addPersianMonths(first, 1).toMillis(), addPersianMonths(first, -5).toMillis(), range
        )
        ReportRange.YEAR -> ReportWindow(
            PersianDate(anchor.year, 1, 1).toMillis(), PersianDate(anchor.year + 1, 1, 1).toMillis(),
            PersianDate(anchor.year - 1, 1, 1).toMillis(), range
        )
    }
}

/** True when the installment is paid and its bill falls in [from, until), no matter when it was paid. */
fun Installment.paidDueIn(from: Long, until: Long): Boolean = paidAt != null && dueAt >= from && dueAt < until

private fun nextDay(date: PersianDate): PersianDate = PersianDate.from(date.toMillis() + 86_400_000L)

/** One bar per day or per month of [window], each with the total and the bills that fall in it (by due date). */
fun reportBars(entries: List<ReportEntry>, window: ReportWindow): List<ReportBar> {
    val daily = window.range == ReportRange.WEEK || window.range == ReportRange.MONTH
    fun bucketOf(date: PersianDate) = if (daily) date else PersianDate(date.year, date.month, 1)
    val dates = mutableListOf<PersianDate>()
    var cursor = bucketOf(PersianDate.from(window.start))
    while (cursor.toMillis() < window.end) {
        dates += cursor
        cursor = if (daily) nextDay(cursor) else addPersianMonths(cursor, 1)
    }
    val grouped = entries
        .filter { it.dueAt >= window.start && it.dueAt < window.end }
        .groupBy { bucketOf(PersianDate.from(it.dueAt)) }
    return dates.map { date ->
        val inBar = grouped[date].orEmpty().sortedByDescending { it.amount }
        val label = if (daily) "${faNumber(date.month)}/${faNumber(date.day)}" else persianMonthName(date.month)
        ReportBar(date, daily, label, inBar.sumOf { it.amount }, inBar)
    }
}
