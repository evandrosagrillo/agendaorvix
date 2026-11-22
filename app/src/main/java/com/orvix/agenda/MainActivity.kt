package com.orvix.agenda

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.orvix.agenda.databinding.ActivityMainBinding
import com.orvix.agenda.databinding.ItemAppointmentBinding
import java.util.Calendar
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private var y = -1; private var mo = -1; private var d = -1
    private var h = -1; private var mi = -1

    private val askNotif = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        Notifier.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            askNotif.launch(Manifest.permission.POST_NOTIFICATIONS)

        b.etDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, yy, mm, dd ->
                y = yy; mo = mm; d = dd
                b.etDate.setText(String.format("%02d/%02d/%04d", dd, mm + 1, yy))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        b.etTime.setOnClickListener {
            TimePickerDialog(this, { _, hh, mm ->
                h = hh; mi = mm
                b.etTime.setText(String.format("%02d:%02d", hh, mm))
            }, 9, 0, true).show()
        }
        b.btnSave.setOnClickListener { save() }
        b.btnClearAll.setOnClickListener {
            if (Store.all(this).isEmpty()) return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Excluir tudo?")
                .setMessage("Todos os agendamentos serão apagados.")
                .setPositiveButton("Excluir todos") { _, _ ->
                    Store.all(this).forEach { Scheduler.cancel(this, it.id) }
                    Store.clear(this); render()
                }
                .setNegativeButton("Cancelar", null).show()
        }
        render()
    }

    private fun save() {
        if (y < 0 || h < 0) { toast("Escolha a data e o horário"); return }
        val c = Calendar.getInstance().apply { set(y, mo, d, h, mi, 0); set(Calendar.MILLISECOND, 0) }
        if (c.timeInMillis <= System.currentTimeMillis()) { toast("Esse horário já passou"); return }
        val a = Appointment(
            Random.nextInt(1, Int.MAX_VALUE),
            b.etClient.text.toString().trim(),
            b.etPhone.text.toString().trim(),
            b.etSummary.text.toString().trim(),
            c.timeInMillis
        )
        Store.add(this, a)
        Scheduler.schedule(this, a)
        y = -1; mo = -1; d = -1; h = -1; mi = -1
        listOf(b.etDate, b.etTime, b.etClient, b.etPhone, b.etSummary).forEach { it.setText("") }
        toast(if (a.time - System.currentTimeMillis() < Scheduler.LEAD_MS)
            "Agendado! Faltam menos de 20 min, avisando já" else "Agendado! Aviso 20 min antes")
        render()
    }

    private fun render() {
        val list = Store.all(this).sortedBy { it.time }
        b.listContainer.removeAllViews()
        b.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        val now = System.currentTimeMillis()
        for (a in list) {
            val row = ItemAppointmentBinding.inflate(layoutInflater, b.listContainer, false)
            row.tvWhen.text = Util.format(a.time)
            row.tvClient.text = if (a.client.isBlank()) "Sem cliente" else a.client
            row.tvSummary.text = a.summary
            row.tvSummary.visibility = if (a.summary.isBlank()) View.GONE else View.VISIBLE
            row.root.alpha = if (a.time < now) 0.55f else 1f
            if (a.phone.isBlank()) row.btnWhats.visibility = View.GONE
            row.btnWhats.setOnClickListener {
                try { startActivity(Util.whatsappIntent(this, a.phone)) }
                catch (e: Exception) { toast("WhatsApp não encontrado") }
            }
            row.btnDelete.setOnClickListener {
                Scheduler.cancel(this, a.id); Store.remove(this, a.id); render()
            }
            b.listContainer.addView(row.root)
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}
