@file:OptIn(ExperimentalCoroutinesApi::class)

package com.cm.uvsc.ble

import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.scan.ScanFilter
import com.polidea.rxandroidble3.scan.ScanSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BleClient @Inject constructor(
    private val rxBleClient: RxBleClient
) {


    companion object {
        private const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        const val CHARACTERISTIC_UUID = "BEB5483E-36E1-4688-B7F5-EA07361B26A8"
        const val DEVICE_NAME = "UV"

    }

    private val scanFilter: ScanFilter = ScanFilter.Builder().build()

    private val settings: ScanSettings =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()


    fun startScan(): Flow<RxBleDevice> {
        return rxBleClient.scanBleDevices(settings, scanFilter)
            .map { it.bleDevice }
            .filter {
                it.name?.contains(DEVICE_NAME, ignoreCase = true) == true
            }
            .distinctUntilChanged { old, new -> old.macAddress == new.macAddress }
            .asFlow()
    }


    fun connectionStateFlow(device: RxBleDevice) = device.observeConnectionStateChanges().asFlow()

    fun connect(device: RxBleDevice) = device.establishConnection(false).asFlow()

    suspend fun send(connection: RxBleConnection, sendByteData: ByteArray) {
        connection.writeCharacteristic(
            UUID.fromString(CHARACTERISTIC_UUID),
            sendByteData
        ).await()
    }

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