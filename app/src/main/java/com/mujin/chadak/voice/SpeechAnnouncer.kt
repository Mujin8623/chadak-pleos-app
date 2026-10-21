package com.mujin.chadak.voice

interface SpeechAnnouncer {
    fun speak(text: String, onDone: () -> Unit = {})

    fun stop()
}
