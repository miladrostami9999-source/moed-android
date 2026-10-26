package com.miladrostami.moed.reminder

import android.app.*
import android.content.*
import androidx.core.app.NotificationCompat
import com.miladrostami.moed.MainActivity

class ReminderReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){
 val nm=context.getSystemService(NotificationManager::class.java);val channel="moed_due"
 if(android.os.Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(NotificationChannel(channel,"یادآوری سررسید",NotificationManager.IMPORTANCE_HIGH))
 val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
 val n=NotificationCompat.Builder(context,channel).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("موعد پرداخت").setContentText(intent.getStringExtra("title")?:"یک پرداخت نزدیک است").setAutoCancel(true).setContentIntent(open).setPriority(NotificationCompat.PRIORITY_HIGH).build()
 nm.notify(intent.getLongExtra("id",1).toInt(),n)
}}
