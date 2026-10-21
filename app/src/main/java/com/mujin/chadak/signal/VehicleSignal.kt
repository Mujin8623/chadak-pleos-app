package com.mujin.chadak.signal

enum class SignalKind {
    ENGINE_OIL,
    BRAKE_FLUID,
    WASHER_FLUID,
    POWER_DOWN,
    TIRE_PRESSURE,
    ODOMETER,
}

/**
 * Conditions mirror the public Vehicle SDK values we are allowed to consume.
 * Oil and brake fluid use OilLevelState (LOW, HIGH, NORMAL, ERROR, CRITICALLY_LOW).
 * Washer fluid and power-down are boolean warnings. Tire pressure is debug-mock only.
 */
enum class SignalCondition {
    NORMAL,
    LOW,
    HIGH,
    CRITICALLY_LOW,
    ERROR,
    WARNING,
    SERVICE_DUE,
    SERVICE_SOON,
}

data class VehicleSignal(
    val kind: SignalKind,
    val condition: SignalCondition,
    val summary: String,
    val wheel: String? = null,
    val reading: Float? = null,
) {
    fun key(): String = kind.name + ":" + (wheel ?: "")
}

enum class Gear {
    PARK,
    REVERSE,
    NEUTRAL,
    DRIVE,
    UNKNOWN,
}

data class DrivingContext(
    val gear: Gear,
    val speedMetersPerSecond: Float?,
    val requiresDistractionOptimization: Boolean,
) {
    companion object {
        fun driving(): DrivingContext = DrivingContext(
            gear = Gear.DRIVE,
            speedMetersPerSecond = null,
            requiresDistractionOptimization = true,
        )
    }
}
