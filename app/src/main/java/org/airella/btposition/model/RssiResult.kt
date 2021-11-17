package org.airella.btposition.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult

data class RssiResult(val device: BluetoothDevice, val rssi: Int)