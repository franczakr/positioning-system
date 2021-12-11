package org.airella.btposition.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
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

        viewBinding.canvas.setVisualDebug(false)
        viewBinding.canvas.setMargin(0.05f)
        viewBinding.canvas.setColor1(Color.parseColor("#A2142F"))
        viewBinding.canvas.setColor2(Color.parseColor("#4DBEEE"))
        viewBinding.canvas.setColor3(Color.parseColor("#77AC30"))
        assignListenerToSeekBar(viewBinding.x1, viewBinding.canvas::setX1, 1f)
        assignListenerToSeekBar(viewBinding.y1, viewBinding.canvas::setY1, 1f)
        assignListenerToSeekBar(viewBinding.s1, viewBinding.canvas::setSignal1, 2f)
        assignListenerToSeekBar(viewBinding.x2, viewBinding.canvas::setX2, 1f)
        assignListenerToSeekBar(viewBinding.y2, viewBinding.canvas::setY2, 1f)
        assignListenerToSeekBar(viewBinding.s2, viewBinding.canvas::setSignal2, 2f)
        assignListenerToSeekBar(viewBinding.x3, viewBinding.canvas::setX3, 1f)
        assignListenerToSeekBar(viewBinding.y3, viewBinding.canvas::setY3, 2f)
        assignListenerToSeekBar(viewBinding.s3, viewBinding.canvas::setSignal3, 2f)
    }

    override fun onStart() {
        super.onStart()
        viewModel.startBtScan(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopBtScan(this)
    }

    fun assignListenerToSeekBar(seekBar: SeekBar, setter: (Float) -> Unit, maxValue: Float) {
        val seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setter(progress / 100f * maxValue)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) { }
            override fun onStopTrackingTouch(seekBar: SeekBar) { }
        }
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener)
    }
}