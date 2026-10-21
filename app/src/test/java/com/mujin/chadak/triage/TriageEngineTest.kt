package com.mujin.chadak.triage

import com.mujin.chadak.signal.FakeScenarios
import com.mujin.chadak.signal.SignalCondition
import com.mujin.chadak.signal.SignalKind
import com.mujin.chadak.signal.VehicleSignal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TriageEngineTest {
    @Test
    fun criticallyLowBrakeFluidStopsNow() {
        val result = TriageEngine.classify(
            listOf(signal(SignalKind.BRAKE_FLUID, SignalCondition.LOW, "브레이크액")),
        )
        assertEquals(TriageLevel.STOP_NOW, result?.level)
    }

    @Test
    fun lowEngineOilIsThisWeek() {
        val result = TriageEngine.classify(
            listOf(signal(SignalKind.ENGINE_OIL, SignalCondition.LOW, "엔진오일")),
        )
        assertEquals(TriageLevel.SERVICE_THIS_WEEK, result?.level)
    }

    @Test
    fun criticallyLowOilStopsNow() {
        val result = TriageEngine.classify(
            listOf(signal(SignalKind.ENGINE_OIL, SignalCondition.CRITICALLY_LOW, "엔진오일")),
        )
        assertEquals(TriageLevel.STOP_NOW, result?.level)
    }

    @Test
    fun washerFluidIsObserve() {
        val result = TriageEngine.classify(
            listOf(signal(SignalKind.WASHER_FLUID, SignalCondition.WARNING, "워셔액")),
        )
        assertEquals(TriageLevel.OBSERVE, result?.level)
    }

    @Test
    fun powerDownStopsNow() {
        val result = TriageEngine.classify(
            listOf(signal(SignalKind.POWER_DOWN, SignalCondition.WARNING, "출력 제한")),
        )
        assertEquals(TriageLevel.STOP_NOW, result?.level)
    }

    @Test
    fun lowTireIsThisWeekAndCriticalTireStops() {
        val low = TriageEngine.classify(
            listOf(signal(SignalKind.TIRE_PRESSURE, SignalCondition.LOW, "타이어", wheel = "FL")),
        )
        val critical = TriageEngine.classify(
            listOf(signal(SignalKind.TIRE_PRESSURE, SignalCondition.CRITICALLY_LOW, "타이어", wheel = "FL")),
        )
        assertEquals(TriageLevel.SERVICE_THIS_WEEK, low?.level)
        assertEquals(TriageLevel.STOP_NOW, critical?.level)
    }

    @Test
    fun odometerDueAndSoon() {
        val due = TriageEngine.classify(
            listOf(signal(SignalKind.ODOMETER, SignalCondition.SERVICE_DUE, "점검")),
        )
        val soon = TriageEngine.classify(
            listOf(signal(SignalKind.ODOMETER, SignalCondition.SERVICE_SOON, "점검")),
        )
        assertEquals(TriageLevel.SERVICE_THIS_WEEK, due?.level)
        assertEquals(TriageLevel.OBSERVE, soon?.level)
    }

    @Test
    fun highestLevelWinsAndNormalIsIgnored() {
        val result = TriageEngine.classify(
            listOf(
                signal(SignalKind.WASHER_FLUID, SignalCondition.WARNING, "워셔액"),
                signal(SignalKind.ENGINE_OIL, SignalCondition.NORMAL, "엔진오일"),
                signal(SignalKind.BRAKE_FLUID, SignalCondition.CRITICALLY_LOW, "브레이크액"),
            ),
        )
        assertEquals(TriageLevel.STOP_NOW, result?.level)
        assertEquals(2, result?.signals?.size)
    }

    @Test
    fun emptyAndNormalProduceNoResult() {
        assertNull(TriageEngine.classify(emptyList()))
        assertNull(
            TriageEngine.classify(
                listOf(signal(SignalKind.ENGINE_OIL, SignalCondition.NORMAL, "엔진오일")),
            ),
        )
    }

    @Test
    fun scriptedStopScenarioOutranksObserve() {
        val result = TriageEngine.classify(
            FakeScenarios.signals(FakeScenarios.Scenario.STOP_WITH_OBSERVE),
        )
        assertEquals(TriageLevel.STOP_NOW, result?.level)
    }

    private fun signal(
        kind: SignalKind,
        condition: SignalCondition,
        summary: String,
        wheel: String? = null,
    ): VehicleSignal = VehicleSignal(kind, condition, summary, wheel = wheel)
}
