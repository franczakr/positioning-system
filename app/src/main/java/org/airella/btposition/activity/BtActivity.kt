package org.airella.btposition.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.airella.btposition.databinding.ActivityBtBinding
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

}