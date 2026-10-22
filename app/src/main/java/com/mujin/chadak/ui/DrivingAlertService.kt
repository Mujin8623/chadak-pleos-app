package com.mujin.chadak.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.mujin.chadak.ChadakApplication
import com.mujin.chadak.R
import com.mujin.chadak.signal.VehicleSignalSource

/**
 * Voice-only path while the car is moving. The activity can be blocked by
 * distraction rules, so announcements are owned by this foreground service.
 */
class DrivingAlertService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var unsubscribe: (() -> Unit)? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val starting = notification(getString(R.string.notification_waiting))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                starting,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, starting)
        }
        val app = application as ChadakApplication
        app.signals.start(
            object : VehicleSignalSource.Listener {
                override fun onSignals(signals: List<com.mujin.chadak.signal.VehicleSignal>) {
                    handler.post { app.onSignals(signals) }
                }

                override fun onDrivingContext(context: com.mujin.chadak.signal.DrivingContext) {
                    handler.post { app.onContext(context) }
                }
            },
        )
        unsubscribe = app.session.observe {
            val text = app.session.announcement.ifBlank { getString(R.string.notification_waiting) }
            getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(text))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        unsubscribe?.invoke()
        (application as ChadakApplication).signals.stop()
        super.onDestroy()
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        manager.createNotificationChannel(channel)
    }

    private fun notification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private companion object {
        const val CHANNEL_ID = "chadak_driving"
        const val NOTIFICATION_ID = 1001
    }
}
