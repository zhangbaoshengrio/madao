package com.zbs.ntfybridge

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/** 无事件订阅；仅在收到验证码请求后，尝试若干次把它填入当前焦点输入框。 */
class AutoFillService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() { instance = this }
    override fun onDestroy() { if (instance === this) instance = null; handler.removeCallbacksAndMessages(null); super.onDestroy() }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    private fun fill(code: String, enter: Boolean, attempt: Int = 0) {
        val node = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (node != null && node.isEditable) {
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, code)
            }
            val ok = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            if (ok && enter) handler.postDelayed({ pressEnter() }, 150)
            if (ok) return
        }
        // 用户可能还在切换到目标 App：每 500ms 重试，最多约 10 秒
        if (attempt < 20) handler.postDelayed({ fill(code, enter, attempt + 1) }, 500)
    }

    private fun pressEnter() {
        val node = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return
        node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id)
    }

    companion object {
        @Volatile private var instance: AutoFillService? = null
        fun isRunning() = instance != null
        fun request(code: String, enter: Boolean) {
            val s = instance ?: return
            s.handler.removeCallbacksAndMessages(null)
            s.handler.post { s.fill(code, enter) }
        }
    }
}
