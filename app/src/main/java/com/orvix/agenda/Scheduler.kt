package com.orvix.agenda

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object Scheduler {
    const val LEAD_MS = 20 * 60 * 1000L

    private fun pending(ctx: Context, id: Int): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java).putExtra("id", id)
        return PendingIntent.getBroadcast(ctx, id, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun schedule(ctx: Context, a: Appointment) {
        val now = System.currentTimeMillis()
        if (a.time <= now) return
        var trigger = a.time - LEAD_MS
        if (trigger <= now) trigger = now + 3000 // faltam menos de 20 min: avisa já
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pending(ctx, a.id)
        if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms())
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        else
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
    }

    fun cancel(ctx: Context, id: Int) {
        (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending(ctx, id))
    }
}
