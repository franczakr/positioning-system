package org.airella.btposition.activity

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.airella.btposition.model.Device
import org.airella.btposition.model.RssiResult
import org.airella.btposition.model.RssiResultAdapter
import java.util.*

class WifiViewModel : ViewModel() {

    val counter: MutableLiveData<Int> = MutableLiveData(0)

    val adapter: RssiResultAdapter = RssiResultAdapter()

    val sensors: MutableSet<BluetoothDevice> = mutableSetOf()


    private fun addScanResult(result: ScanResult) {
        adapter.addItem(RssiResult(Device(result.device.name, result.device.address), result.rssi))
    }

    private var timer: Timer = Timer()

    fun startScanTimer(activity: Activity) {
        timer = Timer()
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
//                        eachSecondTimer(activity)
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

//    private fun eachSecondTimer(activity: Activity) {
//        when (counter.value) {
//            0 -> {
//                startBtScan(activity)
//            }
//            1,2,3 -> {}
//            4 -> {
//                stopBtScan(activity)
//                readSensorsRSSI(activity)
//            }
//            else -> {
//                readSensorsRSSI(activity)
//            }
//        }
//        counter.value = counter.value!! + 1
//        if(counter.value == 10) {
//            counter.value = 0
//            results.clear()
//            adapter.notifyDataSetChanged()
//        }
//    }
}