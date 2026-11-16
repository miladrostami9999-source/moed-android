package com.miladrostami.moed.domain

/** Repeat rules in the order they are shown to the user. */
val repeatRules = listOf("once", "monthly", "quarterly", "semiannual", "yearly")

/** Months between two consecutive due dates; 0 means a single payment. */
fun repeatStepMonths(rule: String): Int = when (rule) {
    "monthly" -> 1
    "quarterly" -> 3
    "semiannual" -> 6
    "yearly" -> 12
    else -> 0
}

/** Suggested number of payments when the user switches to [rule]. */
fun defaultInstallmentCount(rule: String): Int = when (rule) {
    "monthly" -> 12
    "quarterly" -> 4
    "semiannual" -> 2
    "yearly" -> 3
    else -> 1
}

/** Due dates (epoch millis) of a payment that starts on [start] and repeats by [rule]. */
fun scheduleDueDates(start: PersianDate, rule: String, count: Int): List<Long> {
    val step = repeatStepMonths(rule)
    return if (step == 0) listOf(start.toMillis())
    else (0 until count.coerceAtLeast(1)).map { addPersianMonths(start, it * step).toMillis() }
}
