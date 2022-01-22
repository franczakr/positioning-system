package org.airella.btposition.activity

import android.net.wifi.ScanResult
import android.net.wifi.rtt.RangingResult
import androidx.lifecycle.ViewModel
import org.airella.btposition.model.Device
import org.airella.btposition.model.DistanceMeasurements

class WifiViewModel : ViewModel() {

    var isConfigured: Boolean = false

    val devices: MutableMap<String, ScanResult> = mutableMapOf()

    val configuredDevices: MutableMap<String, Device> = mutableMapOf()

    val measurements: DistanceMeasurements = DistanceMeasurements()

    fun addScanResult(result: RangingResult) {
        val mac = result.macAddress.toString()
        val device = configuredDevices[mac]!!
        val distance = result.distanceMm / 1000.0f

        measurements.addItem(device, distance)
    }
}