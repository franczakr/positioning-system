package org.airella.btposition.model

class DistanceMeasurements {

    val measurements: MutableMap<Device, DistanceResult> = mutableMapOf()

    fun values(): List<DistanceResult> = measurements.values.toList()

    fun addItem(device: Device, distance: Float) {
        if (measurements.containsKey(device)) {
            measurements[device]!!.addDistanceMeasurement(distance)
        } else {
            measurements[device] = DistanceResult(device)
        }
    }
}