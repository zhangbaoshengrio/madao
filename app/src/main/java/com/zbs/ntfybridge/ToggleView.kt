package com.zbs.ntfybridge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 自绘开关：固定 52×32dp 的实心圆角轨道 + 始终落在轨道内的白色圆形滑块。
 * 不用系统 Switch，避免它按滑块宽度拉伸轨道、导致滑块伸出轨道。
 */
class ToggleView(
    context: Context,
    private val onColor: Int,
    private val offColor: Int,
    checked: Boolean,
    private val onChange: (Boolean) -> Unit,
) : View(context) {
    var isOn = checked
        private set

    private val d = resources.displayMetrics.density
    private val trackW = 52 * d
    private val trackH = 32 * d
    private val track = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumb = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        setShadowLayer(2 * d, 0f, 1 * d, 0x40000000)
    }
    private val rect = RectF()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // 让滑块阴影生效
        isClickable = true
        setOnClickListener {
            isOn = !isOn
            invalidate()
            onChange(isOn)
        }
        accessibilityDelegate = object : AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = "android.widget.Switch"
                info.isCheckable = true
                info.isChecked = isOn
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(trackW.toInt(), trackH.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        track.color = if (isOn) onColor else offColor
        rect.set(0f, 0f, trackW, trackH)
        canvas.drawRoundRect(rect, trackH / 2, trackH / 2, track)
        val r = 13 * d
        val cx = if (isOn) trackW - trackH / 2 else trackH / 2
        canvas.drawCircle(cx, trackH / 2, r, thumb)
    }
}
