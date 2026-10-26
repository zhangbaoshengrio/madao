package com.zbs.ntfybridge

/** 纯函数：从通知文本中提取验证码。没有关键字或没有可靠的验证码则返回 null。 */
object CodeExtractor {
    private const val KW = "验证码|校验码|动态码|确认码|安全码|verification code|verify code|otp|passcode|code"
    private val keyword = Regex("验证码|校验码|动态码|确认码|安全码|verification|verify|otp|passcode|code", RegexOption.IGNORE_CASE)

    // 关键字后紧跟的字母数字混合码（如 0e2167f04afecf9cbfe1299c）。要求同时含数字和字母，避免把普通英文单词当验证码。
    private val mixedNear = Regex("(?:$KW)[^0-9A-Za-z]{0,20}([0-9A-Za-z]{6,40})(?![0-9A-Za-z])(\\s*(?:…|\\.{3}))?", RegexOption.IGNORE_CASE)

    private val nearKeyword = Regex(
        "(?:$KW)[^0-9A-Za-z]{0,20}([0-9]{4,8})(?![0-9])",
        RegexOption.IGNORE_CASE,
    )
    private val afterDigits = Regex("(?<![0-9A-Za-z])([0-9]{4,8})(?![0-9A-Za-z])[^0-9]{0,12}(?:为|是)?[^0-9]{0,6}(?:验证码|校验码|动态码|code)", RegexOption.IGNORE_CASE)
    private val anyDigits = Regex("(?<![0-9A-Za-z])([0-9]{4,8})(?![0-9A-Za-z])")

    private fun isMixed(token: String) = token.any { it.isDigit() } && token.any { it.isLetter() }

    fun extract(text: String): String? {
        if (text.isBlank() || !keyword.containsMatchIn(text)) return null

        for (m in mixedNear.findAll(text)) {
            val token = m.groupValues[1]
            if (!isMixed(token)) continue
            // 后面跟着省略号：通知预览把验证码截断了，宁可不复制也不能复制残缺的码
            if (m.groupValues[2].isNotEmpty()) return null
            return token
        }

        nearKeyword.find(text)?.let { return it.groupValues[1] }
        afterDigits.find(text)?.let { return it.groupValues[1] }
        return anyDigits.find(text)?.groupValues?.get(1)
    }
}
