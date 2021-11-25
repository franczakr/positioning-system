package org.airella.btposition.bt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import org.airella.btposition.Config
import org.airella.btposition.MyApplication.Companion.runOnUiThread
import org.airella.btposition.utils.Log
import java.util.*

open class ReadRssiBluetoothCallback : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Log.d("onConnectionStateChange")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                runOnUiThread {
                    gatt.readRemoteRssi()
                    onConnected()
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                gatt.close()
            }
        } else {
            runOnUiThread {
                onFailToConnect()
                onFinish()
            }
            gatt.close()
        }
    }


    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        Log.d("onReadRemoteRssi")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            runOnUiThread {
                onSuccess(rssi)
                onFinish()
            }
        } else {
            runOnUiThread {
                onFailToConnect()
                onFinish()
            }
        }
        gatt.close()
    }


    protected open fun onConnected() {
        Log.d("Connected")
    }

    protected open fun onFailToConnect() {
        Log.d("Failed to connect")
        onFailure()
    }

    protected open fun onFailure() {
        Log.d("Failed")
    }

    protected open fun onSuccess(rssi: Int) {
        Log.d("Success")
    }

    private fun onFinish() {
        BluetoothService.connectFinished()
    }

}