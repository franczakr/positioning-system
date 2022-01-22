package org.airella.btposition.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import org.airella.btposition.bt.BluetoothScanService
import org.airella.btposition.databinding.ActivityBtBinding
import org.airella.btposition.databinding.DeviceConfigItemBinding
import org.airella.btposition.model.Device
import org.airella.btposition.utils.Log
import org.airella.btposition.view.CanvasView

class BtActivity : AppCompatActivity() {

    private val viewBinding: ActivityBtBinding by lazy { ActivityBtBinding.inflate(layoutInflater) }

    private lateinit var viewModel: BtViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewModel = ViewModelProvider(this).get(BtViewModel::class.java)

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2137)
        } else {
            Log.i("Permission already granted")
        }
    }

    override fun onStart() {
        super.onStart()
        startBtScan()
    }

    override fun onStop() {
        super.onStop()
        stopBtScan()
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun startBtScan() {
        try {
            BluetoothScanService.scanBTDevices(scanCallback, true)
            Log.i("Started scanning")
        } catch (e: Exception) {
            Log.w("BT disabled")
            Toast.makeText(this, "BT disabled", Toast.LENGTH_SHORT).show()
            mainHandler.postDelayed({ startBtScan() }, 3000)
        }
    }

    private fun stopBtScan() {
        try {
            BluetoothScanService.scanBTDevices(scanCallback, false)
            Log.i("Stopped scanning")
        } catch (e: Exception) {
            Log.w("BT disabled")
            Toast.makeText(this, "BT disabled", Toast.LENGTH_SHORT).show()
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

        val devicesViews: MutableMap<Device, DeviceConfigItemBinding> = mutableMapOf()

        for (device in devices) {
            val deviceItemBinding = DeviceConfigItemBinding.inflate(LayoutInflater.from(this))
            devicesViews[device] = deviceItemBinding
            deviceItemBinding.deviceDescription.text = getDeviceDesc(device)
            deviceItemBinding.deviceX.setText(device.position?.x?.toString() ?: "")
            deviceItemBinding.deviceY.setText(device.position?.y?.toString() ?: "")
            layout.addView(deviceItemBinding.root)
        }

        AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                for (entry in devicesViews.entries) {
                    setPosition(entry.key, entry.value)
                }

                val configuredSensorsCount = viewModel.devices.values.map { it.isConfigured() }.count { it }

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

    private fun setPosition(device: Device, deviceConfigItemBinding: DeviceConfigItemBinding) {
        val x = deviceConfigItemBinding.deviceX.text.toString().toFloatOrNull()
        val y = deviceConfigItemBinding.deviceY.text.toString().toFloatOrNull()

        device.position =
            if (x != null && y != null) {
                CanvasView.Position(x, y)
            } else {
                null
            }
    }

    private fun getDeviceDesc(device: Device) =
        """
           Name: ${device.name}
           MAC: ${device.mac}""".trimIndent()

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("Scan failed with code $errorCode")
        }

        @SuppressLint("SetTextI18n")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.i("Found device")
            viewModel.addScanResult(result)
            if (viewModel.measurements.measurements.size == 3) {
                viewBinding.canvas.setMeasurements(viewModel.measurements.values())
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            Log.i("Found device batch")
            results.forEach { viewModel.addScanResult(it) }
            if (viewModel.measurements.measurements.size == 3) {
                viewBinding.canvas.setMeasurements(viewModel.measurements.values())

            }
        }
    }

}