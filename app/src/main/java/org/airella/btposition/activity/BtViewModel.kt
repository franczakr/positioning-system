package org.airella.btposition.activity

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.airella.btposition.bt.BluetoothScanService
import org.airella.btposition.model.Device
import org.airella.btposition.model.RssiResult
import org.airella.btposition.model.RssiResultAdapter
import org.airella.btposition.utils.Log

class BtViewModel : ViewModel() {

    val counter: MutableLiveData<Int> = MutableLiveData(0)

    val adapter: RssiResultAdapter = RssiResultAdapter()

//    val sensors: MutableSet<BluetoothDevice> = mutableSetOf()

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
        adapter.addItem(RssiResult(Device(result.device.name, result.device.address), result.rssi))
//        sensors.add(result.device)
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

//    private fun readSensorsRSSI(context: Context) {
//        sensors.forEach { sensor ->
//            BluetoothService.connectGatt(sensor, object : ReadRssiBluetoothCallback() {
//                override fun onSuccess(rssi: Int) {
//                    super.onSuccess(rssi)
//                    adapter.addItem(RssiResult(Device(sensor.name, sensor.address), rssi))
//                    Log.e(rssi.toString())
//                }
//            })
//        }
//    }

//    private var timer: Timer = Timer()
//
//    fun startScanTimer(activity: Activity) {
//        timer = Timer()
//        timer.schedule(
//            object : TimerTask() {
//                override fun run() {
//                    activity.runOnUiThread {
//                        eachSecondTimer(activity)
//                    }
//                }
//            },
//            0L,
//            1000L
//        )
//    }

//    fun stopScanTimer() {
//        timer.cancel()
//    }

//    private fun eachSecondTimer(activity: Activity) {
//        when (counter.value) {
//            0 -> {
//                startBtScan(activity)
//            }
////            1, 2, 3 -> {
////            }
////            1 -> {
////                stopBtScan(activity)
//////                readSensorsRSSI(activity)
////            }
////            else -> {
//////                readSensorsRSSI(activity)
////            }
//        }
//        counter.value = counter.value!! + 1
//        if (counter.value == 10) {
//            counter.value = 0
//            adapter.notifyDataSetChanged()
//        }
//    }
}