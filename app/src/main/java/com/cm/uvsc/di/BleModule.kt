package com.cm.uvsc.di

import android.content.Context
import com.polidea.rxandroidble3.RxBleClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier


@Module
@InstallIn(ActivityRetainedComponent::class)
object BleModule {

    @Provides
    @ActivityRetainedScoped
    fun provideRxBle(@ApplicationContext context: Context): RxBleClient = RxBleClient.create(context)

    @Provides
    @ActivityRetainedScoped
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope