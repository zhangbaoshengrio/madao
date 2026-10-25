package com.zbs.ntfybridge

import android.content.Context

object Prefs {
    const val DEFAULT_PACKAGE = "io.heckel.ntfy"
    private fun sp(c: Context) = c.getSharedPreferences("bridge", Context.MODE_PRIVATE)

    fun sourcePackage(c: Context): String = sp(c).getString("pkg", DEFAULT_PACKAGE) ?: DEFAULT_PACKAGE
    fun setSourcePackage(c: Context, v: String) = sp(c).edit().putString("pkg", v.trim().ifEmpty { DEFAULT_PACKAGE }).apply()
    fun autoEnter(c: Context): Boolean = sp(c).getBoolean("enter", true)
    fun setAutoEnter(c: Context, v: Boolean) = sp(c).edit().putBoolean("enter", v).apply()
    fun autoFill(c: Context): Boolean = sp(c).getBoolean("fill", true)
    fun setAutoFill(c: Context, v: Boolean) = sp(c).edit().putBoolean("fill", v).apply()
    fun lastCode(c: Context): String = sp(c).getString("last", "") ?: ""
    fun lastCodeAt(c: Context): Long = sp(c).getLong("lastAt", 0L)
    fun setLastCode(c: Context, v: String) =
        sp(c).edit().putString("last", v).putLong("lastAt", System.currentTimeMillis()).apply()
}
