package com.mujin.chadak

import ai.pleos.playground.location.fused.FusedLocation
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.Mode as SttMode
import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.Mode as TtsMode
import ai.pleos.playground.vehicle.Vehicle
import android.car.VehiclePropertyIds

/**
 * Compile-time touch of the allowed Pleos artifacts and the AAOS properties the
 * driving/park gate will use. This does not open a vehicle or speech session.
 *
 * Telltale and tire-pressure listeners are system-only (PRIVATE_CAR_SAFETY) and
 * are intentionally not referenced.
 */
object PleosSdkEntryPoints {
    fun touch(): Int {
        return TtsMode.ON_DEVICE.ordinal +
            SttMode.ON_DEVICE.ordinal +
            VehiclePropertyIds.GEAR_SELECTION +
            VehiclePropertyIds.PERF_VEHICLE_SPEED +
            Vehicle::class.java.name.length +
            NaviHelper::class.java.name.length +
            TextToSpeech::class.java.name.length +
            SpeechToText::class.java.name.length +
            FusedLocation::class.java.name.length
    }
}
