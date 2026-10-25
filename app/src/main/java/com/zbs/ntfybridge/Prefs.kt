package com.zbs.ntfybridge

import android.content.Context

object Prefs {
    const val NTFY = "io.heckel.ntfy"

    /** 预设来源：包名 → 显示名 */
    val PRESETS = linkedMapOf(
        NTFY to "ntfy",
        "com.google.android.gm" to "Gmail",
        "com.tencent.androidqqmail" to "QQ 邮箱",
        "com.microsoft.office.outlook" to "Outlook",
        "org.telegram.messenger" to "Telegram",
    )

    private fun sp(c: Context) = c.getSharedPreferences("bridge", Context.MODE_PRIVATE)

    /** 监听的应用包名集合；兼容旧版单包名设置。 */
    fun sources(c: Context): Set<String> {
        val s = sp(c)
        s.getStringSet("sources", null)?.let { return it.toSet() }
        return setOf(s.getString("pkg", NTFY) ?: NTFY)
    }
    fun setSources(c: Context, v: Set<String>) = sp(c).edit().putStringSet("sources", v.toSet()).apply()
    fun setSource(c: Context, pkg: String, on: Boolean) {
        val now = sources(c).toMutableSet()
        if (on) now.add(pkg.trim()) else now.remove(pkg)
        setSources(c, now.filter { it.isNotBlank() }.toSet())
    }

    fun autoEnter(c: Context): Boolean = sp(c).getBoolean("enter", true)
    fun setAutoEnter(c: Context, v: Boolean) = sp(c).edit().putBoolean("enter", v).apply()
    fun autoFill(c: Context): Boolean = sp(c).getBoolean("fill", true)
    fun setAutoFill(c: Context, v: Boolean) = sp(c).edit().putBoolean("fill", v).apply()

    fun lastCode(c: Context): String = sp(c).getString("last", "") ?: ""
    fun lastCodeAt(c: Context): Long = sp(c).getLong("lastAt", 0L)
    fun lastSource(c: Context): String = sp(c).getString("lastSrc", "") ?: ""
    fun setLastCode(c: Context, v: String, source: String) =
        sp(c).edit().putString("last", v).putLong("lastAt", System.currentTimeMillis()).putString("lastSrc", source).apply()
}
