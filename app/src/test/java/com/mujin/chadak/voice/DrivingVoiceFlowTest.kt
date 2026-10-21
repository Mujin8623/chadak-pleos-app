package com.mujin.chadak.voice

import com.mujin.chadak.triage.TriageLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrivingVoiceFlowTest {
    @Test
    fun yesAfterPromptAsksForAWaypoint() {
        val spoken = mutableListOf<String>()
        val recognizer = ManualYesNoRecognizer()
        val accepted = mutableListOf<TriageLevel>()
        val flow = DrivingVoiceFlow(
            announcer = PleosOnDeviceAnnouncer { spoken += it },
            recognizer = recognizer,
            onAccepted = { accepted += it },
        )

        flow.onLevel(TriageLevel.SERVICE_THIS_WEEK)
        assertTrue(spoken.single().contains("이번 주"))
        assertTrue(recognizer.isListening)
        recognizer.submit(YesNo.YES)

        assertEquals(listOf(TriageLevel.SERVICE_THIS_WEEK), accepted)
    }

    @Test
    fun noDoesNotAskForAWaypoint() {
        val recognizer = ManualYesNoRecognizer()
        var accepted = 0
        var declined = 0
        val flow = DrivingVoiceFlow(
            announcer = PleosOnDeviceAnnouncer(),
            recognizer = recognizer,
            onAccepted = { accepted += 1 },
            onDeclined = { declined += 1 },
        )

        flow.onLevel(TriageLevel.STOP_NOW)
        recognizer.submit(YesNo.NO)

        assertEquals(0, accepted)
        assertEquals(1, declined)
    }
}
