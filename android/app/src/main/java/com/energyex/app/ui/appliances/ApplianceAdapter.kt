package com.energyex.app.ui.appliances

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.energyex.app.data.model.Appliance
import com.energyex.app.databinding.ItemApplianceBinding
import java.util.Locale

class ApplianceAdapter(
    private var appliances: List<Appliance>,
    private val onToggle: (Appliance, Boolean) -> Unit,
    private val onItemClick: (Appliance) -> Unit
) : RecyclerView.Adapter<ApplianceAdapter.ApplianceViewHolder>() {

    fun updateData(newAppliances: List<Appliance>) {
        this.appliances = newAppliances
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplianceViewHolder {
        val binding = ItemApplianceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApplianceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ApplianceViewHolder, position: Int) {
        holder.bind(appliances[position])
    }

    override fun getItemCount(): Int = appliances.size

    inner class ApplianceViewHolder(private val binding: ItemApplianceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appliance: Appliance) {
            binding.tvApplianceName.text = appliance.name
            binding.tvApplianceChannel.text = "Channel ${appliance.relayChannel} • Rating: ${appliance.powerRatingWatts}W"
            binding.tvLivePower.text = String.format(Locale.US, "%.1f W", appliance.currentPowerW)
            binding.tvEnergyConsumed.text = String.format(Locale.US, "%.2f kWh", appliance.cumulativeEnergyKwh)
            binding.tvEstimatedCost.text = String.format(Locale.US, "₹ %.2f", appliance.estimatedCostInr)

            binding.tvApplianceIcon.text = when (appliance.applianceType.uppercase()) {
                "LIGHT" -> "💡"
                "FAN" -> "🌀"
                "TV" -> "📺"
                "REFRIGERATOR" -> "❄️"
                "AC" -> "❄️"
                else -> "🔌"
            }

            // Remove listener before setting checked state to avoid circular events
            binding.switchPower.setOnCheckedChangeListener(null)
            binding.switchPower.isChecked = appliance.isOn

            binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
                onToggle(appliance, isChecked)
            }

            binding.root.setOnClickListener {
                onItemClick(appliance)
            }
        }
    }
}

