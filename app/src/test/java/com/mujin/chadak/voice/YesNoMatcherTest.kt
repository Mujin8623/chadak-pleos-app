package com.mujin.chadak.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class YesNoMatcherTest {
    @Test
    fun matchesKoreanYesAndNo() {
        assertEquals(YesNo.YES, YesNoMatcher.match("응"))
        assertEquals(YesNo.YES, YesNoMatcher.match(" 네, 좋아 "))
        assertEquals(YesNo.NO, YesNoMatcher.match("아니요"))
        assertEquals(YesNo.NO, YesNoMatcher.match("아니"))
    }

    @Test
    fun unknownUtteranceStaysUnknown() {
        assertEquals(YesNo.UNKNOWN, YesNoMatcher.match("나중에"))
        assertEquals(YesNo.UNKNOWN, YesNoMatcher.match(""))
    }
}
