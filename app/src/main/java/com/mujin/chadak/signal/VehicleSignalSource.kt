package com.mujin.chadak.signal

/**
 * Source of warning-light and consumable signals, plus the gear/speed context
 * used to decide whether the detail UI may open.
 *
 * Implementations: [PleosVehicleSignalSource] on device, [FakeSignalSource] for
 * scripted demo data, and [DebugMockVehicleSource] for the tire-pressure path
 * that has no public API.
 */
interface VehicleSignalSource {
    fun start(listener: Listener)

    fun stop()

    interface Listener {
        fun onSignals(signals: List<VehicleSignal>)

        fun onDrivingContext(context: DrivingContext)
    }
}
