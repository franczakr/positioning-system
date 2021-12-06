package org.airella.btposition.model

data class Device(val name: String?, val mac: String?) {


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
}