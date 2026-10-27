package com.miladrostami.moed

import com.miladrostami.moed.data.Installment
import com.miladrostami.moed.domain.*

enum class ReportRange { WEEK, MONTH, QUARTER, YEAR }
data class ReportWindow(val start:Long,val end:Long,val previousStart:Long,val range:ReportRange)
data class ReportBucket(val label:String,val amount:Long)

fun reportWindow(range:ReportRange,now:Long):ReportWindow {
 val p=PersianDate.from(now);val first=PersianDate(p.year,p.month,1)
 val start=when(range){
  ReportRange.WEEK->java.util.Calendar.getInstance().apply{timeInMillis=now;set(java.util.Calendar.HOUR_OF_DAY,0);set(java.util.Calendar.MINUTE,0);set(java.util.Calendar.SECOND,0);set(java.util.Calendar.MILLISECOND,0);add(java.util.Calendar.DAY_OF_MONTH,-6)}.timeInMillis
  ReportRange.MONTH->first.toMillis()
  ReportRange.QUARTER->addPersianMonths(first,-2).toMillis()
  ReportRange.YEAR->PersianDate(p.year,1,1).toMillis()
 }
 val end=when(range){ReportRange.WEEK->now+1;ReportRange.MONTH,ReportRange.QUARTER->addPersianMonths(first,1).toMillis();ReportRange.YEAR->PersianDate(p.year+1,1,1).toMillis()}
 return ReportWindow(start,end,start-(now-start+1),range)
}

fun reportBuckets(installments:List<Installment>,window:ReportWindow,now:Long):List<ReportBucket>{
 val count=if(window.range==ReportRange.WEEK)7 else ((now-window.start)/86_400_000L+1).coerceIn(1,6).toInt()
 val duration=(now-window.start+1).coerceAtLeast(1)
 val amounts=LongArray(count)
 val weekStarts=(0 until 7).map{day->java.util.Calendar.getInstance().apply{timeInMillis=window.start;add(java.util.Calendar.DAY_OF_MONTH,day)}.timeInMillis}
 installments.forEach{i->i.paidAt?.takeIf{it in window.start..now}?.let{time->val index=if(window.range==ReportRange.WEEK)weekStarts.indexOfLast{it<=time}.coerceAtLeast(0) else ((time-window.start).toDouble()/duration*count).toInt().coerceIn(0,count-1);amounts[index]+=i.amount}}
 return (0 until count).map{index->val p=PersianDate.from(if(window.range==ReportRange.WEEK)weekStarts[index]else window.start+duration*index/count);ReportBucket("${faNumber(p.month)}/${faNumber(p.day)}",amounts[index])}
}
