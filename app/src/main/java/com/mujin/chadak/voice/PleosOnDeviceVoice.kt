package com.mujin.chadak.voice

/**
 * On-device voice stub. v0.3.0 will construct both clients with `Mode.ON_DEVICE`
 * and `Locale.KOREA`, and will not call `registerApp` (that is server/hybrid only).
 *
 * TTS: `TextToSpeech(context, Mode.ON_DEVICE)`, `initialize`, `setLocale(Locale.KOREA)`,
 * `speak`, then `request()` STT from `EventListener.onDone()`.
 * STT: `SpeechToText(context, Mode.ON_DEVICE)`, `addListener`, `request()`,
 * and [YesNoMatcher] when `onUpdated(stt, completed)` reports completed text.
 *
 * Until that wiring lands, [speak] finishes immediately so the debug yes/no
 * injector can drive the same flow.
 */
class PleosOnDeviceAnnouncer(
    private val onSpoken: (String) -> Unit = {},
) : SpeechAnnouncer {
    override fun speak(text: String, onDone: () -> Unit) {
        onSpoken(text)
        onDone()
    }

    override fun stop() = Unit
}

class ManualYesNoRecognizer : YesNoRecognizer {
    private var pending: ((YesNo) -> Unit)? = null

    val isListening: Boolean
        get() = pending != null

    override fun listen(onResult: (YesNo) -> Unit) {
        pending = onResult
    }

    override fun stop() {
        pending = null
    }

    fun submit(answer: YesNo) {
        val callback = pending
        pending = null
        callback?.invoke(answer)
    }
}
