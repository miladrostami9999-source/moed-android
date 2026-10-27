package com.miladrostami.moed

import com.miladrostami.moed.data.Installment
import com.miladrostami.moed.domain.PersianDate
import org.junit.Assert.*
import org.junit.Test

class ReportMathTest {
 @Test fun monthWindowDoesNotIncludeOtherMonths(){
  val now=PersianDate(1405,8,15).toMillis()+12*3_600_000L
  val w=reportWindow(ReportRange.MONTH,now)
  assertEquals(PersianDate(1405,8,1).toMillis(),w.start)
  assertEquals(PersianDate(1405,9,1).toMillis(),w.end)
  assertEquals(now-w.start+1,w.start-w.previousStart)
 }
 @Test fun quarterWindowCrossesPersianYear(){
  val now=PersianDate(1405,1,15).toMillis()
  val w=reportWindow(ReportRange.QUARTER,now)
  assertEquals(PersianDate(1404,11,1).toMillis(),w.start)
  assertEquals(PersianDate(1405,2,1).toMillis(),w.end)
 }
 @Test fun chartTotalsMatchPaidAmountsAndIgnoreUnpaidAndFuture(){
  val now=PersianDate(1405,8,15).toMillis()
  val w=reportWindow(ReportRange.MONTH,now)
  fun item(amount:Long,paid:Long?)=Installment(paymentItemId=1,sequence=1,amount=amount,dueAt=now,paidAt=paid)
  val buckets=reportBuckets(listOf(item(100,w.start),item(200,now),item(500,null),item(600,w.start-1),item(700,now+1)),w,now)
  assertEquals(300L,buckets.sumOf{it.amount})
  assertEquals(100L,buckets.first().amount)
  assertEquals(200L,buckets.last().amount)
 }
 @Test fun emptyChartIsSafe(){
  val now=PersianDate(1405,8,1).toMillis()
  assertTrue(reportBuckets(emptyList(),reportWindow(ReportRange.MONTH,now),now).all{it.amount==0L})
 }
 @Test fun weeklyChartUsesSevenDistinctDays(){
  val now=PersianDate(1405,8,15).toMillis()+12*3_600_000L
  val buckets=reportBuckets(emptyList(),reportWindow(ReportRange.WEEK,now),now)
  assertEquals(7,buckets.size)
  assertEquals(7,buckets.map{it.label}.distinct().size)
 }
}
