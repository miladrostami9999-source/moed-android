package com.miladrostami.moed

import com.miladrostami.moed.data.Installment
import com.miladrostami.moed.domain.*
import java.util.Calendar

enum class ReportRange { WEEK, MONTH, QUARTER, YEAR }

/** [start] inclusive, [end] exclusive. [previousStart] is where the comparison period begins (it ends at [start]). */
data class ReportWindow(val start: Long, val end: Long, val previousStart: Long, val range: ReportRange)
data class ReportBucket(val label: String, val amount: Long)

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

fun reportBuckets(installments: List<Installment>, window: ReportWindow): List<ReportBucket> {
    val weekly = window.range == ReportRange.WEEK
    val count = if (weekly) 7 else 6
    val duration = (window.end - window.start).coerceAtLeast(1)
    val amounts = LongArray(count)
    val dayStarts = (0 until 7).map { midnight(window.start, it) }
    installments.forEach { installment ->
        if (!installment.paidDueIn(window.start, window.end)) return@forEach
        val index = if (weekly) dayStarts.indexOfLast { it <= installment.dueAt }.coerceAtLeast(0)
        else ((installment.dueAt - window.start).toDouble() / duration * count).toInt().coerceIn(0, count - 1)
        amounts[index] += installment.amount
    }
    return (0 until count).map { index ->
        val date = PersianDate.from(if (weekly) dayStarts[index] else window.start + duration * index / count)
        ReportBucket("${faNumber(date.month)}/${faNumber(date.day)}", amounts[index])
    }
}
