package com.cm.geofence.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(
    private val context: Context,
    private val channelId: String,
    private val channelName: String,
    private val channelImportance: Int = NotificationManager.IMPORTANCE_DEFAULT
) {
    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(channelId, channelName, channelImportance).apply {
            description = channelName
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    fun show(
        notificationId: Int,
        title: String,
        message: String,
        @DrawableRes smallIcon: Int,
        clickPendingIntent: PendingIntent? = null
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .also {
                clickPendingIntent?.let { pendingIntent ->
                    it.setContentIntent(pendingIntent)
                }
            }

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}