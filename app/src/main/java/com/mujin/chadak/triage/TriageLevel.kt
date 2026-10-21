package com.mujin.chadak.triage

import com.mujin.chadak.signal.VehicleSignal

enum class TriageLevel(val label: String, val priority: Int) {
    OBSERVE("관찰", 1),
    SERVICE_THIS_WEEK("이번 주 정비", 2),
    STOP_NOW("즉시 정차", 3),
}

data class TriageResult(
    val level: TriageLevel,
    val signals: List<VehicleSignal>,
    val reason: String,
) {
    val signature: String
        get() = level.name + ":" + signals.joinToString(",") { it.key() + it.condition.name }
}
