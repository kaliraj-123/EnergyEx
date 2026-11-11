package com.energyex.app.ui.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.energyex.app.EnergyExApp
import com.energyex.app.databinding.FragmentLiveMonitorBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class LiveMonitoringFragment : Fragment() {

    private var _binding: FragmentLiveMonitorBinding? = null
    private val binding get() = _binding!!

    private val repository get() = EnergyExApp.instance.repository
    private val voltagePoints = mutableListOf<Float>()
    private var liveStreamJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveMonitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startTelemetryStreaming()
    }

    private fun startTelemetryStreaming() {
        liveStreamJob?.cancel()
        liveStreamJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val result = repository.getDashboardSummary()
                result.onSuccess { summary ->
                    binding.tvGaugeVoltage.text = String.format(Locale.US, "%.1f V", summary.currentVoltageV)
                    binding.tvGaugeCurrent.text = String.format(Locale.US, "%.3f A", summary.currentCurrentA)
                    binding.tvGaugePower.text = String.format(Locale.US, "%.1f W", summary.currentPowerWatts)
                    binding.tvGaugeEnergy.text = String.format(Locale.US, "%.2f kWh", summary.totalEnergyKwh)
                    binding.tvGaugeCost.text = String.format(Locale.US, "Cost: ₹ %.2f", summary.estimatedBillInr)
                    binding.tvGaugeFrequency.text = String.format(Locale.US, "%.2f Hz", summary.frequencyHz)
                    binding.tvGaugePowerFactor.text = String.format(Locale.US, "%.2f PF", summary.powerFactor)

                    binding.tvLiveMeterSub.text = "${summary.dataLabel} • Last sync: ${summary.lastDataUpdate ?: "Streaming"}"

                    voltagePoints.add(summary.currentVoltageV.toFloat())
                    if (voltagePoints.size > 16) voltagePoints.removeAt(0)
                    binding.liveMonitorChart.setData(voltagePoints)
                }
                delay(2500)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        liveStreamJob?.cancel()
        _binding = null
    }
}

