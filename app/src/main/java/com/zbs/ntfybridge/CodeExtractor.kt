package com.zbs.ntfybridge

/** 纯函数：从通知文本中提取验证码。没有关键字或没有数字则返回 null。 */
object CodeExtractor {
    private val keyword = Regex("验证码|校验码|动态码|确认码|安全码|verification|verify|otp|passcode|code", RegexOption.IGNORE_CASE)
    private val nearKeyword = Regex(
        "(?:验证码|校验码|动态码|确认码|安全码|verification code|verify code|otp|passcode|code)[^0-9A-Za-z]{0,20}([0-9]{4,8})(?![0-9])",
        RegexOption.IGNORE_CASE,
    )
    private val afterDigits = Regex("(?<![0-9])([0-9]{4,8})(?![0-9])[^0-9]{0,12}(?:为|是)?[^0-9]{0,6}(?:验证码|校验码|动态码|code)", RegexOption.IGNORE_CASE)
    private val anyDigits = Regex("(?<![0-9])([0-9]{4,8})(?![0-9])")

    fun extract(text: String): String? {
        if (text.isBlank() || !keyword.containsMatchIn(text)) return null
        nearKeyword.find(text)?.let { return it.groupValues[1] }
        afterDigits.find(text)?.let { return it.groupValues[1] }
        return anyDigits.find(text)?.groupValues?.get(1)
    }
}
