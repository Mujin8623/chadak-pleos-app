package com.mujin.chadak.triage

import com.mujin.chadak.signal.SignalCondition
import com.mujin.chadak.signal.SignalKind
import com.mujin.chadak.signal.VehicleSignal

/**
 * Pure mapping from vehicle signals to one of three levels.
 * The result level is the highest level among active signals.
 *
 * Brake fluid LOW/CRITICALLY_LOW/ERROR and power-down stop the car.
 * Engine oil CRITICALLY_LOW/ERROR stops the car; LOW/HIGH is this-week service.
 * Low tire pressure is this-week service; critically low tire pressure stops the car.
 * Washer fluid is observe-only. Odometer due is this-week service; odometer soon is observe.
 * NORMAL and unknown combinations produce no level.
 */
object TriageEngine {
    fun classify(signals: List<VehicleSignal>): TriageResult? {
        val ranked = signals.mapNotNull { signal ->
            levelOf(signal)?.let { level -> signal to level }
        }
        if (ranked.isEmpty()) {
            return null
        }
        val level = ranked.maxBy { it.second.priority }.second
        val reason = ranked
            .sortedByDescending { it.second.priority }
            .joinToString(separator = " ") { (signal, signalLevel) ->
                "${signal.summary} (${signalLevel.label})"
            }
        return TriageResult(
            level = level,
            signals = ranked.map { it.first },
            reason = reason,
        )
    }

    fun levelOf(signal: VehicleSignal): TriageLevel? = when (signal.kind) {
        SignalKind.ENGINE_OIL -> when (signal.condition) {
            SignalCondition.CRITICALLY_LOW, SignalCondition.ERROR -> TriageLevel.STOP_NOW
            SignalCondition.LOW, SignalCondition.HIGH -> TriageLevel.SERVICE_THIS_WEEK
            else -> null
        }
        SignalKind.BRAKE_FLUID -> when (signal.condition) {
            SignalCondition.CRITICALLY_LOW, SignalCondition.LOW, SignalCondition.ERROR ->
                TriageLevel.STOP_NOW
            SignalCondition.HIGH -> TriageLevel.SERVICE_THIS_WEEK
            else -> null
        }
        SignalKind.WASHER_FLUID -> when (signal.condition) {
            SignalCondition.WARNING, SignalCondition.LOW -> TriageLevel.OBSERVE
            else -> null
        }
        SignalKind.POWER_DOWN -> when (signal.condition) {
            SignalCondition.WARNING, SignalCondition.ERROR -> TriageLevel.STOP_NOW
            else -> null
        }
        SignalKind.TIRE_PRESSURE -> when (signal.condition) {
            SignalCondition.CRITICALLY_LOW -> TriageLevel.STOP_NOW
            SignalCondition.LOW, SignalCondition.WARNING -> TriageLevel.SERVICE_THIS_WEEK
            else -> null
        }
        SignalKind.ODOMETER -> when (signal.condition) {
            SignalCondition.SERVICE_DUE -> TriageLevel.SERVICE_THIS_WEEK
            SignalCondition.SERVICE_SOON -> TriageLevel.OBSERVE
            else -> null
        }
    }
}
