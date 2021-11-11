package org.airella.btposition.acitivity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.airella.btposition.R
import org.airella.btposition.bt.BluetoothScanService
import org.airella.btposition.utils.DistanceCalculator
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        viewModel.timeToUpdate.observe(this, {
            time_to_update.text = "Time to update: $it"
        })

        viewModel.scanResult.observe(this, { scanResult ->
            val distance = DistanceCalculator.calculateDistance(scanResult.rssi)

            main_text_info.text = """
            Name: ${scanResult?.device?.name}
            MAC: ${scanResult?.device?.address}
            RSSI: ${scanResult.rssi}
            Distance: $distance
            """
        })
    }

    fun onClick(view: View) {
        view.visibility = View.GONE
        viewModel.startScanTimer(this)
    }

}