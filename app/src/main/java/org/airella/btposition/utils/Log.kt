package org.airella.btposition.utils

object Log {
    private const val loggerTag = "bt-position"

    fun d(text: String) = android.util.Log.d(loggerTag, prepareLogMsg(text))
    fun i(text: String) = android.util.Log.i(loggerTag, prepareLogMsg(text))
    fun w(text: String) = android.util.Log.w(loggerTag, prepareLogMsg(text))
    fun e(text: String) = android.util.Log.e(loggerTag, prepareLogMsg(text))


    private fun prepareLogMsg(text: String): String {
        val caller = Thread.currentThread().stackTrace[4]
        return "${caller.className.substringAfterLast('.')}#${caller.methodName}: $text"
    }
}
