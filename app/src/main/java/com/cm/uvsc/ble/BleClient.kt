package com.cm.uvsc.ble

import android.os.ParcelUuid
import android.util.Log
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.scan.ScanFilter
import com.polidea.rxandroidble3.scan.ScanSettings
import io.reactivex.rxjava3.disposables.Disposable
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleClient @Inject constructor(
    private val rxBleClient: RxBleClient
) {

    companion object {
        private const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914"
        private const val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
        private const val TAG = "BleClient"
    }

    private var scanSubscription: Disposable? = null
    private var connectionStateDisposable: Disposable? = null


    val scanFilter: ScanFilter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_UUID)))
        .build()

    val settings: ScanSettings = ScanSettings.Builder()
        .build()


    fun startScan() {

        if (scanSubscription?.isDisposed == false) {
            Log.d(TAG, "Scan is already running.")
            return
        }

        scanSubscription = rxBleClient.scanBleDevices(settings, scanFilter).subscribe(
            { scanResult ->
                Log.d(TAG, "Scan Success: ${scanResult.bleDevice.macAddress}")
            }, { throwable ->
                Log.e(TAG, "Scan Failure: ", throwable)
            })
    }

    fun stopScan() {
        scanSubscription?.dispose()
        scanSubscription = null
    }

    fun connectDevice(device: RxBleDevice) {
        // register connectionStateListener
        connectionStateDisposable?.dispose()
        connectionStateDisposable = device.observeConnectionStateChanges()
            .subscribe(
                { connectionState ->
//                    connectionStateListener(device, connectionState)
                }
            ) { throwable ->
                throwable.printStackTrace()
            }

        // connect

    }

}