package com.miladrostami.moed.domain

import android.icu.util.PersianCalendar
import java.util.Calendar

data class PersianDate(val year:Int,val month:Int,val day:Int){
 fun toMillis():Long=PersianCalendar().apply{clear();set(year,month-1,day,12,0)}.timeInMillis
 override fun toString()="${year}/${month.toString().padStart(2,'0')}/${day.toString().padStart(2,'0')}"
 companion object { fun from(millis:Long=System.currentTimeMillis())=PersianCalendar().apply{timeInMillis=millis}.let{PersianDate(it.get(Calendar.YEAR),it.get(Calendar.MONTH)+1,it.get(Calendar.DAY_OF_MONTH))} }
}
fun persianMonthName(month:Int)=listOf("فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند")[month-1]
fun persianMonthDays(year:Int,month:Int):Int=when{month<=6->31;month<=11->30;else->{val a=PersianDate(year,12,1).toMillis();val b=PersianDate(year+1,1,1).toMillis();((b-a)/86400000).toInt()}}
fun addPersianMonths(date:PersianDate,n:Int):PersianDate{val total=(date.year*12+date.month-1)+n;val y=total/12;val m=total%12+1;return PersianDate(y,m,date.day.coerceAtMost(persianMonthDays(y,m)))}
fun faNumber(value:Any)=value.toString().map{if(it in '0'..'9') "۰۱۲۳۴۵۶۷۸۹"[it-'0'] else it}.joinToString("")
fun formatMoney(value:Long)=faNumber("%,d".format(value))
