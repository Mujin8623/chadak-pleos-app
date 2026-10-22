package com.mujin.chadak.ui

import com.mujin.chadak.signal.DrivingContext
import com.mujin.chadak.signal.Gear
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrivingPolicyTest {
    @Test
    fun detailOpensOnlyInParkWithoutDistractionOptimization() {
        assertTrue(
            DrivingPolicy.showDetailUi(
                DrivingContext(Gear.PARK, 0f, requiresDistractionOptimization = false),
            ),
        )
        assertFalse(
            DrivingPolicy.showDetailUi(
                DrivingContext(Gear.PARK, 0f, requiresDistractionOptimization = true),
            ),
        )
        assertFalse(
            DrivingPolicy.showDetailUi(
                DrivingContext(Gear.DRIVE, 12f, requiresDistractionOptimization = false),
            ),
        )
        assertFalse(DrivingPolicy.showDetailUi(DrivingContext.driving()))
    }
}
