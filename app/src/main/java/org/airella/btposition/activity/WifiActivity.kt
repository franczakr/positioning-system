package org.airella.btposition.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import org.airella.btposition.databinding.ActivityWifiBinding
import org.airella.btposition.databinding.DeviceConfigItemBinding
import org.airella.btposition.model.Device
import org.airella.btposition.view.CanvasView
import java.util.*

class WifiActivity : AppCompatActivity() {

    private val wifiManager: WifiManager by lazy { getSystemService(WIFI_SERVICE) as WifiManager }

    private var wifiRttManager: WifiRttManager? = null

    private val viewModel: WifiViewModel by lazy { ViewModelProvider(this).get(WifiViewModel::class.java) }

    private val viewBinding: ActivityWifiBinding by lazy {
        ActivityWifiBinding.inflate(
            layoutInflater
        )
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private val rttRunner = object : Runnable {
        override fun run() {
            if(wifiRttManager == null) {
                wifiRttManager =  getSystemService(WIFI_RTT_RANGING_SERVICE) as WifiRttManager?
            }
            if(wifiRttManager != null) {
                if (wifiRttManager!!.isAvailable) {
                    if (!viewModel.isConfigured) {
                        findRttDevices()
                    } else {
                        updateRttDistance()
                    }
                }
            }
            mainHandler.postDelayed(this, 1000)
        }
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

        mainHandler.post(rttRunner)
    }
    private fun findRttDevices() {
        val scanResults = wifiManager.scanResults

        val request: RangingRequest = RangingRequest.Builder().run {
            addAccessPoints(scanResults)
            build()
        }

        requestFindRttDevices(request)
    }

    private fun requestFindRttDevices(request: RangingRequest) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            wifiRttManager!!.startRanging(request, mainExecutor, object : RangingResultCallback() {

                override fun onRangingResults(results: List<RangingResult>) {
                    results.forEach {
                        if (it.status == RangingResult.STATUS_SUCCESS) {
                            wifiManager.scanResults
                                .find { result -> result.BSSID == it.macAddress.toString() }
                                ?.let { device ->
                                    viewModel.devices.putIfAbsent(
                                        device.BSSID,
                                        device
                                    )
                                }
                        }
                    }
                }

                override fun onRangingFailure(code: Int) {
                    Toast.makeText(this@WifiActivity, "RTT ranging failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        } else {
            Toast.makeText(this, "Location permission missing", Toast.LENGTH_SHORT).show()
        }
    }

    fun configureSensors(view: View) {
        val devices = viewModel.devices.values.toMutableList()

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        layout.setPadding(10)

        val devicesViews: MutableMap<ScanResult, DeviceConfigItemBinding> = mutableMapOf()

        for (device in devices) {
            val deviceItemBinding = DeviceConfigItemBinding.inflate(LayoutInflater.from(this))

            val configuredDevice = viewModel.configuredDevices[device.BSSID]

            devicesViews[device] = deviceItemBinding
            deviceItemBinding.deviceDescription.text = getDeviceDesc(device)
            deviceItemBinding.deviceX.setText(configuredDevice?.position?.x?.toString() ?: "")
            deviceItemBinding.deviceY.setText(configuredDevice?.position?.y?.toString() ?: "")
            layout.addView(deviceItemBinding.root)
        }

        AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                for (entry in devicesViews.entries) {
                    val device = entry.key
                    val pos = getPosition(entry.value)

                    if(pos == null) {
                        viewModel.configuredDevices.remove(device.BSSID)
                        continue
                    }

                    if(viewModel.configuredDevices.containsKey(device.BSSID)) {
                        viewModel.configuredDevices[device.BSSID]!!.position = pos
                    } else {
                        viewModel.configuredDevices[device.BSSID] = Device(device, pos)
                    }
                }

                val configuredSensorsCount = viewModel.configuredDevices.values.size

                if (configuredSensorsCount == 3) {
                    viewModel.isConfigured = true
                } else {
                    viewModel.isConfigured = false
                    Toast.makeText(
                        this,
                        "Exactly 3 sensors must be configured, $configuredSensorsCount found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .create()
            .show()
    }

    private fun getPosition(deviceConfigItemBinding: DeviceConfigItemBinding): CanvasView.Position? {
        val x = deviceConfigItemBinding.deviceX.text.toString().toFloatOrNull()
        val y = deviceConfigItemBinding.deviceY.text.toString().toFloatOrNull()
        return if (x != null && y != null) {
            return CanvasView.Position(x, y)
        } else null
    }

    private fun getDeviceDesc(scanResult: ScanResult) =
        """
           Name: ${scanResult.SSID}
           MAC: ${scanResult.BSSID}""".trimIndent()


    private fun updateRttDistance() {
        val request: RangingRequest = RangingRequest.Builder().run {
            addAccessPoints(viewModel.configuredDevices.values.map { it.scanResult!! })
            build()
        }

        requestUpdateRttDistance(request)
    }

    private fun requestUpdateRttDistance(request: RangingRequest) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            wifiRttManager!!.startRanging(request, mainExecutor, object : RangingResultCallback() {

                override fun onRangingResults(results: List<RangingResult>) {
                    results.forEach { viewModel.addScanResult(it) }
                    if (viewModel.isConfigured) {
                        viewBinding.canvas.setMeasurements(viewModel.measurements.values())
                    }
                }

                override fun onRangingFailure(code: Int) {
                    Toast.makeText(this@WifiActivity, "RTT ranging failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        } else {
            Toast.makeText(this, "Location permission missing", Toast.LENGTH_SHORT).show()
        }
    }

}