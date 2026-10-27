package com.miladrostami.moed.reminder

import android.app.*
import android.content.*
import androidx.core.app.NotificationCompat
import com.miladrostami.moed.MainActivity
import com.miladrostami.moed.AlarmActivity
import com.miladrostami.moed.domain.AlarmScheduler

class ReminderReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){
 val nm=context.getSystemService(NotificationManager::class.java);val id=intent.getLongExtra("id",1);val title=intent.getStringExtra("title")?:"یک پرداخت نزدیک است";val alarm=intent.getBooleanExtra("alarm",false)
 if(intent.action=="com.miladrostami.moed.SNOOZE"){AlarmScheduler.schedule(context,id,title,System.currentTimeMillis()+10*60*1000L,true);nm.cancel(id.toInt());return}
 val channel=if(alarm)"moed_alarm" else "moed_due"
 if(android.os.Build.VERSION.SDK_INT>=26){val importance=if(alarm)NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT;nm.createNotificationChannel(NotificationChannel(channel,if(alarm)"آلارم سررسید" else "یادآوری سررسید",importance).apply{enableVibration(true)})}
 val openIntent=if(alarm)Intent(context,AlarmActivity::class.java).putExtra("id",id).putExtra("title",title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) else Intent(context,MainActivity::class.java)
 val open=PendingIntent.getActivity(context,id.toInt(),openIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
 val builder=NotificationCompat.Builder(context,channel).setSmallIcon(com.miladrostami.moed.R.drawable.ic_notification).setContentTitle(if(alarm)"آلارم Mento" else "یادآوری Mento").setContentText(title).setAutoCancel(true).setContentIntent(open).setPriority(if(alarm)NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT).setCategory(if(alarm)NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
 if(alarm){val snoozeIntent=Intent(context,ReminderReceiver::class.java).setAction("com.miladrostami.moed.SNOOZE").putExtra("id",id).putExtra("title",title).putExtra("alarm",true);val snooze=PendingIntent.getBroadcast(context,(id.toInt() xor 0x5A5A),snoozeIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);builder.setOngoing(true).setFullScreenIntent(open,true).addAction(android.R.drawable.ic_lock_idle_alarm,"۱۰ دقیقه بعد",snooze)}
 nm.notify(id.toInt(),builder.build())
}}
