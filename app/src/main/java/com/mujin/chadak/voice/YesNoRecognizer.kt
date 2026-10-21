package com.mujin.chadak.voice

enum class YesNo {
    YES,
    NO,
    UNKNOWN,
}

interface YesNoRecognizer {
    fun listen(onResult: (YesNo) -> Unit)

    fun stop()
}
