package com.miladrostami.moed.domain

import android.app.*
import android.content.*
import android.os.Build
import com.miladrostami.moed.reminder.ReminderReceiver

object AlarmScheduler {
 fun atTime(day:Long,hour:Int,minute:Int):Long=java.util.Calendar.getInstance().apply{timeInMillis=day;set(java.util.Calendar.HOUR_OF_DAY,hour);set(java.util.Calendar.MINUTE,minute);set(java.util.Calendar.SECOND,0);set(java.util.Calendar.MILLISECOND,0)}.timeInMillis
 fun schedule(context:Context,id:Long,title:String,at:Long,alarm:Boolean){
  if(at<=System.currentTimeMillis())return
  val manager=context.getSystemService(AlarmManager::class.java)
  val intent=Intent(context,ReminderReceiver::class.java).putExtra("title",title).putExtra("id",id)
  val pi=PendingIntent.getBroadcast(context,id.toInt(),intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  if(alarm && (Build.VERSION.SDK_INT<31 || manager.canScheduleExactAlarms())) manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi)
  else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi)
 }
 fun cancel(context:Context,id:Long){
  val intent=Intent(context,ReminderReceiver::class.java)
  val pi=PendingIntent.getBroadcast(context,id.toInt(),intent,PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
  if(pi!=null){context.getSystemService(AlarmManager::class.java).cancel(pi);pi.cancel()}
 }
}
