package com.miladrostami.moed.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.miladrostami.moed.MoedApplication
import com.miladrostami.moed.domain.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rows = (context.applicationContext as MoedApplication).database.paymentDao().getAll()
                rows.forEach { row ->
                    row.installments.filter { it.paidAt == null }.forEach { installment ->
                        val offsets=row.item.reminderOffsets.split(",").mapNotNull{it.toIntOrNull()}.ifEmpty{listOf(row.item.reminderDaysBefore)}
                        offsets.forEachIndexed{slot,days->
                            val at=AlarmScheduler.atTime(installment.dueAt-days*86_400_000L,row.item.reminderHour,row.item.reminderMinute)
                            AlarmScheduler.schedule(context,AlarmScheduler.reminderId(row.item.id,installment.sequence-1,slot,false),row.item.title,at,false)
                            if(row.item.reminderMode=="alarm"||row.item.reminderMode=="both")AlarmScheduler.schedule(context,AlarmScheduler.reminderId(row.item.id,installment.sequence-1,slot,true),row.item.title,at,true)
                        }
                    }
                }
            } finally { pending.finish() }
        }
    }
}
