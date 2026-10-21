package com.mujin.chadak.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.mujin.chadak.ChadakApplication
import com.mujin.chadak.R
import com.mujin.chadak.databinding.ActivityParkedDetailBinding

class ParkedDetailActivity : ComponentActivity() {
    private lateinit var binding: ActivityParkedDetailBinding
    private var unsubscribe: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParkedDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.openTimeline.setOnClickListener {
            startActivity(Intent(this, TimelineActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val session = (application as ChadakApplication).session
        unsubscribe = session.observe { render() }
        render()
    }

    override fun onStop() {
        unsubscribe?.invoke()
        unsubscribe = null
        super.onStop()
    }

    private fun render() {
        val session = (application as ChadakApplication).session
        if (!DrivingPolicy.showDetailUi(session.context)) {
            finish()
            return
        }
        val triage = session.triage
        binding.detailLevel.text = triage?.level?.label ?: getString(R.string.detail_clear)
        binding.detailReason.text = triage?.reason ?: getString(R.string.detail_clear_reason)
        binding.waypointStatus.text = session.waypoint
        binding.signalContainer.removeAllViews()
        val signals = triage?.signals.orEmpty()
        if (signals.isEmpty()) {
            binding.signalContainer.addView(signalLine(getString(R.string.detail_no_signals)))
        } else {
            signals.forEach { signal ->
                binding.signalContainer.addView(signalLine(signal.summary))
            }
        }
    }

    private fun signalLine(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
            setTextColor(getColor(R.color.chadak_text))
            setPadding(0, 12, 0, 12)
        }
    }
}
