package com.orvix.agenda

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat

object Notifier {
    private const val CHANNEL = "orvix_lembretes"

    fun ensureChannel(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL) != null) return
        val ch = NotificationChannel(CHANNEL, "Lembretes de compromisso", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Avisos 20 minutos antes de cada compromisso"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            )
        }
        nm.createNotificationChannel(ch)
    }

    fun show(ctx: Context, a: Appointment) {
        ensureChannel(ctx)
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val click: Intent = if (a.phone.isNotBlank()) Util.whatsappIntent(ctx, a.phone, a.summary)
        else Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pi = PendingIntent.getActivity(ctx, a.id, click, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val mins = ((a.time - System.currentTimeMillis() + 30000) / 60000).toInt()
        val title = if (mins > 0) "Compromisso em $mins min" else "Compromisso agora"
        val hora = Util.format(a.time)
        val sb = StringBuilder(hora)
        if (a.client.isNotBlank()) sb.append("\nCliente: ").append(a.client)
        if (a.summary.isNotBlank()) sb.append("\n").append(a.summary)
        if (a.phone.isNotBlank()) sb.append("\nToque para abrir o WhatsApp")

        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_event)
            .setContentTitle(title)
            .setContentText(if (a.client.isNotBlank()) "${a.client} · $hora" else hora)
            .setStyle(NotificationCompat.BigTextStyle().bigText(sb.toString()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        nm.notify(a.id, n)
    }
}
