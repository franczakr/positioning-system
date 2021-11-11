package org.airella.btposition.utils

object Log {
    private const val loggerTag = "bt-position"

    fun d(text: String) = android.util.Log.d(loggerTag, text)
    fun i(text: String) = android.util.Log.i(loggerTag, text)
    fun w(text: String) = android.util.Log.w(loggerTag, text)
    fun e(text: String) = android.util.Log.e(loggerTag, text)
}