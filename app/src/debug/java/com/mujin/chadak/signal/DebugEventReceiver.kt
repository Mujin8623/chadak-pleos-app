package com.mujin.chadak.signal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mujin.chadak.ChadakApplication

/**
 * `adb shell am broadcast -a ai.chadak.DEBUG_EVENT --es type TIRE_LOW`
 * The action is fixed as `ai.chadak.DEBUG_EVENT` even though the application id
 * is `com.mujin.chadak`. This receiver is merged only into debug builds.
 */
class DebugEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) {
            return
        }
        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        val app = context.applicationContext as? ChadakApplication ?: return
        app.debugMock?.handle(type)
    }

    companion object {
        const val ACTION = "ai.chadak.DEBUG_EVENT"
        const val EXTRA_TYPE = "type"
    }
}
