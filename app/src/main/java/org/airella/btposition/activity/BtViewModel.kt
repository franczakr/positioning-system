package org.airella.btposition.activity

import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import org.airella.btposition.model.Device
import org.airella.btposition.model.DistanceMeasurements
import org.airella.btposition.utils.DistanceCalculator
import java.util.*

class BtViewModel : ViewModel() {

    val devices: MutableMap<String, Device> = mutableMapOf()

    val measurements: DistanceMeasurements = DistanceMeasurements()
    var isConfigured = false

    fun addScanResult(result: ScanResult) {
        val device = result.device
        val mac = device.address

        if (!devices.containsKey(mac)) {
            devices[mac] = Device(result.device.name, mac)
        } else if(devices[mac]!!.name == null && device.name != null) {
            devices[mac]!!.name = device.name
        }
        if (devices[mac]?.isConfigured() == true) {
            measurements.addItem(devices[mac]!!, DistanceCalculator.calculateDistance(result.rssi))
        }
    }

}