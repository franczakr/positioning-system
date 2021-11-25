package org.airella.btposition.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.airella.btposition.bt.BluetoothScanService
import org.airella.btposition.bt.BluetoothService
import org.airella.btposition.bt.ReadRssiBluetoothCallback
import org.airella.btposition.model.RssiResult
import org.airella.btposition.model.RssiResultAdapter
import org.airella.btposition.model.Device
import org.airella.btposition.utils.Log
import java.util.*

class WifiViewModel : ViewModel() {

    val counter: MutableLiveData<Int> = MutableLiveData(0)

    private val results = arrayListOf<RssiResult>()

    val adapter: RssiResultAdapter = RssiResultAdapter(results)

    val sensors: MutableSet<BluetoothDevice> = mutableSetOf()


    private fun addScanResult(result: ScanResult) {
        results.add(0, RssiResult(Device(result.device.name, result.device.address), result.rssi))
        adapter.notifyItemInserted(0)
        sensors.add(result.device)
    }

    private var timer: Timer = Timer()

    fun startScanTimer(activity: Activity) {
        timer = Timer()
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
        when (counter.value) {
            0 -> {
                startBtScan(activity)
            }
            1,2,3 -> {}
            4 -> {
                stopBtScan(activity)
                readSensorsRSSI(activity)
            }
            else -> {
                readSensorsRSSI(activity)
            }
        }
        counter.value = counter.value!! + 1
        if(counter.value == 10) {
            counter.value = 0
            results.clear()
            adapter.notifyDataSetChanged()
        }
    }
}