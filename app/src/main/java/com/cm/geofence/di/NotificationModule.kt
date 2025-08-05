package com.cm.geofence.di

import android.app.NotificationManager
import android.content.Context
import com.cm.geofence.core.notification.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    private const val URGENT_CHANNEL_ID = "urgent_notice_channel"

    @Singleton
    @Provides
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper {
        return NotificationHelper(
            context = context,
            channelId = URGENT_CHANNEL_ID,
            channelName = "긴급 알림",
            channelImportance = NotificationManager.IMPORTANCE_DEFAULT // 기본 중요도
        )
    }
}