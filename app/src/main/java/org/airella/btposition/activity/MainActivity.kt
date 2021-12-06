package org.airella.btposition.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.airella.btposition.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewBinding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }

    fun startBtActivity(view: View) {
        startActivity(Intent(this, BtActivity::class.java))
    }

    fun startWifiActivity(view: View) {
        startActivity(Intent(this, WifiActivity::class.java))
    }

}