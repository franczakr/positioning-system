package org.airella.btposition.model

class DistanceMeasurements {

    val measurements: MutableList<DistanceResult> = mutableListOf()

    fun addItem(rssiResult: DistanceResult): Int {
        val index: Int = measurements.map { it.device }.indexOf(rssiResult.device)
        if (index != -1) {
            measurements[index] = rssiResult
            return index
        } else {
            measurements.add(0, rssiResult)
            return 0
        }
    }
}