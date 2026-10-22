package com.mujin.chadak.ui

import androidx.activity.ComponentActivity
import com.mujin.chadak.ChadakApplication

object DebugHooks {
    @Suppress("UNUSED_PARAMETER")
    fun attach(activity: ComponentActivity, app: ChadakApplication) = Unit
}
