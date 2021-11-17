package org.airella.btposition.acitivity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.airella.btposition.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewBinding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var viewModel: MainActivityViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        viewModel.timeToUpdate.observe(this, {
            viewBinding.timeToUpdate.text = "Time to update: $it"
        })

        viewBinding.results.adapter = viewModel.adapter
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