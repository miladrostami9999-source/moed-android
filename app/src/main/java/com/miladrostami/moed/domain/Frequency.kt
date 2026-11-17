package com.miladrostami.moed.domain

import java.util.Calendar

/** Repeat rules in the order they are shown to the user. A custom interval is stored as "days:N". */
val repeatRules = listOf("once", "monthly", "quarterly", "semiannual", "yearly")

private const val CUSTOM_DAYS_PREFIX = "days:"
const val MAX_CUSTOM_DAYS = 3650

/** Rule for "every [days] days", e.g. customDaysRule(45) == "days:45". */
fun customDaysRule(days: Int): String = CUSTOM_DAYS_PREFIX + days.coerceIn(1, MAX_CUSTOM_DAYS)

/** Days between payments for a custom rule, or 0 when [rule] is not a custom interval. */
fun repeatStepDays(rule: String): Int =
    if (rule.startsWith(CUSTOM_DAYS_PREFIX)) rule.removePrefix(CUSTOM_DAYS_PREFIX).toIntOrNull()?.coerceIn(1, MAX_CUSTOM_DAYS) ?: 0 else 0

fun isCustomRule(rule: String): Boolean = repeatStepDays(rule) > 0

/** True when the rule produces more than one payment. */
fun isRecurring(rule: String): Boolean = repeatStepMonths(rule) > 0 || isCustomRule(rule)

/** Months between two consecutive due dates; 0 means a single payment or a day-based rule. */
fun repeatStepMonths(rule: String): Int = when (rule) {
    "monthly" -> 1
    "quarterly" -> 3
    "semiannual" -> 6
    "yearly" -> 12
    else -> 0
}

/** Suggested number of payments when the user switches to [rule]. */
fun defaultInstallmentCount(rule: String): Int = when {
    rule == "monthly" -> 12
    rule == "quarterly" -> 4
    rule == "semiannual" -> 2
    rule == "yearly" -> 3
    isCustomRule(rule) -> 6
    else -> 1
}

/** Due dates (epoch millis) of a payment that starts on [start] and repeats by [rule]. */
fun scheduleDueDates(start: PersianDate, rule: String, count: Int): List<Long> {
    val days = repeatStepDays(rule)
    if (days > 0) {
        val first = start.toMillis()
        return (0 until count.coerceAtLeast(1)).map {
            Calendar.getInstance().apply { timeInMillis = first; add(Calendar.DAY_OF_YEAR, it * days) }.timeInMillis
        }
    }
    val step = repeatStepMonths(rule)
    return if (step == 0) listOf(start.toMillis())
    else (0 until count.coerceAtLeast(1)).map { addPersianMonths(start, it * step).toMillis() }
}
