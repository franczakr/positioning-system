package org.airella.btposition.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult

data class RssiResult(val device: Device, val rssi: Int)