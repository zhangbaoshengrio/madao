package com.zbs.ntfybridge

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.text.format.DateUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val bg = Color.parseColor("#F2F5F3")
    private val ink = Color.parseColor("#0F1F1B")
    private val text = Color.parseColor("#12201C")
    private val muted = Color.parseColor("#51615B")
    private val teal = Color.parseColor("#0B7A5C")
    private val mint = Color.parseColor("#5EEAC0")
    private val line = Color.parseColor("#E4EAE7")

    private lateinit var content: LinearLayout
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }
        setContentView(ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
            addView(content, ViewGroup.LayoutParams(-1, -2))
        })
    }

    override fun onResume() { super.onResume(); render() }

    private fun render() {
        content.removeAllViews()
        val listenerOn = Settings.Secure.getString(contentResolver, "enabled_notification_listeners").orEmpty()
            .contains(ComponentName(this, NtfyListenerService::class.java).flattenToString())
        val a11yOn = AutoFillService.isRunning()
        val batteryOk = (getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)

        content.addView(header())
        content.addView(heroCard(listenerOn && a11yOn), lp(top = 14))
        content.addView(card().apply {
            addView(permRow("通知使用权", "读取 ntfy 的通知内容", listenerOn, "M18 8a6 6 0 0 0-12 0") {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            })
            addView(divider())
            addView(permRow("无障碍服务", "把验证码填进输入框", a11yOn, "") {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            })
            addView(divider())
            addView(permRow("电池优化", "建议设为不限制，避免被系统杀掉", batteryOk, "") {
                startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
            })
        }, lp(top = 14))
        content.addView(sourcesCard(), lp(top = 14))
        content.addView(settingsCard(), lp(top = 14))
    }

    private fun lp(top: Int = 0) = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top) }

    private fun shape(color: Int, radius: Int, stroke: Int? = null) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }

    private fun tv(t: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        this.text = t; textSize = size; setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = shape(Color.WHITE, 20)
        setPadding(dp(16), dp(6), dp(16), dp(6))
    }

    private fun divider() = View(this).apply {
        setBackgroundColor(line)
        layoutParams = LinearLayout.LayoutParams(-1, dp(1))
    }

    private fun header(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(4), 0, dp(4), 0)
        addView(tv("码到", 22f, text, true))
        addView(tv("ntfy 通知 → 自动填入并回车", 13f, muted))
    }

    private fun heroCard(running: Boolean): View {
        val last = Prefs.lastCode(this)
        val shown = if (last.length == 6) last.substring(0, 3) + " " + last.substring(3) else last.ifEmpty { "——" }
        val at = Prefs.lastCodeAt(this)
        val whenText = if (last.isEmpty()) "还没有收到验证码"
        else DateUtils.getRelativeTimeSpanString(at, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString() +
            Prefs.lastSource(this).let { if (it.isEmpty()) "" else " · 来自 $it" } + " · 已复制"

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = shape(ink, 24)
            setPadding(dp(20), dp(20), dp(20), dp(16))

            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(View(context).apply {
                    background = shape(if (running) mint else Color.parseColor("#F5A97F"), 5)
                    layoutParams = LinearLayout.LayoutParams(dp(10), dp(10)).apply { marginEnd = dp(8) }
                })
                addView(tv(if (running) "运行中" else "未就绪：请完成下方授权", 15f, Color.parseColor("#E8F1EE"), true))
            })
            addView(tv("监听 ${Prefs.sources(context).size} 个应用", 12f, Color.parseColor("#9FB5AD")), lp(top = 4))
            addView(tv("最近一次验证码", 12f, Color.parseColor("#9FB5AD")), lp(top = 18))
            addView(tv(shown, 44f, Color.WHITE, true).apply {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                letterSpacing = 0.12f
            }, lp(top = 4))
            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(tv(whenText, 13f, Color.parseColor("#9FB5AD")).apply {
                    layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                })
                if (last.isNotEmpty()) addView(Button(context).apply {
                    this.text = "复制"; isAllCaps = false; textSize = 14f
                    setTextColor(Color.parseColor("#E8F1EE"))
                    background = shape(Color.parseColor("#16302A"), 22, Color.parseColor("#2F4A42"))
                    minHeight = dp(44); minimumHeight = dp(44)
                    setPadding(dp(18), 0, dp(18), 0)
                    setOnClickListener {
                        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("code", last))
                        Toast.makeText(context, "已复制 $last", Toast.LENGTH_SHORT).show()
                    }
                })
            }, lp(top = 10))
        }
    }

    private fun permRow(title: String, sub: String, ok: Boolean, @Suppress("UNUSED_PARAMETER") icon: String, onClick: () -> Unit): View =
        LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(60)
            setOnClickListener { onClick() }
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                addView(tv(title, 15f, text, true))
                addView(tv(sub, 12f, muted))
            })
            addView(tv(if (ok) "已开启" else "去设置", 12f, if (ok) Color.parseColor("#0B6048") else Color.parseColor("#9A3608"), true).apply {
                background = shape(if (ok) Color.parseColor("#E1F5EE") else Color.parseColor("#FFEDE0"), 12)
                setPadding(dp(10), dp(5), dp(10), dp(5))
            })
        }

    private fun toggleRow(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit): View =
        LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(56)
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                addView(tv(title, 15f, text, true))
                addView(tv(sub, 12f, muted))
            })
            addView(Switch(context).apply {
                isChecked = checked
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                trackTintList = ColorStateList(states, intArrayOf(teal, Color.parseColor("#B7C3BE")))
                thumbTintList = ColorStateList.valueOf(Color.WHITE)
                setOnCheckedChangeListener { _, v -> onChange(v) }
            })
        }

    private fun settingsCard(): View = card().apply {
        addView(toggleRow("自动填入输入框", "收到验证码后写入当前焦点输入框", Prefs.autoFill(context)) { Prefs.setAutoFill(context, it) })
        addView(divider())
        addView(toggleRow("填入后自动回车", "自动提交，省去手动点确认", Prefs.autoEnter(context)) { Prefs.setAutoEnter(context, it) })
    }

    private fun sourcesCard(): View = card().apply {
        setPadding(dp(16), dp(12), dp(16), dp(6))
        addView(tv("监听的应用", 15f, text, true))
        addView(tv("打开开关的应用发来的通知，只要含验证码就会复制并填入。", 12f, muted), lp(top = 2))
        val current = Prefs.sources(context)
        val extra = current.filter { it !in Prefs.PRESETS }
        (Prefs.PRESETS.keys.toList() + extra).forEach { pkg ->
            val name = Prefs.PRESETS[pkg] ?: pkg
            addView(divider(), lp(top = 8))
            addView(toggleRow(name, if (pkg in Prefs.PRESETS) pkg else "自定义 · 长按可移除", pkg in current) {
                Prefs.setSource(context, pkg, it)
                if (pkg !in Prefs.PRESETS) render()
            }.apply {
                if (pkg !in Prefs.PRESETS) setOnLongClickListener {
                    Prefs.setSource(context, pkg, false); render(); true
                }
            })
        }
        addView(divider(), lp(top = 8))
        val input = EditText(context).apply {
            hint = "其他应用包名，如 com.example.app"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setSingleLine(); textSize = 14f; typeface = Typeface.MONOSPACE; setTextColor(this@MainActivity.text)
            background = shape(Color.parseColor("#F8FAF9"), 12, Color.parseColor("#CBD6D1"))
            setPadding(dp(12), 0, dp(12), 0)
        }
        addView(LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(input, LinearLayout.LayoutParams(0, dp(44), 1f).apply { marginEnd = dp(8) })
            addView(Button(context).apply {
                this.text = "添加"; isAllCaps = false; textSize = 14f; setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                background = shape(teal, 12)
                minHeight = dp(44); minimumHeight = dp(44)
                setPadding(dp(18), 0, dp(18), 0)
                setOnClickListener {
                    val v = input.text.toString().trim()
                    if (v.isNotEmpty()) { Prefs.setSource(context, v, true); render() }
                }
            })
        }, lp(top = 10))
        addView(tv(
            "注意：只有通知里能看到验证码才有效。若应用设置了“锁屏/通知不显示内容”（邮件类应用常见），" +
                "请在系统的通知设置里改为显示全部内容；邮件需要有新邮件通知，且通知预览里含验证码。",
            12f, Color.parseColor("#9A3608"),
        ), lp(top = 12).apply { bottomMargin = dp(10) })
    }
}
