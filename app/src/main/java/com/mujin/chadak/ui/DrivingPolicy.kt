package com.mujin.chadak.ui

import com.mujin.chadak.signal.DrivingContext
import com.mujin.chadak.signal.Gear

/**
 * Detail UI opens only in Park, and only when CarUxRestrictions is not asking
 * for distraction optimization. While driving, the foreground service speaks.
 */
object DrivingPolicy {
    fun showDetailUi(context: DrivingContext): Boolean {
        return context.gear == Gear.PARK && !context.requiresDistractionOptimization
    }
}
