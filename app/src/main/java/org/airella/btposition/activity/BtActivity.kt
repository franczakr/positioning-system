package org.airella.btposition.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.airella.btposition.databinding.ActivityBtBinding

class BtActivity : AppCompatActivity() {

    private val viewBinding: ActivityBtBinding by lazy { ActivityBtBinding.inflate(layoutInflater) }

    private lateinit var viewModel: BtViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewModel = ViewModelProvider(this).get(BtViewModel::class.java)

        viewModel.counter.observe(this, {
            viewBinding.timeToUpdate.text = "Counter: $it"
        })

        viewBinding.results.adapter = viewModel.adapter
        viewBinding.results.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        viewModel.startScanTimer(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopScanTimer()
    }

}