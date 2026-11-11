package com.energyex.app.ui.appliances

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.energyex.app.EnergyExApp
import com.energyex.app.data.model.Appliance
import com.energyex.app.databinding.FragmentAppliancesBinding
import kotlinx.coroutines.launch

class ApplianceListFragment : Fragment() {

    private var _binding: FragmentAppliancesBinding? = null
    private val binding get() = _binding!!

    private val repository get() = EnergyExApp.instance.repository
    private lateinit var adapter: ApplianceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppliancesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ApplianceAdapter(
            emptyList(),
            onToggle = { appliance, isChecked ->
                val state = if (isChecked) "ON" else "OFF"
                viewLifecycleOwner.lifecycleScope.launch {
                    val res = repository.controlAppliance(appliance.id, state)
                    res.onSuccess { cmd ->
                        Toast.makeText(requireContext(), "${appliance.name} switched $state (${cmd.executionStatus})", Toast.LENGTH_SHORT).show()
                        loadAppliances()
                    }.onFailure {
                        Toast.makeText(requireContext(), "Control failed", Toast.LENGTH_SHORT).show()
                        loadAppliances()
                    }
                }
            },
            onItemClick = { appliance ->
                showApplianceDetailsDialog(appliance)
            }
        )

        binding.rvAppliances.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAppliances.adapter = adapter

        binding.swipeRefreshAppliances.setOnRefreshListener {
            loadAppliances()
        }

        binding.fabAddAppliance.setOnClickListener {
            showAddApplianceDialog()
        }

        loadAppliances()
    }

    private fun loadAppliances() {
        binding.swipeRefreshAppliances.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getAppliances()
            binding.swipeRefreshAppliances.isRefreshing = false
            result.onSuccess { list ->
                adapter.updateData(list)
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Error: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddApplianceDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etName = EditText(context).apply { hint = "Appliance Name (e.g. Geyser / AC)" }
        val etRating = EditText(context).apply { hint = "Power Rating in Watts (e.g. 1500)" }
        val etChannel = EditText(context).apply { hint = "Relay Channel (1 to 4)" }

        layout.addView(etName)
        layout.addView(etRating)
        layout.addView(etChannel)

        AlertDialog.Builder(context)
            .setTitle("Add New Home Appliance")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                val watts = etRating.text.toString().toDoubleOrNull() ?: 100.0
                val channel = etChannel.text.toString().toIntOrNull() ?: 1

                if (name.isNotEmpty()) {
                    val newApp = Appliance(
                        name = name,
                        applianceType = "OTHER",
                        relayChannel = channel,
                        sensorChannel = channel,
                        powerRatingWatts = watts,
                        isOn = false
                    )
                    viewLifecycleOwner.lifecycleScope.launch {
                        repository.createAppliance(newApp)
                        loadAppliances()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showApplianceDetailsDialog(appliance: Appliance) {
        val details = """
            Name: ${appliance.name}
            Type: ${appliance.applianceType}
            Relay Channel: ${appliance.relayChannel}
            Measurement Channel: ${appliance.sensorChannel}
            Rated Power: ${appliance.powerRatingWatts} W
            Current Power: ${appliance.currentPowerW} W
            Voltage: ${appliance.voltageV} V
            Current: ${appliance.currentA} A
            Cumulative Energy: ${appliance.cumulativeEnergyKwh} kWh
            Estimated Cost: ₹ ${appliance.estimatedCostInr}
            Connection: ${appliance.connectionStatus}
            Mode: ${if (appliance.isSimulated) "SIMULATED (Demo Data)" else "LIVE HARDWARE"}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Appliance Channel Diagnostics")
            .setMessage(details)
            .setPositiveButton("Close", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

