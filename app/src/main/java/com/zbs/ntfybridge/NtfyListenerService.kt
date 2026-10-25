package com.zbs.ntfybridge

import android.app.Notification
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** 事件驱动：只在有新通知时被系统唤醒，不轮询、不持有唤醒锁。 */
class NtfyListenerService : NotificationListenerService() {
    private var lastKey = ""
    private var lastAt = 0L

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != Prefs.sourcePackage(this)) return
        val e = sbn.notification.extras
        val text = listOfNotNull(
            e.getCharSequence(Notification.EXTRA_TITLE),
            e.getCharSequence(Notification.EXTRA_TEXT),
            e.getCharSequence(Notification.EXTRA_BIG_TEXT),
        ).joinToString("\n")
        val code = CodeExtractor.extract(text) ?: return

        // 同一条通知被更新（ntfy 常重复 post）时 30 秒内不重复处理
        val now = System.currentTimeMillis()
        val key = sbn.key + code
        if (key == lastKey && now - lastAt < 30_000) return
        lastKey = key; lastAt = now

        Prefs.setLastCode(this, code)
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("code", code))
        if (Prefs.autoFill(this)) AutoFillService.request(code, Prefs.autoEnter(this))
    }
}
