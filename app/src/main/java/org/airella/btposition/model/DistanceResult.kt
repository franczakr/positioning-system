package org.airella.btposition.model

import java.util.*

data class DistanceResult(val device: Device) {

    private val lastMeasuresCountForAverage = 20

    private val distanceMeasurements: LinkedList<Float> = LinkedList()

    fun distance(): Float = distanceMeasurements.average().toFloat()

    fun addDistanceMeasurement(distanceMeasurement: Float) {
        distanceMeasurements.addLast(distanceMeasurement)

        if(distanceMeasurements.size > lastMeasuresCountForAverage) {
            distanceMeasurements.removeFirst()
        }
    }
}
