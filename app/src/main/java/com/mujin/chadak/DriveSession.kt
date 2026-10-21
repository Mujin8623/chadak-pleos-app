package com.mujin.chadak

import com.mujin.chadak.signal.DrivingContext
import com.mujin.chadak.signal.VehicleSignal
import com.mujin.chadak.triage.TriageResult

class DriveSession {
    var signals: List<VehicleSignal> = emptyList()
        private set
    var context: DrivingContext = DrivingContext.driving()
        private set
    var triage: TriageResult? = null
        private set
    var announcement: String = ""
        private set
    var phase: String = ""
        private set
    var waypoint: String = ""
        private set

    private val listeners = linkedSetOf<() -> Unit>()

    fun observe(listener: () -> Unit): () -> Unit {
        listeners += listener
        return { listeners -= listener }
    }

    fun updateVehicle(signals: List<VehicleSignal>, context: DrivingContext, triage: TriageResult?) {
        this.signals = signals
        this.context = context
        this.triage = triage
        if (triage == null) {
            announcement = ""
            phase = ""
            waypoint = ""
        }
        publish()
    }

    fun onSpoken(text: String) {
        announcement = text
        phase = "듣는 중"
        publish()
    }

    fun onWaypoint(message: String) {
        waypoint = message
        phase = "경유지"
        publish()
    }

    fun onDeclined() {
        phase = "안내 종료"
        publish()
    }

    private fun publish() {
        listeners.toList().forEach { it() }
    }
}
