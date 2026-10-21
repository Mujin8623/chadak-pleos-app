package com.mujin.chadak.signal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugMockVehicleSourceTest {
    @Test
    fun tireLowAndGearParkUpdateListeners() {
        val source = DebugMockVehicleSource()
        val seen = mutableListOf<List<VehicleSignal>>()
        var reported: DrivingContext? = null
        source.start(
            object : VehicleSignalSource.Listener {
                override fun onSignals(signals: List<VehicleSignal>) {
                    seen += signals
                }

                override fun onDrivingContext(context: DrivingContext) {
                    reported = context
                }
            },
        )

        assertTrue(source.handle("TIRE_LOW"))
        assertEquals(SignalKind.TIRE_PRESSURE, source.snapshot().single().kind)
        assertTrue(source.handle("GEAR_PARK"))
        assertEquals(Gear.PARK, source.drivingContext().gear)
        assertFalse(source.handle("NOT_A_SIGNAL"))
        assertTrue(seen.isNotEmpty())
        assertEquals(Gear.PARK, reported?.gear)
    }
}
