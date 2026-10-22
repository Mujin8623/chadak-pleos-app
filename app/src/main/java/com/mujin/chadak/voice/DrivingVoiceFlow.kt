package com.mujin.chadak.voice

import com.mujin.chadak.triage.TriageLevel

/**
 * Driving-time flow: speak the level, then listen for yes/no.
 * A yes answer asks the navigator to add a repair shop. Detail UI stays closed.
 */
class DrivingVoiceFlow(
    private val announcer: SpeechAnnouncer,
    private val recognizer: YesNoRecognizer,
    private val onAccepted: (TriageLevel) -> Unit,
    private val onDeclined: (TriageLevel) -> Unit = {},
) {
    fun onLevel(level: TriageLevel) {
        announcer.speak(Announcements.prompt(level)) {
            recognizer.listen { answer ->
                if (answer == YesNo.YES) {
                    onAccepted(level)
                } else {
                    onDeclined(level)
                }
            }
        }
    }

    fun stop() {
        announcer.stop()
        recognizer.stop()
    }
}
