package org.airella.btposition.model

import org.airella.btposition.activity.CanvasView

data class Device(val name: String?, val mac: String?, var position: CanvasView.Position? = null) {


    override fun hashCode(): Int {
        return mac?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Device

        if (mac != other.mac) return false

        return true
    }

    fun isConfigured() = position != null
}