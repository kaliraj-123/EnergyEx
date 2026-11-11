package com.energyex.app.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.energyex.app.EnergyExApp
import com.energyex.app.R
import com.energyex.app.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch
import java.util.Locale

class ConsumptionHistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val repository get() = EnergyExApp.instance.repository
    private var selectedRange = "daily"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toggleGroupRange.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedRange = when (checkedId) {
                    R.id.btnWeekly -> "weekly"
                    R.id.btnMonthly -> "monthly"
                    else -> "daily"
                }
                loadHistoryData(selectedRange)
            }
        }

        loadHistoryData(selectedRange)
    }

    private fun loadHistoryData(range: String) {
        binding.tvHistoryList.text = "Fetching $range records from database..."

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getConsumptionHistory(range)
            result.onSuccess { readings ->
                if (readings.isEmpty()) {
                    binding.tvHistoryList.text = "No history records found for $range yet. Readings are generated periodically in simulation and IoT modes."
                    binding.historyChartView.setData(listOf(10f, 25f, 40f, 35f, 60f))
                    return@onSuccess
                }

                val totalEnergy = readings.map { it.energyKwh }.maxOrNull() ?: 0.0
                val totalCost = readings.map { it.costInr }.maxOrNull() ?: 0.0

                binding.tvHistoryTotalKwh.text = String.format(Locale.US, "%.2f kWh", totalEnergy)
                binding.tvHistoryTotalCost.text = String.format(Locale.US, "₹ %.2f", totalCost)

                val points = readings.takeLast(20).map { it.activePowerW.toFloat() }
                binding.historyChartView.setData(points)

                val logSummary = readings.takeLast(10).reversed().joinToString("\n") { r ->
                    "• [${r.timestamp.take(19)}] ${r.activePowerW}W • ${r.voltageV}V • ${r.currentA}A (${r.readingSource})"
                }
                binding.tvHistoryList.text = logSummary
            }.onFailure {
                binding.tvHistoryList.text = "Failed to load history: ${it.localizedMessage}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

