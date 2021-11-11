package org.airella.btposition.acitivity

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.airella.btposition.bt.BluetoothScanService
import org.airella.btposition.utils.Log
import java.util.*

class MainActivityViewModel : ViewModel() {

    val scanResult: MutableLiveData<ScanResult> = MutableLiveData()

    val timeToUpdate: MutableLiveData<Int> = MutableLiveData(0)

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("Scan failed with code $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            scanResult.value = result
            Log.i("Found device")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            scanResult.value = results[0]
            Log.i("Found device batch")
        }
    }

    fun restartScan(context: Context) {
        if (BluetoothScanService.isScanning.value == true) {
            stopBtScan(context)
        }
        startBtScan(context)
    }

    fun startBtScan(context: Context) {
        try {
            BluetoothScanService.scanBTDevices(scanCallback, true)
        } catch (e: Exception) {
            Log.w("BT disabled")
            Toast.makeText(context, "BT disabled", Toast.LENGTH_SHORT).show()
        }
        Log.i("Started scanning")
    }

    fun stopBtScan(context: Context) {
        try {
            BluetoothScanService.scanBTDevices(scanCallback, false)
        } catch (e: Exception) {
            Log.w("BT disabled")
            Toast.makeText(context, "BT disabled", Toast.LENGTH_SHORT).show()
        }
    }

    fun startScanTimer(activity: Activity) {
        Timer().schedule(
                object : TimerTask() {
                    override fun run() {
                        activity.runOnUiThread {
                            val timeToUpdate = timeToUpdate
                            timeToUpdate.value = timeToUpdate.value!! - 1
                            if (timeToUpdate.value!! < 0) {
                                timeToUpdate.value = 6
                                restartScan(activity)
                            }
                        }
                    }
                },
                0L,
                1000L
        )
    }
}