package org.airella.btposition.model

import android.net.wifi.ScanResult
import org.airella.btposition.view.CanvasView

data class Device(var name: String?, val mac: String?, var position: CanvasView.Position? = null, var scanResult: ScanResult? = null) {

    constructor(scanResult: ScanResult, position: CanvasView.Position?) : this(scanResult.SSID, scanResult.BSSID, position, scanResult)

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