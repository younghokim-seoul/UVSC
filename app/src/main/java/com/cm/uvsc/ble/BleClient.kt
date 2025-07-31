@file:OptIn(ExperimentalCoroutinesApi::class)

package com.cm.uvsc.ble

import android.os.ParcelUuid

import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.scan.ScanFilter
import com.polidea.rxandroidble3.scan.ScanSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.rx3.asFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleClient @Inject constructor(
    private val rxBleClient: RxBleClient
) {


    companion object {
        private const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914"
        const val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

    }

    val scanFilter: ScanFilter =
        ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_UUID))).build()

    val settings: ScanSettings = ScanSettings.Builder().build()


    fun startScan() =
        rxBleClient.scanBleDevices(settings, scanFilter).map { it.bleDevice }
            .distinct { it.macAddress }.asFlow()


    fun connectionStateFlow(device: RxBleDevice) = device.observeConnectionStateChanges().asFlow()

    fun connect(device: RxBleDevice) = device.establishConnection(false).asFlow()

    fun getNotificationFlow(
        connection: RxBleConnection,
        characteristicUuid: UUID
    ): Flow<ByteArray> {
        return connection.setupNotification(characteristicUuid)
            .asFlow()
            .flatMapConcat { notificationObservable ->
                notificationObservable.asFlow()
            }
    }

}