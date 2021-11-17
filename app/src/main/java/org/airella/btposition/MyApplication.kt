package org.airella.btposition

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        fun runOnUiThread(runnable: Runnable) = mainThreadHandler.post(runnable)

        private lateinit var context: Context

        val appContext: Context
            get() = context

        private val toast by lazy { Toast.makeText(appContext, "", Toast.LENGTH_SHORT) }

        fun createToast(text: String) {
            runOnUiThread {
                toast.setText(text)
                toast.show()
            }
        }
    }
}