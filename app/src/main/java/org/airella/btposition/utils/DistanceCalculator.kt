package org.airella.btposition.utils

import kotlin.math.pow

object DistanceCalculator {

    /**
     * Calculation based on
     * https://iotandelectronics.wordpress.com/2016/10/07/how-to-calculate-distance-from-the-rssi-value-of-the-ble-beacon/
     */

    private const val txPower = -65  // one meter power
    private const val N = 3.5 // Constant depends on the Environmental factor

    fun calculateDistance(rssi: Int): Float {
        return 10.0.pow((txPower - rssi) / (10.0 * N)).toFloat()
    }
}