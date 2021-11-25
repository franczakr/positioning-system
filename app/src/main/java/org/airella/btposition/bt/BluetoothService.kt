package org.airella.btposition.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import org.airella.btposition.MyApplication
import org.airella.btposition.utils.Log
import java.util.*

object BluetoothService {

    private val bluetoothAdapter: BluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }

    private var isConnected: Boolean = false

    private val tasksQueue: Queue<Pair<BluetoothDevice, ReadRssiBluetoothCallback>> = LinkedList()

    fun connectGatt(btDevice: BluetoothDevice, callback: ReadRssiBluetoothCallback) {
        Log.d("connectGatt")
        synchronized(isConnected) {
            if (!isConnected) {
                isConnected = true
                MyApplication.runOnUiThread {
                    btDevice.connectGatt(MyApplication.appContext, false, callback)
                }
            } else {
                tasksQueue.add(Pair(btDevice, callback))
            }
        }
    }

    fun connectFinished() {
        synchronized(isConnected) {
            if (tasksQueue.isNotEmpty()) {
                val request = tasksQueue.remove()
                MyApplication.runOnUiThread {
                    request.first.connectGatt(MyApplication.appContext, false, request.second)
                }
            } else {
                isConnected = false
            }
        }
    }

    fun getDeviceByMac(macAddress: String): BluetoothDevice {
        try {
            return bluetoothAdapter.getRemoteDevice(macAddress.toUpperCase(Locale.getDefault()))
        } catch (e: NullPointerException) {
            throw RuntimeException()
        }
    }

    fun isBtBusy(): Boolean {
        synchronized(isConnected) {
            return isConnected || tasksQueue.isNotEmpty()
        }
    }
}






































