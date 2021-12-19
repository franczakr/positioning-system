package org.airella.btposition.model

import org.airella.btposition.utils.DistanceCalculator

data class DistanceResult(val device: Device, val distance: Float, val rssi: Int? = null) {

    constructor(device: Device, rssi: Int) : this(
        device,
        DistanceCalculator.calculateDistance(rssi),
        rssi
    )

    fun position() = device.position
}
