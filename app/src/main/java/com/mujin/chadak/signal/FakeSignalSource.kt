package com.mujin.chadak.signal

/**
 * Scripted signals for debug and demo builds. This does not talk to the vehicle.
 */
class FakeSignalSource : VehicleSignalSource {
    private var listener: VehicleSignalSource.Listener? = null
    private var signals: List<VehicleSignal> = emptyList()
    private var context: DrivingContext? = null

    override fun start(listener: VehicleSignalSource.Listener) {
        this.listener = listener
        listener.onSignals(signals)
        context?.let(listener::onDrivingContext)
    }

    override fun stop() {
        listener = null
    }

    fun play(scenario: FakeScenarios.Scenario) {
        signals = FakeScenarios.signals(scenario)
        context = FakeScenarios.context(scenario)
        listener?.onSignals(signals)
        context?.let { listener?.onDrivingContext(it) }
    }
}

object FakeScenarios {
    enum class Scenario {
        IDLE,
        TIRE_LOW,
        OIL_LOW,
        WASHER,
        STOP_WITH_OBSERVE,
        PARKED,
    }

    fun signals(scenario: Scenario): List<VehicleSignal> = when (scenario) {
        Scenario.IDLE, Scenario.PARKED -> emptyList()
        Scenario.TIRE_LOW -> listOf(
            VehicleSignal(
                kind = SignalKind.TIRE_PRESSURE,
                condition = SignalCondition.LOW,
                summary = "앞왼쪽 타이어 공기압이 낮습니다.",
                wheel = "FL",
                reading = 180f,
            ),
        )
        Scenario.OIL_LOW -> listOf(
            VehicleSignal(
                kind = SignalKind.ENGINE_OIL,
                condition = SignalCondition.LOW,
                summary = "엔진오일 레벨이 낮습니다.",
            ),
        )
        Scenario.WASHER -> listOf(
            VehicleSignal(
                kind = SignalKind.WASHER_FLUID,
                condition = SignalCondition.WARNING,
                summary = "워셔액이 부족합니다.",
            ),
        )
        Scenario.STOP_WITH_OBSERVE -> listOf(
            VehicleSignal(
                kind = SignalKind.BRAKE_FLUID,
                condition = SignalCondition.CRITICALLY_LOW,
                summary = "브레이크액이 위험 수준입니다.",
            ),
            VehicleSignal(
                kind = SignalKind.WASHER_FLUID,
                condition = SignalCondition.WARNING,
                summary = "워셔액이 부족합니다.",
            ),
        )
    }

    fun context(scenario: Scenario): DrivingContext? = when (scenario) {
        Scenario.PARKED -> DrivingContext(
            gear = Gear.PARK,
            speedMetersPerSecond = 0f,
            requiresDistractionOptimization = false,
        )
        Scenario.IDLE -> null
        else -> DrivingContext.driving()
    }
}
