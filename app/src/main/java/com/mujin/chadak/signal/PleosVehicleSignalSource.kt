package com.mujin.chadak.signal

/**
 * Stub for the real Pleos Vehicle connection.
 *
 * v0.2.0 will construct `Vehicle(context)`, call `initialize()`, then register
 * only the public listeners:
 * - `Safety.registerEngineOilLevelState` / `getEngineOilLevelState` (CAR_SAFETY)
 * - `Safety.registerBrakeFluidLevelState` / `getBrakeFluidLevelState`
 * - `Safety.registerLowWasherFluidWaringState` (method spelling is "Waring")
 * - `Safety.registerPowerDownWarningIndicator`
 * - `Odometer.registerDistanceDriven` (CAR_DRIVE)
 *
 * Do not call `Safety.registerTelltales`, `Safety.getTelltalesInfo`,
 * `Tire.registerTirePressure`, or `Tire.getTirePressure`. Those require
 * PRIVATE_CAR_SAFETY and are blocked for third-party apps. `Tire.getLowTirePressure`
 * returns a threshold in kPa, not a warning, so it is not a TPMS signal.
 * TPMS events come from [DebugMockVehicleSource].
 *
 * Do not call the hidden, deprecated `Gear.registerGearSelection`. Park versus
 * drive will use `CarPropertyManager` with `VehiclePropertyIds.GEAR_SELECTION`
 * (`android.car.permission.CAR_POWERTRAIN`) and `PERF_VEHICLE_SPEED`
 * (`android.car.permission.CAR_SPEED`), plus `CarUxRestrictionsManager`.
 */
class PleosVehicleSignalSource : VehicleSignalSource {
    private var listener: VehicleSignalSource.Listener? = null

    override fun start(listener: VehicleSignalSource.Listener) {
        this.listener = listener
        listener.onSignals(emptyList())
    }

    override fun stop() {
        listener = null
    }
}
