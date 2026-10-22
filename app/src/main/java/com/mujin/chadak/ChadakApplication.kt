package com.mujin.chadak

import android.app.Application
import android.util.Log
import com.mujin.chadak.navi.GeoPoint
import com.mujin.chadak.navi.LocationFix
import com.mujin.chadak.navi.PleosFusedLocationFix
import com.mujin.chadak.navi.PleosWaypointNavigator
import com.mujin.chadak.navi.RepairShopJson
import com.mujin.chadak.signal.CompositeSignalSource
import com.mujin.chadak.signal.DebugMockVehicleSource
import com.mujin.chadak.signal.DrivingContext
import com.mujin.chadak.signal.FakeSignalSource
import com.mujin.chadak.signal.PleosVehicleSignalSource
import com.mujin.chadak.signal.VehicleSignal
import com.mujin.chadak.signal.VehicleSignalSource
import com.mujin.chadak.triage.TriageEngine
import com.mujin.chadak.ui.DrivingPolicy
import com.mujin.chadak.voice.DrivingVoiceFlow
import com.mujin.chadak.voice.ManualYesNoRecognizer
import com.mujin.chadak.voice.PleosOnDeviceAnnouncer

class ChadakApplication : Application() {
    lateinit var signals: VehicleSignalSource
        private set
    lateinit var fakeSignals: FakeSignalSource
        private set
    var debugMock: DebugMockVehicleSource? = null
        private set
    lateinit var session: DriveSession
        private set
    lateinit var yesNo: ManualYesNoRecognizer
        private set

    private lateinit var voiceFlow: DrivingVoiceFlow
    private var latestSignals: List<VehicleSignal> = emptyList()
    private var latestContext: DrivingContext = DrivingContext.driving()
    private var lastSignature: String? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Pleos entry points ${PleosSdkEntryPoints.touch()}")
        fakeSignals = FakeSignalSource()
        val children = mutableListOf<VehicleSignalSource>(
            PleosVehicleSignalSource(),
            fakeSignals,
        )
        if (BuildConfig.DEBUG) {
            val mock = DebugMockVehicleSource()
            debugMock = mock
            children += mock
        }
        signals = CompositeSignalSource(children)
        session = DriveSession()
        yesNo = ManualYesNoRecognizer()
        val shops = RepairShopJson.parse(
            assets.open("repair_shops.json").bufferedReader().use { it.readText() },
        )
        val location: LocationFix = if (BuildConfig.DEBUG) {
            LocationFix { GeoPoint(DEBUG_LATITUDE, DEBUG_LONGITUDE) }
        } else {
            PleosFusedLocationFix()
        }
        val navigator = PleosWaypointNavigator(shops, location)
        voiceFlow = DrivingVoiceFlow(
            announcer = PleosOnDeviceAnnouncer { text -> session.onSpoken(text) },
            recognizer = yesNo,
            onAccepted = { session.onWaypoint(navigator.addRepairShopWaypoint()) },
            onDeclined = { session.onDeclined() },
        )
    }

    fun onSignals(signals: List<VehicleSignal>) {
        latestSignals = signals
        publish()
    }

    fun onContext(context: DrivingContext) {
        latestContext = context
        publish()
    }

    private fun publish() {
        val triage = TriageEngine.classify(latestSignals)
        session.updateVehicle(latestSignals, latestContext, triage)
        val signature = triage?.signature
        val driving = !DrivingPolicy.showDetailUi(latestContext)
        if (driving && triage != null && signature != lastSignature) {
            lastSignature = signature
            voiceFlow.onLevel(triage.level)
        }
        if (triage == null) {
            lastSignature = null
        }
    }

    private companion object {
        const val TAG = "Chadak"
        const val DEBUG_LATITUDE = 37.4980
        const val DEBUG_LONGITUDE = 127.0280
    }
}
