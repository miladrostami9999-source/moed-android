package com.miladrostami.moed.reminder

import android.app.*
import android.content.*
import androidx.core.app.NotificationCompat
import com.miladrostami.moed.MainActivity
import com.miladrostami.moed.domain.AlarmScheduler

class ReminderReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){
 val nm=context.getSystemService(NotificationManager::class.java);val id=intent.getLongExtra("id",1);val title=intent.getStringExtra("title")?:"یک پرداخت نزدیک است";val alarm=intent.getBooleanExtra("alarm",false)
 if(intent.action=="com.miladrostami.moed.SNOOZE"){AlarmScheduler.schedule(context,id,title,System.currentTimeMillis()+10*60*1000L,true);nm.cancel(id.toInt());return}
 val channel=if(alarm)"moed_alarm" else "moed_due"
 if(android.os.Build.VERSION.SDK_INT>=26){val importance=if(alarm)NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT;nm.createNotificationChannel(NotificationChannel(channel,if(alarm)"آلارم سررسید" else "یادآوری سررسید",importance).apply{enableVibration(true)})}
 val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
 val builder=NotificationCompat.Builder(context,channel).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(if(alarm)"آلارم موعد پرداخت" else "موعد پرداخت").setContentText(title).setAutoCancel(true).setContentIntent(open).setPriority(if(alarm)NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT).setCategory(if(alarm)NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
 if(alarm){val snoozeIntent=Intent(context,ReminderReceiver::class.java).setAction("com.miladrostami.moed.SNOOZE").putExtra("id",id).putExtra("title",title).putExtra("alarm",true);val snooze=PendingIntent.getBroadcast(context,(id.toInt() xor 0x5A5A),snoozeIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);builder.setOngoing(true).addAction(android.R.drawable.ic_lock_idle_alarm,"۱۰ دقیقه بعد",snooze)}
 nm.notify(id.toInt(),builder.build())
}}
