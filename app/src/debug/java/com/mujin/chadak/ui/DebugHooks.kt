package com.mujin.chadak.ui

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import com.google.android.material.button.MaterialButton
import com.mujin.chadak.ChadakApplication
import com.mujin.chadak.R
import com.mujin.chadak.signal.FakeScenarios
import com.mujin.chadak.voice.YesNo

/**
 * Debug-only controls. The release source set keeps this object empty.
 * Tire pressure is injected here because the public Vehicle SDK cannot report it.
 */
object DebugHooks {
    fun attach(activity: ComponentActivity, app: ChadakApplication) {
        val slot = activity.findViewById<LinearLayout>(R.id.debug_slot) ?: return
        slot.visibility = android.view.View.VISIBLE
        slot.removeAllViews()
        fun button(label: String, click: () -> Unit) {
            val view = MaterialButton(activity).apply {
                text = label
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = 12 }
                setOnClickListener { click() }
            }
            slot.addView(view)
        }
        val mock = app.debugMock
        button(activity.getString(R.string.debug_tire)) { mock?.handle("TIRE_LOW") }
        button(activity.getString(R.string.debug_oil)) { mock?.handle("ENGINE_OIL_LOW") }
        button(activity.getString(R.string.debug_demo)) {
            app.fakeSignals.play(FakeScenarios.Scenario.STOP_WITH_OBSERVE)
        }
        button(activity.getString(R.string.debug_yes)) { app.yesNo.submit(YesNo.YES) }
        button(activity.getString(R.string.debug_no)) { app.yesNo.submit(YesNo.NO) }
        button(activity.getString(R.string.debug_park)) { mock?.handle("GEAR_PARK") }
        button(activity.getString(R.string.debug_drive)) { mock?.handle("GEAR_DRIVE") }
        button(activity.getString(R.string.debug_clear)) { mock?.handle("CLEAR") }
    }
}
