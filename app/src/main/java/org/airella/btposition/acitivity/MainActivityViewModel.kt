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
import org.airella.btposition.bt.BluetoothService
import org.airella.btposition.bt.ReadRssiBluetoothCallback
import org.airella.btposition.model.RssiResult
import org.airella.btposition.model.BtResultAdapter
import org.airella.btposition.utils.Log
import java.util.*

class MainActivityViewModel : ViewModel() {

    val timeToUpdate: MutableLiveData<Int> = MutableLiveData(0)

    private val results = arrayListOf<RssiResult>()

    val adapter: BtResultAdapter = BtResultAdapter(results)

    val sensors: ArrayList<BluetoothDevice> = arrayListOf()

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
        results.add(RssiResult(result.device, result.rssi))
        adapter.notifyItemInserted(results.size - 1)
        sensors.add(result.device)
    }

    private fun startBtScan(context: Context) {
        try {
            BluetoothScanService.scanBTDevices(scanCallback, true)
            Log.i("Started scanning")
        } catch (e: Exception) {
            Log.w("BT disabled")
            Toast.makeText(context, "BT disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopBtScan(context: Context) {
        try {
            BluetoothScanService.scanBTDevices(scanCallback, false)
            Log.i("Stopped scanning")
        } catch (e: Exception) {
            Log.w("BT disabled")
            Toast.makeText(context, "BT disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readSensorsRSSI(context: Context) {
        sensors.forEach { sensor ->
            BluetoothService.connectGatt(sensor, object : ReadRssiBluetoothCallback() {
                override fun onSuccess(rssi: Int) {
                    super.onSuccess(rssi)
                    results.add(RssiResult(sensor, rssi))
                }
            })
        }
    }

    private val timer = Timer()

    fun startScanTimer(activity: Activity) {
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        eachSecondTimer(activity)
                    }
                }
            },
            0L,
            1000L
        )
    }

    fun stopScanTimer() {
        timer.cancel()
    }

    private fun eachSecondTimer(activity: Activity) {
        when (timeToUpdate.value) {
            0 -> {
                startBtScan(activity)
            }
            1,2,3 -> {}
            4 -> {
                stopBtScan(activity)
            }
            else -> {
//                readSensorsRSSI(activity)
            }
        }
        timeToUpdate.value = timeToUpdate.value!! + 1
        if(timeToUpdate.value == 6) {
            timeToUpdate.value = 0
        }
    }
}