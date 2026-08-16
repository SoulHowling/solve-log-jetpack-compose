package com.soulhowling.solvelog_jetpackcompose.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.MainActivity

object NotificationUtils {

    const val STOPWATCH_CHANNEL_ID = "stopwatch_channel"
    const val TIMER_CHANNEL_ID = "timer_channel"

    /**
     * Creates a notification channel.
     * Required for Android 8.0 (Oreo) and above.
     */
    fun createNotificationChannel(
        context: Context,
        channelId: String,
        name: String,
        description: String? = null,
        importance: Int = NotificationManager.IMPORTANCE_LOW
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, name, importance).apply {
            this.description = description
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * Provides a standard notification builder with common configurations.
     */
    fun getBaseNotificationBuilder(
        context: Context,
        channelId: String,
        title: String,
        content: String,
        smallIcon: Int = R.drawable.solve_log_notification
    ): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    /**
     * Show simple notification
     */
    fun showNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        content: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = getBaseNotificationBuilder(context, channelId, title, content).build()
        manager.notify(notificationId, notification)
    }
}
