package com.zbs.ntfybridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CodeExtractorTest {
    @Test fun cnBracket() = assertEquals("123456", CodeExtractor.extract("【某平台】验证码 123456，5分钟内有效"))
    @Test fun cnColon() = assertEquals("4821", CodeExtractor.extract("您的验证码是：4821，请勿泄露"))
    @Test fun codeFirst() = assertEquals("987654", CodeExtractor.extract("987654 为您的登录验证码"))
    @Test fun english() = assertEquals("246810", CodeExtractor.extract("Your verification code is 246810."))
    @Test fun noKeyword() = assertNull(CodeExtractor.extract("快递已到，取件码在柜子 123456"))
    @Test fun noDigits() = assertNull(CodeExtractor.extract("验证码已发送"))
    @Test fun longNumberIgnored() = assertNull(CodeExtractor.extract("验证码 1234567890123"))
    @Test fun hexCodeFromMail() = assertEquals(
        "0e2167f04afecf9cbfe1299c",
        CodeExtractor.extract("nodeseek邮箱动态验证登录，你的验证码是0e2167f04afecf9cbfe1299c，不要告诉他人"),
    )
    @Test fun mixedCodeWithColon() = assertEquals("A1B2C3", CodeExtractor.extract("验证码：A1B2C3，10分钟内有效"))
    @Test fun truncatedMixedCodeIsRejected() = assertNull(CodeExtractor.extract("你的验证码是0e2167f04afecf9c…"))
    @Test fun doesNotTakeDigitsInsideToken() =
        assertEquals("0e2167f04afecf9cbfe1299c", CodeExtractor.extract("邮箱登录验证码 0e2167f04afecf9cbfe1299c"))
    @Test fun plainWordAfterKeywordIgnored() = assertEquals("246810", CodeExtractor.extract("Your verification code is 246810. Please keep it safe"))
}
