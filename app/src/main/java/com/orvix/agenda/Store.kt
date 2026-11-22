package com.orvix.agenda

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Appointment(
    val id: Int,
    val client: String,
    val phone: String,
    val summary: String,
    val time: Long
)

object Store {
    private fun prefs(c: Context) = c.getSharedPreferences("orvix", Context.MODE_PRIVATE)

    fun all(c: Context): MutableList<Appointment> {
        val arr = JSONArray(prefs(c).getString("list", "[]"))
        val out = mutableListOf<Appointment>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(Appointment(o.getInt("id"), o.getString("client"), o.getString("phone"),
                o.getString("summary"), o.getLong("time")))
        }
        return out
    }

    private fun save(c: Context, list: List<Appointment>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("id", it.id).put("client", it.client).put("phone", it.phone)
                .put("summary", it.summary).put("time", it.time))
        }
        prefs(c).edit().putString("list", arr.toString()).apply()
    }

    fun add(c: Context, a: Appointment) { val l = all(c); l.add(a); save(c, l) }
    fun remove(c: Context, id: Int) { save(c, all(c).filter { it.id != id }) }
    fun clear(c: Context) { save(c, emptyList()) }
    fun find(c: Context, id: Int) = all(c).firstOrNull { it.id == id }
}
