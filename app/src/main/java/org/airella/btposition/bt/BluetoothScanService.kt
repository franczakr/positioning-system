package org.airella.btposition.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.airella.btposition.Config
import org.airella.btposition.utils.Log

object BluetoothScanService {

    private val bluetoothAdapter: BluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }

    private val scanning: MutableLiveData<Boolean> = MutableLiveData(false)

    val isScanning: LiveData<Boolean> = scanning

    private val filters: List<ScanFilter> = listOf(
        ScanFilter.Builder().setServiceUuid(ParcelUuid(Config.SERVICE_UUID)).build()
    )

    private val settings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setReportDelay(200L)
        .build()

    fun scanBTDevices(callback: ScanCallback, enable: Boolean) {
        try {
            scanning.value = enable
            if (enable) {
                Log.d("Start BT scan")
                bluetoothAdapter.bluetoothLeScanner.startScan(filters, settings, callback)
            } else {
                Log.d("Stop BT scan")
                bluetoothAdapter.bluetoothLeScanner.stopScan(callback)
            }
        } catch (e: NullPointerException) {
            Log.e("Bluetooth disabled")
            throw RuntimeException()
        }
    }
}