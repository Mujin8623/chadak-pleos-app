package com.mujin.chadak.signal

/**
 * Debug and demo injector for signals the public SDK cannot deliver.
 *
 * Tire pressure has no third-party callback. Virtual Console can flip the TPMS
 * telltale, but this app must not read telltales. The overlay button and
 * `adb shell am broadcast -a ai.chadak.DEBUG_EVENT --es type TIRE_LOW` both land here.
 *
 * Supported `type` values: TIRE_LOW, TIRE_CRITICAL, ENGINE_OIL_LOW,
 * ENGINE_OIL_CRITICAL, BRAKE_FLUID_LOW, WASHER_LOW, POWER_DOWN, ODOMETER_DUE,
 * ODOMETER_SOON, GEAR_PARK, GEAR_DRIVE, CLEAR.
 */
class DebugMockVehicleSource : VehicleSignalSource {
    private val signals = linkedMapOf<String, VehicleSignal>()
    private var context = DrivingContext(
        gear = Gear.DRIVE,
        speedMetersPerSecond = 12f,
        requiresDistractionOptimization = true,
    )
    private var listener: VehicleSignalSource.Listener? = null

    override fun start(listener: VehicleSignalSource.Listener) {
        this.listener = listener
        dispatch()
    }

    override fun stop() {
        listener = null
    }

    fun handle(type: String): Boolean {
        when (type.trim().uppercase()) {
            "TIRE_LOW" -> put(
                VehicleSignal(
                    SignalKind.TIRE_PRESSURE,
                    SignalCondition.LOW,
                    "앞왼쪽 타이어 공기압이 낮습니다.",
                    wheel = "FL",
                    reading = 180f,
                ),
            )
            "TIRE_CRITICAL" -> put(
                VehicleSignal(
                    SignalKind.TIRE_PRESSURE,
                    SignalCondition.CRITICALLY_LOW,
                    "앞왼쪽 타이어 공기압이 위험합니다.",
                    wheel = "FL",
                    reading = 100f,
                ),
            )
            "ENGINE_OIL_LOW" -> put(
                VehicleSignal(SignalKind.ENGINE_OIL, SignalCondition.LOW, "엔진오일 레벨이 낮습니다."),
            )
            "ENGINE_OIL_CRITICAL" -> put(
                VehicleSignal(
                    SignalKind.ENGINE_OIL,
                    SignalCondition.CRITICALLY_LOW,
                    "엔진오일이 위험 수준입니다.",
                ),
            )
            "BRAKE_FLUID_LOW" -> put(
                VehicleSignal(
                    SignalKind.BRAKE_FLUID,
                    SignalCondition.CRITICALLY_LOW,
                    "브레이크액이 위험 수준입니다.",
                ),
            )
            "WASHER_LOW" -> put(
                VehicleSignal(SignalKind.WASHER_FLUID, SignalCondition.WARNING, "워셔액이 부족합니다."),
            )
            "POWER_DOWN" -> put(
                VehicleSignal(
                    SignalKind.POWER_DOWN,
                    SignalCondition.WARNING,
                    "출력 제한 경고가 켜졌습니다.",
                ),
            )
            "ODOMETER_DUE" -> put(
                VehicleSignal(
                    SignalKind.ODOMETER,
                    SignalCondition.SERVICE_DUE,
                    "정기 점검 주행거리에 도달했습니다.",
                    reading = 10_000f,
                ),
            )
            "ODOMETER_SOON" -> put(
                VehicleSignal(
                    SignalKind.ODOMETER,
                    SignalCondition.SERVICE_SOON,
                    "정기 점검 주행거리가 가까워졌습니다.",
                    reading = 9_200f,
                ),
            )
            "GEAR_PARK" -> context = DrivingContext(
                gear = Gear.PARK,
                speedMetersPerSecond = 0f,
                requiresDistractionOptimization = false,
            )
            "GEAR_DRIVE" -> context = DrivingContext(
                gear = Gear.DRIVE,
                speedMetersPerSecond = 12f,
                requiresDistractionOptimization = true,
            )
            "CLEAR" -> signals.clear()
            else -> return false
        }
        dispatch()
        return true
    }

    fun snapshot(): List<VehicleSignal> = signals.values.toList()

    fun drivingContext(): DrivingContext = context

    private fun put(signal: VehicleSignal) {
        signals[signal.key()] = signal
    }

    private fun dispatch() {
        listener?.onSignals(signals.values.toList())
        listener?.onDrivingContext(context)
    }
}
