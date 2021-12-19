package org.airella.btposition.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.airella.btposition.databinding.ActivityBtBinding
import org.airella.btposition.databinding.DeviceItemBinding
import org.airella.btposition.model.Device
import org.airella.btposition.utils.Log

class BtActivity : AppCompatActivity() {

    private val viewBinding: ActivityBtBinding by lazy { ActivityBtBinding.inflate(layoutInflater) }

    private lateinit var viewModel: BtViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewModel = ViewModelProvider(this).get(BtViewModel::class.java)

        viewBinding.results.adapter = viewModel.adapter
        viewBinding.results.layoutManager = LinearLayoutManager(this)

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2137)
        } else {
            Log.i("Permission already granted")
        }

    }

    override fun onStart() {
        super.onStart()
        viewModel.startBtScan(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopBtScan(this)
    }

    fun configureSensors(view: View) {
        val devices = viewModel.devices.values.toMutableList()
        if (devices.size < 3) {
            Toast.makeText(this, "Three sensors needed, ${devices.size} found", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val devicesConfigBinding = DeviceItemBinding.inflate(LayoutInflater.from(this))

        devicesConfigBinding.device1Description.text = getDeviceDesc(devices[0])
        devicesConfigBinding.device2Description.text = getDeviceDesc(devices[1])
        devicesConfigBinding.device3Description.text = getDeviceDesc(devices[2])

        AlertDialog.Builder(this)
            .setView(devicesConfigBinding.root)
            .setPositiveButton("Save") { _, _ ->
                setPosition(
                    devices[0],
                    devicesConfigBinding.device1X,
                    devicesConfigBinding.device1Y
                )
                setPosition(
                    devices[1],
                    devicesConfigBinding.device2X,
                    devicesConfigBinding.device2Y
                )
                setPosition(
                    devices[2],
                    devicesConfigBinding.device3X,
                    devicesConfigBinding.device3Y
                )
            }
            .create()
            .show()
    }

    private fun setPosition(device: Device, xEditText: EditText, yEditText: EditText) {
        val x = xEditText.text.toString().toFloatOrNull() ?: 0f
        val y = yEditText.text.toString().toFloatOrNull() ?: 0f
        device.position = CanvasView.Position(x, y)
    }

    private fun getDeviceDesc(device: Device) =
        """
           Name: ${device.name}
           MAC: ${device.mac}""".trimIndent()

}