package org.airella.btposition.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.airella.btposition.R
import org.airella.btposition.databinding.ResultItemBinding
import org.airella.btposition.utils.DistanceCalculator

class RssiResultAdapter(private val data: List<RssiResult>) : RecyclerView.Adapter<RssiResultAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val description: TextView

        init {
            val viewBinding = ResultItemBinding.bind(view)
            description = viewBinding.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.result_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val btResult = data[position]
        val device = btResult.device
        val rssi = btResult.rssi

        val distance = DistanceCalculator.calculateDistance(rssi)

        val text = """
            Name: ${device.name}
            MAC: ${device.mac}
            RSSI: $rssi
            Distance: $distance
            """

        holder.description.text = text
    }

    override fun getItemCount(): Int = data.size
}