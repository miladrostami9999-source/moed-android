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
                        AlarmScheduler.schedule(
                            context,
                            row.item.id * 1000 + installment.sequence - 1,
                            row.item.title,
                            installment.dueAt - row.item.reminderDaysBefore * 86_400_000L,
                            row.item.reminderMode != "notification"
                        )
                    }
                }
            } finally { pending.finish() }
        }
    }
}
