package org.airella.btposition.activity

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import org.airella.btposition.bt.BluetoothScanService
import org.airella.btposition.model.Device
import org.airella.btposition.model.DistanceMeasurements
import org.airella.btposition.model.DistanceResult
import org.airella.btposition.model.RssiResultAdapter
import org.airella.btposition.utils.Log

class BtViewModel : ViewModel() {

    val devices: MutableMap<String, Device> = mutableMapOf()

    val measurements: DistanceMeasurements = DistanceMeasurements()

    val adapter: RssiResultAdapter = RssiResultAdapter(measurements)

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("Scan failed with code $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.i("Found device")
            addScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            Log.i("Found device batch")
            results.forEach { addScanResult(it) }
        }
    }

    private fun addScanResult(result: ScanResult) {
        val mac = result.device.address

        if (!devices.containsKey(mac)) {
            devices[mac] = Device(result.device.name, mac)
        } else {
            val itemChanged = measurements.addItem(DistanceResult(devices[mac]!!, result.rssi))
            adapter.notifyItemChanged(itemChanged)
        }
    }

    fun startBtScan(context: Context) {
        try {
            BluetoothScanService.scanBTDevices(scanCallback, true)
            Log.i("Started scanning")
        } catch (e: Exception) {
            Log.w("BT disabled")
            Toast.makeText(context, "BT disabled", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopBtScan(context: Context) {
        try {
            BluetoothScanService.scanBTDevices(scanCallback, false)
            Log.i("Stopped scanning")
        } catch (e: Exception) {
            Log.w("BT disabled")
            Toast.makeText(context, "BT disabled", Toast.LENGTH_SHORT).show()
        }
    }
}