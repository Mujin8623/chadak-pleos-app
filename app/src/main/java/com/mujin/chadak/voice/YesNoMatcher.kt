package com.mujin.chadak.voice

/**
 * SpeechToText returns text, not an intent. Match a completed utterance against
 * a small Korean yes/no set. Call this only when `completed == true`.
 */
object YesNoMatcher {
    private val yes = setOf("응", "어", "예", "네", "그래", "좋아", "응응", "네네", "맞아", "yes", "예스")
    private val no = setOf("아니", "아니요", "아뇨", "아니오", "싫어", "됐어", "no")

    fun match(text: String): YesNo {
        val tokens = text
            .trim()
            .lowercase()
            .split(Regex("""[\s.,!?~。]+"""))
            .filter { it.isNotEmpty() }
        if (tokens.any { it in yes }) {
            return YesNo.YES
        }
        if (tokens.any { it in no }) {
            return YesNo.NO
        }
        return YesNo.UNKNOWN
    }
}
