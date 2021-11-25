package org.airella.btposition.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.airella.btposition.databinding.ActivityWifiBinding

class WifiActivity : AppCompatActivity() {

    private lateinit var viewModel: BtViewModel

    private val wifiManager: WifiManager by lazy { getSystemService(WIFI_SERVICE) as WifiManager }

    private val wifiRttManager: WifiRttManager by lazy { getSystemService(WIFI_RTT_RANGING_SERVICE) as WifiRttManager }

    private val viewBinding: ActivityWifiBinding by lazy {
        ActivityWifiBinding.inflate(
            layoutInflater
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val hasRtt = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)

        if (hasRtt) {
            viewBinding.status.text = "RTT is AVAILABLE"
            viewBinding.status.setTextColor(Color.GREEN)
        } else {
            viewBinding.status.text = "RTT is NOT available"
            viewBinding.status.setTextColor(Color.RED)
        }

        initRtt()
    }

    private fun initRtt() {
        val filter = IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED)

        val myReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (wifiRttManager.isAvailable) {
                    startRtt()
                }
            }
        }
        registerReceiver(myReceiver, filter)
    }

    private fun startRtt() {
        val scanResults = wifiManager.scanResults

        val request: RangingRequest = RangingRequest.Builder().run {
            addAccessPoints(scanResults)
            build()
        }

        requestRtt(request)
    }

    private fun requestRtt(request: RangingRequest) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            wifiRttManager.startRanging(request, mainExecutor, object : RangingResultCallback() {

                override fun onRangingResults(results: List<RangingResult>) {
                    TODO()
                }

                override fun onRangingFailure(code: Int) {
                    TODO()
                }
            })
        } else {
            TODO()
        }
    }


}