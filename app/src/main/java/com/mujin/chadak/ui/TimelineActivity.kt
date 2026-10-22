package com.mujin.chadak.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.mujin.chadak.R
import com.mujin.chadak.databinding.ActivityTimelineBinding

/**
 * Placeholder for the parked consumable timeline. History arrives in a later version.
 */
class TimelineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val rows = listOf(
            R.string.timeline_oil,
            R.string.timeline_brake,
            R.string.timeline_washer,
            R.string.timeline_tire,
            R.string.timeline_odometer,
        )
        rows.forEach { resId ->
            val row = TextView(this).apply {
                text = getString(resId)
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
                setTextColor(getColor(R.color.chadak_text))
                setPadding(0, 20, 0, 20)
            }
            binding.timelineContainer.addView(row)
        }
    }
}
