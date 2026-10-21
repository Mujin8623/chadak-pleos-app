package com.mujin.chadak.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.mujin.chadak.ChadakApplication
import com.mujin.chadak.R
import com.mujin.chadak.databinding.ActivityDrivingBinding

class DrivingActivity : ComponentActivity() {
    private lateinit var binding: ActivityDrivingBinding
    private var unsubscribe: (() -> Unit)? = null
    private var routedToDetail = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrivingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ContextCompat.startForegroundService(this, Intent(this, DrivingAlertService::class.java))
        DebugHooks.attach(this, application as ChadakApplication)
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
        binding.announcement.text = session.announcement.ifBlank {
            getString(R.string.driving_waiting)
        }
        binding.voicePhase.text = session.phase.ifBlank { getString(R.string.driving_voice_idle) }
        binding.waypointStatus.text = session.waypoint
        if (DrivingPolicy.showDetailUi(session.context)) {
            if (!routedToDetail) {
                routedToDetail = true
                startActivity(Intent(this, ParkedDetailActivity::class.java))
            }
        } else {
            routedToDetail = false
        }
    }
}
