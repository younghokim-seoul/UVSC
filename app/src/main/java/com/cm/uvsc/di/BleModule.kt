package com.cm.uvsc.di

import android.content.Context
import com.cm.uvsc.ble.BleClient
import com.polidea.rxandroidble3.RxBleClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    @Provides
    @Singleton
    fun provideRxBle(@ApplicationContext context: Context): RxBleClient =
        RxBleClient.create(context)


    @Provides
    @Singleton
    fun provideBleClient(rxBleClient: RxBleClient) = BleClient(rxBleClient)
}