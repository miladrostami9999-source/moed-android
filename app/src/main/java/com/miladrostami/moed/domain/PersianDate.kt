package com.miladrostami.moed.domain

import java.util.Calendar

data class PersianDate(val year: Int, val month: Int, val day: Int) {
    fun toMillis(): Long {
        val (gy, gm, gd) = jalaliToGregorian(year, month, day)
        return Calendar.getInstance().apply { clear(); set(gy, gm - 1, gd, 12, 0, 0) }.timeInMillis
    }

    override fun toString() = "$year/${month.toString().padStart(2, '0')}/${day.toString().padStart(2, '0')}"

    companion object {
        fun from(millis: Long = System.currentTimeMillis()): PersianDate {
            val c = Calendar.getInstance().apply { timeInMillis = millis }
            return gregorianToJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        }
    }
}

private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): PersianDate {
    val monthDays = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    val adjustedYear = if (gm > 2) gy + 1L else gy.toLong()
    var days = 355666L + 365L * gy + (adjustedYear + 3) / 4 - (adjustedYear + 99) / 100 +
        (adjustedYear + 399) / 400 + gd + monthDays[gm - 1]
    var jy = -1595L + 33 * (days / 12053)
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) { jy += (days - 1) / 365; days = (days - 1) % 365 }
    val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
    val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
    return PersianDate(jy.toInt(), jm.toInt(), jd.toInt())
}

private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
    val adjustedYear = jy + 1595L
    var days = -355668L + 365L * adjustedYear + (adjustedYear / 33) * 8 +
        ((adjustedYear % 33) + 3) / 4 + jd + if (jm < 7) (jm - 1) * 31L else (jm - 7) * 30L + 186
    var gy = 400L * (days / 146097)
    days %= 146097
    if (days > 36524) { days--; gy += 100 * (days / 36524); days %= 36524; if (days >= 365) days++ }
    gy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) { gy += (days - 1) / 365; days = (days - 1) % 365 }
    var remaining = (days + 1).toInt()
    val february = if ((gy % 4 == 0L && gy % 100 != 0L) || gy % 400 == 0L) 29 else 28
    val lengths = intArrayOf(0, 31, february, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var gm = 1
    while (gm <= 12 && remaining > lengths[gm]) { remaining -= lengths[gm]; gm++ }
    return Triple(gy.toInt(), gm, remaining)
}

fun persianMonthName(month: Int) = if(com.miladrostami.moed.AppLanguage.code=="en")listOf("Farvardin","Ordibehesht","Khordad","Tir","Mordad","Shahrivar","Mehr","Aban","Azar","Dey","Bahman","Esfand")[month.coerceIn(1,12)-1] else listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")[month.coerceIn(1, 12) - 1]
fun persianMonthDays(year: Int, month: Int): Int = when { month <= 6 -> 31; month <= 11 -> 30; else -> ((PersianDate(year + 1, 1, 1).toMillis() - PersianDate(year, 12, 1).toMillis()) / 86_400_000L).toInt() }
fun addPersianMonths(date: PersianDate, count: Int): PersianDate { val total = date.year * 12 + date.month - 1 + count; val year = total / 12; val month = total % 12 + 1; return PersianDate(year, month, date.day.coerceAtMost(persianMonthDays(year, month))) }
fun faNumber(value: Any) = if(com.miladrostami.moed.AppLanguage.code=="en")value.toString() else value.toString().map { if (it in '0'..'9') "۰۱۲۳۴۵۶۷۸۹"[it - '0'] else it }.joinToString("")
fun formatMoney(value: Long) = faNumber("%,d".format(value))
