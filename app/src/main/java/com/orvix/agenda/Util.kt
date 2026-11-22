package com.orvix.agenda

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Locale

object Util {
    fun format(time: Long): String =
        SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(time)

    fun normalizePhone(raw: String): String {
        val d = raw.filter { it.isDigit() }
        return if (d.length in 10..11) "55$d" else d
    }

    private fun installed(ctx: Context, pkg: String) =
        try { ctx.packageManager.getPackageInfo(pkg, 0); true } catch (e: Exception) { false }

    /** Abre direto a conversa no WhatsApp Business (ou WhatsApp comum se não houver). */
    fun whatsappIntent(ctx: Context, phone: String): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + normalizePhone(phone)))
        listOf("com.whatsapp.w4b", "com.whatsapp").firstOrNull { installed(ctx, it) }?.let { i.setPackage(it) }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return i
    }
}
